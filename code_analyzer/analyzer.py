#!/usr/bin/python3

import os, sys, inspect, subprocess, tempfile

# Include the parent directory in the search path.
cmd_folder = os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0])
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn, DEFAULT_TAG, BACKUP_TOP, config, problems

from datetime import datetime
import itertools
import time
import calendar
import glob

# Edit distance, for approximate problem name matching.
def editDist( a, b ):
    dst = [ [ 0 for x in range( 0, len( b ) + 1 ) ] for y in range( 0, len( a ) + 1 ) ]

    for i in range( 0, len( a ) + 1 ):
        dst[ i ][ 0 ] = i

    for j in range( 0, len( b ) + 1 ):
        dst[ 0 ][ j ] = j

    for i in range( 1, len( a ) + 1 ):
        for j in range( 1, len( b ) + 1 ):
            if ( a[ i - 1 ] == b[ j - 1 ] ):
                dst[ i ][ j ] = dst[ i - 1 ][ j - 1 ]
            else:
                dst[ i ][ j ] = dst[ i - 1 ][ j - 1 ] + 1

                if dst[ i - 1 ][ j ] + 1 < dst[ i ][ j ]:
                    dst[ i ][ j ] = dst[ i - 1 ][ j ] + 1

                if dst[ i ][ j - 1 ] + 1 < dst[ i ][ j ]:
                    dst[ i ][ j ] = dst[ i ][ j - 1 ] + 1

    return dst[ len( a ) ][ len( b ) ]

def repeatedString( str, pat ):
    """Return true if str is one or more repeated copies of pat."""

    n = len( pat );
    if len( str ) < n:
        return 0

    for j in range( 0, len( str ) ):
        if str[ j ] != pat[ j % n ]:
            return 0;

    return 1;



class File:
    def __init__( self, path, time ):
        # Set team directory, path under that and modification time.
        self.path = path
        self.time = time

        # File size in bytes
        self.size = 0

        # Number of lines in the file (once we compute it)
        self.lineCount = 0

        # Report of lines added and removed (from git)
        self.linesChanged = 0

    def __repr__(self):
        return '%s' % self.path

# Representation for the value part of the file_to_problem mapping,
# i.e., one line from the file_to_problem table.  This used to be a
# 4-tuple.  When the lang_id was added, this struct was created to
# help with the transition to 5 fields and to make the structure
# easier to interpret.
class MappingRec:
    def __init__( self, db_id, problem_id, lang_id, override, new_problem_id ):
        # Remember the five database fields.
        self.db_id = db_id
        self.problem_id = problem_id
        self.lang_id = lang_id
        self.override = override
        self.new_problem_id = new_problem_id


# This functionality needs to be split into an executable analyzer and
# a reusable classifier.  That will help with testing.
class Analyzer:
    def __init__( self, basePath ):
        # path to the top of the backup directory.
        self.basePath = basePath

        # index of the last team in the competition.
        self.lastTeam = config[ "teambackup" ][ "lastTeam" ]

        # interval for updating team backups, used in this script to freshen stale modification
        # times, if it looks like there's a reason.
        self.backupInterval = config[ "teambackup" ][ "interval" ]

        # Strings to strip from the filename before we try to guess
        self.commonStrips = [ 'problem', 'prob', '_', '-' ]

        # Valid source file extensions, and what each one says
        # about the source language.
        self.extensionMap = {
            'cc': "C++",
            'cpp': "C++",
            'c': "C",
            'java': "Java",
            'kt': "Kotlin",
            'py': "Python" # FIXME: do we want to discern between Python 2/3?
            }

        # map from problem ID to a list of keywords to look for.
        self.probKeywords = {}

        # List of problems.  We use this mostly as the offficial
        # list of problem letters, from the configuration.
        self.problemList = problems

        # Contest start time in Unix seconds from the database.
        cursor = dbConn.cursor()
        cursor.execute( "SELECT start_time FROM contests ORDER BY id DESC LIMIT 1" )
        row = cursor.fetchone()
        if ( row == None ):
            print("Error: no contest found in the database.")
            exit(1)

        self.contestStart = row[0]

        # For each team, a list of team-specific strips from filenames.
        self.teamStrips = {}

        # we use the next two fields to hold copies of database
        # information (the file_to_problem and file_modtime tables)
        # while the script is running, and to update the tables once
        # the script has run.

        # For every team and path, this is a triple, datatabase_id,
        # latest modification time and File object (if the file has
        # has changed).  We only add a new entry to edit_activity if
        # it's sufficiently newer than what we have there or if we just
        # committed and git reports that a file has changed.
        self.lastEditTimes = {}

        # map from team_id and path to a MappingRec instance
        # containing db_id, problem_id, lang_id, override flag and new
        # problem ID (if we just generated a new mapping).  This lets
        # us know what to ignore in the mapping and what to update
        # when we re-write the database.  Multiple files may map to
        # the same problem, if the team is working on multiple
        # versions or has some supporting files.
        self.fileMappings = {}

    def loadConfiguration( self ):
        # Read the list of problem names
        self.probKeywords = {}

        cursor = dbConn.cursor()
        cursor.execute( "SELECT problem_id, keyword FROM problem_keywords" )
        row = cursor.fetchone()
        while ( row != None ):
            if ( row[ 0 ] in self.probKeywords ):
                self.probKeywords[ row[ 0 ] ].append( row[ 1 ].lower() )
            else:
                self.probKeywords[ row[ 0 ] ] = [ row[ 1 ].lower() ]

            row = cursor.fetchone()

        # get latest known edit times for every team/path
        cursor.execute( "SELECT id, team_id, path, modify_timestamp FROM file_modtime" )
        row = cursor.fetchone()
        while ( row != None ):
            self.lastEditTimes[ ( row[ 1 ], row[ 2 ] ) ] = [ row[ 0 ], row[ 3 ], None ]

            row = cursor.fetchone()

        # get existing mapping records for all mapped files.
        cursor.execute( "SELECT id, team_id, path, problem_id, lang_id, override FROM file_to_problem" )
        row = cursor.fetchone()
        while ( row != None ):
            self.fileMappings[ ( int( row[ 1 ] ), row[ 2 ] ) ] = MappingRec( row[ 0 ], row[ 3 ], row[ 4 ], row[ 5 ], None )

# Old mapping
# 0 -> id, 1 -> problem_id, 2 -> override, 3 -> new_problem_id

            row = cursor.fetchone()

        # load any team-specific strips.
        cursor.execute( "SELECT team_id, str FROM team_strips" )
        row = cursor.fetchone()
        while ( row != None ):
            if ( row[ 0 ] in self.teamStrips ):
                self.teamStrips[ row[ 0 ] ].append( row[ 1 ].lower() )
            else:
                self.teamStrips[ row[ 0 ] ] = [ row[ 1 ].lower() ]

            row = cursor.fetchone()

        cursor.close()

    def parseGitDiffs( self, bdir ):
        """Get a full report of differences between the current
        revision and the previous one.  Return as a map from path name
        (teamxxx/path/to/file) to a tuple giving lines removed and
        lines added."""
        origin = os.getcwd()
        os.chdir( bdir )

        statFile = tempfile.TemporaryFile()

        # Get the report.
        subprocess.call( [ "git", "diff", "--numstat", "HEAD^", "HEAD" ], stdout=statFile )

        # Each line is lines added, lines removed, path
        result = {}
        statFile.seek( 0 )
        for line in statFile:
            fields = line.rstrip().decode("utf-8").split( "\t" )
            # Git still tracks binary files, but it doesn't report lines
            # changed.  Looks like it just reports a dash instead, but
            # we ignore anything that's not an int.
            try:
                result[ fields[ 2 ] ] = ( int( fields[ 0 ] ), int( fields[ 1 ] ) )
            except ValueError:
                pass

        os.chdir( origin )

        return result;

    def countLines(self, p):
        """Given path p, count the number of lines in the file it points to."""
        f = open( p )
        lineCount = sum( 1 for line in f)
        f.close()
        return lineCount

    def checkAutosaves( self, f ):
        # Split into directory and file.
        ( dirName, fileName ) = os.path.split( f )

        autoTime = None

        # is it an emacs autosave file
        autoFile = "%s/#%s#" % ( dirName, fileName )
        if os.path.exists( autoFile ):
            autoTime = os.path.getmtime( autoFile )

        # is it a vim autosave file
        autoFile = "%s/.%s.swp" % ( dirName, fileName )
        if os.path.exists( autoFile ):
            newTime = os.path.getmtime( autoFile )
            if ( autoTime == None or newTime > autoTime ):
                autoTime = newTime

        return autoTime

    def stripDecoration( self, strips, str ):
        last = None
        while str != last:
            last = str
            for s in strips:
                idx = str.find( s )
                while idx != -1:
                    str = str[:idx] + str[(idx + len( s )):]
                    idx = str.find( s )

        return str

    def guessProblem( self, team, path ):
        """Should return the most likely problem id for this file,
           or None.
           """
        # Split into directory and file.
        ( dirName, fileName ) = os.path.split( path )
        dirName = dirName.lower()
        fileName = fileName.lower()

        # Build general and team-specific strips.  I'm sure
        # there's a better way to do this.
        strips = []
        for x in self.commonStrips:
            strips.append( x )
        if ( team in self.teamStrips ):
            for x in self.teamStrips[ team ]:
                strips.append( x )

        baseName, extension = os.path.splitext( fileName )
        extension = extension.lstrip( '.' )

        if extension not in self.extensionMap:
            return None

        # Strip off extra words
        shortName = self.stripDecoration( strips, baseName )

        # Ordering here is a little bit important.  We look first
        # at the matches that are more confident.  Then, we look
        # at the ones that are less likely

        # First, consider just the filename against all problem keywords.
        for problem_id, keywords in self.probKeywords.items():
            for keyword in keywords:
                # tsp.cpp -> a
                if shortName == keyword:
                    return problem_id

        # Here, we try to match against arbitrarily many occurrences of the problem
        # letter.  Some teams are using names like aaa.c for their third attempt.
        for problem_id in self.problemList:
            # a.cpp -> a or aaa.cpp -> a
            if repeatedString( shortName, problem_id.lower() ):
                return problem_id

        # Then, start looking at the path.
        if len( dirName ) > 0:
            dirList = dirName.split( '/' )

            for dir in dirList:
                shortDirName = self.stripDecoration( strips, dir )

                for problem_id, keywords in self.probKeywords.items():
                    for keyword in keywords:
                        # tsp/sol.java -> a
                        if shortDirName == keyword:
                            return problem_id

                for problem_id in self.problemList:
                    # a/code.cpp -> a or aaa/code.cpp -> a
                    if repeatedString( shortDirName, problem_id.lower() ):
                        return problem_id


        # Then, take matches that occur anywhere in the problem name
        for problem_id, keywords in self.probKeywords.items():
            for keyword in keywords:
                if ( keyword in baseName ):
                    return problem_id

        # Then, the problem letter attached to some other word with a
        # non-alpha character.
        for problem_id in self.problemList:
            letter = problem_id.lower()

            # b_2.c -> b
            if ( len( baseName ) > len( letter ) and
                 baseName.startswith( letter ) and
                 not baseName[ len( letter ) ].isalpha() ):
                return problem_id

            # losning_b.c -> b
            if ( len( baseName ) > len( letter ) and
                 baseName.endswith( letter ) and
                 not baseName[ -( len( letter ) + 1  )].isalpha() ):
                return problem_id


        # Then, look for path elements containing the name.
        if len( dirName ) > 0:
            dirList = dirName.split( '/' )

            for dir in dirList:
                shortDirName = self.stripDecoration( strips, dir )

                for problem_id, keywords in self.probKeywords.items():
                    for keyword in keywords:
                        # retry_b/sol.java -> b
                        if keyword in shortDirName:
                            return problem_id

        # Then, try an approximate match against a keyword, willing to miss
        # a fraction of the total characters.
        for problem_id, keywords in self.probKeywords.items():
            for keyword in keywords:
                if ( len( keyword ) > 3 and
                     editDist( keyword, shortName ) <= len( keyword ) * 0.25 ):
                    return problem_id


        # Then, look for approximate matches in any directory element.
        if len( dirName ) > 0:
            dirList = dirName.split( '/' )
            for dir in dirList:
                shortDirName = self.stripDecoration( strips, dir )
                for problem_id, keywords in self.probKeywords.items():
                    for keyword in keywords:
                        if ( len( keyword ) > 3 and
                             editDist( keyword, shortDirName ) <= len( keyword ) * 0.25 ):
                            return problem_id
        return None

    def guessPath( self, team, path ):
        """Testing interface for problem guessing"""
        self.loadConfiguration()
        return self.guessProblem( team, path )


    def checkActivity( self, bdir, tag ):
        """Scan the given backup dir and generate reports of the state of
        files believed to correspond to various problems in the problem set."""

        # Time when this script started running.
        scriptStartTime = int( time.time() )

        self.loadConfiguration()

        # Get diff reports from git.
        gitDiffs = self.parseGitDiffs( bdir )

        # We should rethink some of the following loop.  Right now, it
        # tries to find file changes, including changes to editor auto-save
        # files.  But, to report changed lines in the file, we depend on git
        # (which probably isn't tracking these auto-save files, so we'd have
        # nothing to report)

        # Visit home directory for each team.
        tlist = sorted( glob.glob( bdir + '/team*' ) )
        for tdir in tlist:
            ( dirname, tname ) = os.path.split( tdir )
            team = int( tname.lstrip( 'team' ) )
            cmd = "find %s/ -type f" % tdir
            for f in os.popen( cmd ).readlines():
                f = f.rstrip( '\n' )
                fname = f[len(tdir) + 1:]
                ( dummy, extension ) = os.path.splitext( fname )
                extension = extension.lstrip( '.' )
                if extension in self.extensionMap:
                    fobj = File( fname, os.path.getmtime( f ) )

                    # Get lines changed, etc.  We need to consult the git diff output.
                    # The tag is to make sure we only record lines changed on an analysis
                    # pass that's paired to a git commit.  Independent analysis passes
                    # don't get this, since that could infate the appearance of how
                    # much editing is being done.
                    gitPath = f[len(bdir) + 1:]
                    if gitPath in gitDiffs and tag != DEFAULT_TAG:
                        fobj.linesChanged = gitDiffs[ gitPath ][ 0 ] + gitDiffs[ gitPath ][ 1 ];

                    mappingRec = None
                    lastEditRec = None

                    # Files with completely implausible modification times can get ignored.
                    ignoreEdit = False

                    # see if there's a mapping for this file.
                    if ( team, fname ) in self.fileMappings:
                        mappingRec = self.fileMappings[ ( team, fname ) ]

                    # If there's no forced mapping for this problem, try to guess one.
                    if ( mappingRec == None or mappingRec.override == 0 ):
                        prob = self.guessProblem( team, fobj.path )
                        if prob != None:
                            if mappingRec == None:
                                mappingRec = MappingRec( None, None, self.extensionMap[ extension ],
                                                         0, None );
                                self.fileMappings[ ( team, fname ) ] = mappingRec

                            if mappingRec.problem_id != prob:
                                mappingRec.new_problem_id = prob;

                    # see if there's an edit record for this file.
                    if ( team, fname ) in self.lastEditTimes:
                        lastEditRec = self.lastEditTimes[ ( team, fname ) ]

                    # check common editor auto-saves, to see if there
                    # is a fresher modification time.
                    autoTime = self.checkAutosaves( f );
                    if ( autoTime != None and autoTime > fobj.time ):
                        fobj.time = autoTime

                    # Try to guard against anomalous file edit times. These are unlikely to happen,
                    # but they could look strange in the report or even suppress tracking of files
                    # if they have a modification time in the future.

                    # No edits should happen before the start of the contest or after right now.
                    if fobj.time < self.contestStart:
                        fobj.time = self.contestStart

                    # If the file really changes, we definitely want to record it, possibly with
                    # a sane-ified modification time.
                    if fobj.time < scriptStartTime - 2 * self.backupInterval:
                        if fobj.linesChanged > 0:
                            fobj.time = int( scriptStartTime - self.backupInterval / 2 )

                    # If the file looks like it changed in the future, ignore it unless git agrees it's changing.
                    if fobj.time > scriptStartTime + self.backupInterval:
                        print("Future Modification: ", fobj.path, " changed ", fobj.linesChanged, " lines, ", (fobj.time - scriptStartTime), " seconds in the future")
                        if fobj.linesChanged > 0:
                            fobj.time = scriptStartTime
                        else:
                            ignoreEdit = True

                    # Is this newer than our last known edit?
                    # We don't just depend on git for this, since we can
                    # also watch auto-saves.
                    if not ignoreEdit and ( lastEditRec == None or lastEditRec[ 1 ] + 10 < fobj.time ):
                        if lastEditRec == None:
                            lastEditRec = [ None, None, None ]
                            self.lastEditTimes[ ( team, fname ) ] = lastEditRec

                        # Grab file size and number of lines.
                        fobj.size = os.path.getsize( f )
                        fobj.lineCount = self.countLines( f )

                        lastEditRec[ 2 ] = fobj;


        # Write out any new mappings
        cursor = dbConn.cursor()
        for k, v in self.fileMappings.items():
            if v.new_problem_id != None:
                if v.db_id == None:
                    update = "INSERT INTO file_to_problem (team_id, path, problem_id, lang_id, override ) VALUES ( %s, %s, %s, %s, %s )"

                    cursor.execute( update, ( k[ 0 ], k[ 1 ], v.new_problem_id, v.lang_id, 0 ) )
                else:
                    update = "UPDATE file_to_problem SET problem_id=%s WHERE id=%s"
                    cursor.execute( update, ( v.new_problem_id, v[ 0 ] ) )
                print("( %s, %s ) -> %s" % ( k[ 0 ], k[ 1 ], v.new_problem_id ))

        # Write out fresh edit times to file_modtime and new records to edit_activity
        cursor = dbConn.cursor()
        for k, v in self.lastEditTimes.items():
            if v[ 2 ] != None:
                t = v[ 2 ].time
                if v[ 0 ] == None:
                    update = "INSERT INTO file_modtime (team_id, path, modify_timestamp ) VALUES ( %s, %s, %s )"
                    cursor.execute( update, ( k[ 0 ], k[ 1 ], t ))
                else:
                    update = "UPDATE file_modtime SET modify_timestamp=%s WHERE id=%s"
                    cursor.execute( update, ( t, v[ 0 ] ) )

                # Compute time since start of contest.
                cmin = ( v[ 2 ].time - self.contestStart ) / 60

                update = "INSERT INTO edit_activity (team_id, path, modify_timestamp, modify_time, file_size_bytes, line_count, lines_changed, git_tag ) VALUES ( %s, %s, %s, %s, %s, %s, %s, %s )"

                cursor.execute( update, ( k[ 0 ], k[ 1 ] , t, cmin, v[ 2 ].size, v[ 2 ].lineCount, v[ 2 ].linesChanged, tag ) )


        # Create and write the summary of edit activity by problem, edit_latest

        # Map from team and problem_id to a triple, database_id,
        # timestamp and valid flag.  the valid flag lets us delete
        # database rows (say, if a file_to_problem mapping changes).  An
        # entry is valid as long as there is a file that's mapped to
        # the given problem, even if the file no longer exists.
        modLatest = {}

        # get latest known edit times for every team/problem.
        cursor.execute( "SELECT id, team_id, problem_id, modify_timestamp FROM edit_latest" )
        row = cursor.fetchone()
        while ( row != None ):
            modLatest[ ( row[ 1 ], row[ 2 ] ) ] = [ row[ 0 ], row[ 3 ], 0 ]
            row = cursor.fetchone()

        for k, v in self.fileMappings.items():
            prob = v.problem_id
            if v.new_problem_id != None:
                prob = v.new_problem_id

            if prob != None and prob != 'none':
                if k in self.lastEditTimes:
                    lastEditRec = self.lastEditTimes[ k ]
                    t = lastEditRec[ 1 ]
                    if lastEditRec[ 2 ] != None:
                        t = lastEditRec[ 2 ].time;

                    if ( k[ 0 ], prob ) in modLatest:
                        rec = modLatest[ ( k[ 0 ], prob ) ]
                        if t > rec[ 1 ]:
                            rec[ 1 ] = t
                        rec[ 2 ] = 1;
                    else:
                        modLatest[ ( k[ 0 ], prob ) ] = [ None, t, 1 ]

        for k, v in modLatest.items():
            t = v[ 1 ]
            if v[ 0 ] == None:
                update = "INSERT INTO edit_latest (team_id, problem_id, modify_timestamp ) VALUES ( %s, %s, %s )"

                cursor.execute( update, ( k[ 0 ], k[ 1 ], t ) )
            elif v[ 2 ]:
                update = "UPDATE edit_latest SET modify_timestamp=%s WHERE id=%s"
                cursor.execute( update, ( t, v[ 0 ] ))
            else:
                update = "DELETE FROM edit_latest WHERE id=%s"
                cursor.execute( update, ( v[ 0 ], ) )


    def reportUnclassified( self, bdir ):
        """Report all the source files that are not mapped to any problem
        yet."""

        self.loadConfiguration()

        # Visit home directory for each team.
        tlist = sorted( glob.glob( bdir + '/team*' ) )
        for tdir in tlist:
            ( dirname, tname ) = os.path.split( tdir )
            team = int( tname.lstrip( 'team' ) )
            cmd = "find %s/ -type f" % tdir
            for f in os.popen( cmd ).readlines():
                f = f.rstrip( '\n' )
                fname = f[len(tdir) + 1:]

                ( dummy, extension ) = os.path.splitext( fname )
                extension = extension.lstrip( '.' )
                if extension in self.extensionMap:
                    fobj = File( fname, os.path.getmtime( f ) )

                    prob = None;

                    # see if there's an override for this file.
                    if ( team, fname ) in self.fileMappings:
                        mappingRec = self.fileMappings[ ( team, fname ) ]
                        if mappingRec.override:
                            prob = mappingRec.problem_id
                            print("%s <= %s" % ( prob, f ))

                    # if it's not a forced mapping, try to guess and report that.
                    if prob == None:
                        # No forced problem, try to guess.
                        prob = self.guessProblem( team, fobj.path )

                        # report the file and the problem its assigned to.
                        if prob == None:
                            print("unknown <- %s" % ( f ))
                        else:
                            print("%s <- %s" % ( prob, f ))


if __name__ == '__main__':
    analyzer = Analyzer( BACKUP_TOP )

    tag = DEFAULT_TAG;
    if len( sys.argv ) > 1:
        tag = sys.argv[ 1 ]

    analyzer.checkActivity( BACKUP_TOP, tag )

