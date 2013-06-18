#!/usr/bin/python

# /home/icpc/team_backups

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0])
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn, BACKUP_TOP

from datetime import datetime, timedelta
import itertools
import time
import calendar
import glob

# Edit distance, for approximate problem name matching, not used right now.
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

# Longest common sequence, for approximate problem matching.
def lcs( a, b ):
    dst = [ [ 0 for x in range( 0, len( b ) + 1 ) ] for y in range( 0, len( a ) + 1 ) ]
    
    for i in range( 0, len( a ) + 1 ):
        dst[ i ][ 0 ] = 0

    for j in range( 0, len( b ) + 1 ):
        dst[ 0 ][ j ] = 0

    for i in range( 1, len( a ) + 1 ):
        for j in range( 1, len( b ) + 1 ):
            if ( a[ i - 1 ] == b[ j - 1 ] ):
                dst[ i ][ j ] = dst[ i - 1 ][ j - 1 ] + 1
            else:
                dst[ i ][ j ] = dst[ i - 1 ][ j - 1 ]

                if dst[ i - 1 ][ j ] > dst[ i ][ j ]:
                    dst[ i ][ j ] = dst[ i - 1 ][ j ]

                if dst[ i ][ j - 1 ] > dst[ i ][ j ]:
                    dst[ i ][ j ] = dst[ i ][ j - 1 ]
    
    return dst[ len( a ) ][ len( b ) ]

class File:
    def __init__( self, path, time ):
        # Set real and effective path names, maybe reapPath is really
        # an autosave file representing path.
        self.path = path
        self.time = time

        # Number of lines in the file (once we compute it)
        self.lineCount = None

    def __repr__(self):
        return '%s' % self.path



# This functionality needs to be split into an executable analyzer and
# a reusable classifier.  That will help with testing.
class Analyzer:
    def __init__( self, basePath ):
        # path to the top of the backup directory.
        self.basePath = basePath

        # Time of the latest current backup, in the local filesystem time.
        self.latestBackupTime = None

        # Strings to strip from the filename before we try to guess
        self.commonStrips = [ 'problem', 'prob', '_', '-' ]

        # filename extensions for code files.
        self.codeExtensions = [ 'cc', 'cpp', 'c', 'java' ]

        # map from problem ID to a list of plausible names.
        self.probNames = {}

        # Contest start time, in UTC seconds, likely to be overwritten by the
        # database later.
        self.contestStart = time.time()

        # Latest database version of every problem, so we can tell if
        # it's been edited recently.  A map from team and problem to a
        # database id and a timestamp (in utc seconds)
        self.dbTime = {}

        # map from team_id and path to the problem_id.  Multiple
        # files may map to the same problem, if the team is working on
        # multiple versions.
        self.fileOverride = {}

        # For each team, a list of team-specific strips from filenames.
        self.teamStrip = {}

    def loadConfiguration( self ):
        # Read the list of problem names
        self.probNames = {}

        cursor = dbConn.cursor()
        cursor.execute( "SELECT problem_id, name FROM problem_name" )
        row = cursor.fetchone()
        while ( row != None ):
            if ( row[ 0 ] in self.probNames ):
                self.probNames[ row[ 0 ] ].append( row[ 1 ].lower() )
            else:
                self.probNames[ row[ 0 ] ] = [ row[ 1 ].lower() ]

            row = cursor.fetchone()

        cursor.execute( "SELECT name, value FROM analyzer_parameters" )
        row = cursor.fetchone()
        while ( row != None ):
            if row[ 0 ] == "CONTEST_START":
                t = int( calendar.timegm( time.strptime( row[ 1 ], "%Y-%m-%d %H:%M:%S" ) ) )
                self.contestStart = t
                
            row = cursor.fetchone()

        # get latest edit times for every problem
        cursor.execute( "SELECT id, team_id, problem_id, modify_time_utc FROM edit_latest" )
        row = cursor.fetchone()
        while ( row != None ):
            t = int( calendar.timegm( row[ 3 ].timetuple() ) )
            self.dbTime[ ( row[ 1 ], row[ 2 ] ) ] = ( row[ 0 ], t )

            row = cursor.fetchone()
        
        # get any file overrides for team/problem
        cursor.execute( "SELECT team_id, problem_id, path FROM problem_file" )
        row = cursor.fetchone()
        while ( row != None ):
            self.fileOverride[ ( row[ 0 ], row[ 2 ] ) ] = row[ 1 ]
            row = cursor.fetchone()

        # load any team-specific strips.
        cursor.execute( "SELECT team_id, str FROM team_strip" )
        row = cursor.fetchone()
        while ( row != None ):
            if ( row[ 0 ] in self.teamStrip ):
                self.teamStrip[ row[ 0 ] ].append( row[ 1 ].lower() )
            else:
                self.teamStrip[ row[ 0 ] ] = [ row[ 1 ].lower() ]

            row = cursor.fetchone()
        
        cursor.close()

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

        # is it a vim vim autosave file
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
        if ( team in self.teamStrip ):
            for x in self.teamStrip[ team ]:
                strips.append( x )
        
        baseName, extension = os.path.splitext( fileName )
        extension = extension.lstrip( '.' )

        if not extension in self.codeExtensions:
            return None

        # Strip off extra words
        shortName = self.stripDecoration( strips, baseName )

        # Ordering here is a little bit important.  We look first
        # at the matches that are more confident.  Then, we look
        # at the ones that are less likely

        # Need to add a check for names like bbb.cpp or the same
        # thing in the path.  Some teams are doing this when they
        # start working on a second version of the problem.

        # First, consider just the filename against all problem names.
        for problem_id, names in self.probNames.iteritems():
            for name in names:
                # a.cpp -> a
                if shortName == name:
                    return problem_id
                

        # Then, start looking at the path.
        if len( dirName ) > 0:
            dirList = dirName.split( '/' )

            for dir in dirList:
                shortDirName = self.stripDecoration( strips, dir )
                        
                for problem_id, names in self.probNames.iteritems():
                    for name in names:
                        # b/sol.java -> b
                        if shortDirName == name:
                            return problem_id

                
        # Then, take matches that occur anywhere in the problem name
        for problem_id, names in self.probNames.iteritems():
            for name in names:
                # For longer names, we'll take the problem name anywhere in the
                # filename.  Really, this covers the next two checks for
                # all but the really short names.
                if ( len( name ) > 1 and name in baseName ):
                    return problem_id

                # b_2.c -> b
                if ( len( baseName ) > len( name ) and 
                     baseName.startswith( name ) and
                     not baseName[ len( name ) ].isalpha() ):
                    return problem_id

                # losning_b.c -> b
                if ( len( baseName ) > len( name ) and 
                     baseName.endswith( name ) and
                     not baseName[ -( len( name ) + 1  )].isalpha() ):
                    return problem_id
        

        # Then, look for path elements containing the name.
        if len( dirName ) > 0:
            dirList = dirName.split( '/' )

            for dir in dirList:
                shortDirName = self.stripDecoration( strips, dir )
                        
                for problem_id, names in self.probNames.iteritems():
                    for name in names:
                        # retry_b/sol.java -> b
                        if len( name ) > 1 and name in shortDirName:
                            return problem_id

        # Then, try an approximate match for the file name.
        for problem_id, names in self.probNames.iteritems():
            for name in names:
                # For longer names, we'll take the problem name anywhere in the
                # filename.  Really, this covers the next two checks for
                # all but the really short names.
                if ( len( name ) > 3 and 
                     editDist( name, shortName ) <= len( name ) * 0.25 ):
                    return problem_id


        # Then, look for approximate matches in any directory element.
        if len( dirName ) > 0:
            dirList = dirName.split( '/' )
            for dir in dirList:
                shortDirName = self.stripDecoration( strips, dir )
                for problem_id, names in self.probNames.iteritems():
                    for name in names:
                        if ( len( name ) > 3 and 
                             editDist( name, shortDirName ) <= len( name ) * 0.25 ):
                            return problem_id
        return None

    def guessPath( self, team, path ):
        """Testing interface for problem guessing"""
        self.loadConfiguration()
        return self.guessProblem( team, path )


    def checkActivity( self, bdir, tag ):
        """Scan the given backup dir and generate reports of the state of
        files believed to correspond to various problems in the problem set."""

        self.loadConfiguration()

        # map from team and problem to a file object.
        probMap = {}

        # Visit home directory for each team.
        tlist = sorted( glob.glob( bdir + '/team*' ) )
        for tdir in tlist:
            ( dirname, tname ) = os.path.split( tdir )
            team = int( tname.lstrip( 'team' ) )
            cmd = "find %s -type f" % tdir
            for f in os.popen( cmd ).readlines():
                f = f.rstrip( '\n' )
                fname = f[len(tdir) + 1:]
                fobj = File( fname, os.path.getmtime( f ) )

                prob = None;

                # see if there's an override for this file.
                if ( team, fname ) in self.fileOverride:
                    prob = self.fileOverride[ ( team, fname ) ]

                # If there's no override for this problem, guess
                # the problem ID.
                if prob == None:
                    prob = self.guessProblem( team, fobj.path )

                if prob != None and prob.lower() != "none":
                    # check common editor auto-saves, to see if there
                    # is a fresher modificationt ime.
                    autoTime = self.checkAutosaves( f );
                    if ( autoTime != None and autoTime > fobj.time ):
                        fobj.time = autoTime

                    timeTuple = None
                    if ( team, prob ) in self.dbTime:
                        timeTuple = self.dbTime[ ( team, prob ) ]
                    
                    # is this newer than our last known edit?
                    if ( timeTuple == None or timeTuple[ 1 ] + 15 < fobj.time ):
                        # count lines for this file, since it's worth something.
                        fobj.lineCount = self.countLines( f )
                        
                        # and store it in our map.
                        probMap[ ( team, prob ) ] = fobj
                        if timeTuple == None:
                            self.dbTime[ ( team, prob ) ] = ( None, fobj.time )
                        else:
                            self.dbTime[ ( team, prob ) ] = ( timeTuple[ 0 ], fobj.time )


        # Add db content for all files.
        cursor = dbConn.cursor()
        for k, v in probMap.iteritems():
            tstr = time.strftime( "%Y-%m-%d %H:%M:%S", time.gmtime( v.time ) )
            cmin = ( v.time - self.contestStart ) / 60
            update = "INSERT INTO edit_activity (team_id, problem_id, path, modify_time_utc, modify_time, line_count, git_tag, valid ) VALUES ( '%s', '%s', '%s', '%s', '%d', '%s', '%s', 1 )" % ( k[ 0 ], k[ 1 ], v.path, tstr, cmin, v.lineCount, tag )
                
            cursor.execute( update )

            print "( %s, %s ) -> %s %d" % ( k[ 0 ], k[ 1 ], v.path, v.time )

        for k, v in self.dbTime.iteritems():
            tstr = time.strftime( "%Y-%m-%d %H:%M:%S", time.gmtime( v[ 1 ] ) )

            if v[ 0 ] == None:
                update = "INSERT INTO edit_latest (team_id, problem_id, modify_time_utc ) VALUES ( '%s', '%s', '%s' )" % ( k[ 0 ], k[ 1 ], tstr )
                cursor.execute( update )
            else:
                update = "UPDATE edit_latest SET modify_time_utc='%s' WHERE id='%d'" % ( tstr, v[ 0 ] )
                cursor.execute( update )

        cursor.close()

    def reportUnclassified( self, bdir ):
        """Report all the source files that don't match any of our
        patterns"""

        self.loadConfiguration()

        # Visit home directory for each team.
        tlist = sorted( glob.glob( bdir + '/team*' ) )
        for tdir in tlist:
            ( dirname, tname ) = os.path.split( tdir )
            team = int( tname.lstrip( 'team' ) )
            cmd = "find %s -type f" % tdir
            for f in os.popen( cmd ).readlines():
                f = f.rstrip( '\n' )
                fname = f[len(tdir) + 1:]
                fobj = File( fname, os.path.getmtime( f ) )

                prob = None;

                # see if there's an override for this file.
                if ( team, fname ) in self.fileOverride:
                    prob = self.fileOverride[ ( team, fname ) ]

                # see if there's an override for this problem.
                if prob == None:
                    prob = self.guessProblem( team, fobj.path )

                ( dummy, extension ) = os.path.splitext( fobj.path )
                extension = extension.lstrip( '.' )
                if extension in self.codeExtensions:
                    if prob == None:
                        print "unknown -> %s" % ( f )
                    else:
                        print "%s -> %s" % ( prob, f )


    def updateAnalysis( self ):
        # Is current newer than the latest backup time?
        current = "%s/current" % self.basePath;
        if not os.path.exists( current ):
            return

        st = os.lstat( current )
        if ( self.latestBackupTime == None or
             st.st_mtime > self.latestBackupTime ):
            print "Generating Analysis Snapshot"
            self.checkActivity( current, "default-tag" )
            self.latestBackupTime = st.st_mtime


    def periodicCheck( self ):
        while ( True ):
            # Check once per second for new backup directories.
            time.sleep( 1 )
            analyzer.updateAnalysis()

if __name__ == '__main__':
    analyzer = Analyzer( BACKUP_TOP )

    tag = "default-tag";
    if len( sys.argv ) > 1:
        tag = sys.argv[ 1 ]
    
    analyzer.checkActivity( BACKUP_TOP, tag )
    
