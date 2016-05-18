#!/usr/bin/python

import os, shutil, inspect, subprocess, yaml, tempfile, time, urllib, sys
from common import dbConn, config
from datetime import datetime, timedelta
import httplib2
import re

# Just an object wrapping up the githomes functioanlity.
class GitHomes:
    def __init__( self ):
        # Find the top-level directory for the analyzer
        self.analystTop = os.path.dirname(os.path.dirname(os.path.abspath( inspect.getfile( inspect.currentframe()))))

        # remember where we were when the program was run.  We keep returning to here
        # after temporarily working in the gitdir.
        self.origin = os.getcwd()

        # interval for updating team backups.
        self.interval = config[ "teambackup" ][ "interval" ]

        # location for the repository of team backup snapshots
        self.gitdir = config[ "teambackup" ][ "gitdir" ]

        # location where to rsync backups from when using 'copy' method
        self.backupdir = config[ "teambackup" ][ "backupdir" ]

        self.pullmethod = config[ "teambackup" ][ "method" ]

        # Method to retrieve backups.
        # index of the last team in the competition.
        self.lastTeam = config[ "teambackup" ][ "lastTeam" ]

        # CDS config: base URL and credentials
        self.CDSRoot = config[ "CDS" ][ "baseurl" ]
        self.CDSUser = config[ "CDS" ][ "user" ]
        self.CDSPass = config[ "CDS" ][ "pass" ]

        # read additional configuration parameters, depending on method.
        if self.pullmethod == 'CDS':
            self.initCDS()
        elif self.pullmethod == 'simulate':
            self.initSimulation()

        # files listing modification times, etc for the contents of the repository.
        self.LISTINGFULL = "listing_full.txt"
        self.LISTINGSHORT = "listing.txt"

    def initSimulation( self ):
        """Initialization that just needs to be done for simulation runs"""

        # location the source repo lives for the 'simulate' method.
        self.sourceRepo = config[ "teambackup" ][ "sourceRepo" ]
        self.simulationRate = 1
        if "simulationRate" in config[ "teambackup" ]:
            self.simulationRate = config[ "teambackup" ][ "simulationRate" ]

        os.chdir( self.sourceRepo )

        # figure out the first real, contest-time commit to this
        # repo, I'm thinking it's the second commit, since (at
        # least for repos made by this script), the first one will
        # be the init when the repo is first created, maybe well
        # before the contest starts.
        t = tempfile.TemporaryFile()
        subprocess.call( [ "git", "log", "--format=%ai", "--reverse" ], stdout=t )
        t.seek( 0 );
        lines = t.readlines()
        t.close();

        if len( lines ) < 2:
            print("Error: Can't find start time from sourceRepo")
            exit(1)
            
        # Ugly, but I can't get strptime() to recognize %z
        match = re.compile( '(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) ([-+]\d{2}\d{2})' ).match( lines[ 1 ] )
        if not match:
            print("Error: can't parse simulation start time")
            exit(1)
        
        # current simulation time, gets changed incrementally as we run the simulation.
        self.sourceRepoStart = datetime.strptime( match.group( 1 ), '%Y-%m-%d %H:%M:%S' )
        # Really, we should put this in the previous datetime object.
        self.sourceRepoTzone = match.group( 2 )

        # remember the wall clock time when the simulation started, so we can play back at
        # an adjusted speed.
        self.simulationStart = datetime.now()

        os.chdir( self.origin )

    def initCDS( self ):
        """Initialization that just needs to be done for runs with backups from the CDS.
        This is getting kind of ugly."""

        # figure out a start time in the format we will get from the CDS.
        cursor = dbConn.cursor()
        cursor.execute( "SELECT UNIX_TIMESTAMP(start_time) FROM contests ORDER BY start_time DESC LIMIT 1" )
        row = cursor.fetchone()
        if ( row == None ):
            print("Error: no contest found in the database.")
            exit(1)

#        startTime = time.strftime( "%a, %d %b %Y %H:%M:%S GMT", time.gmtime(row[0]) )
        self.teamLastModified = {};
        for teamIdx in range( 1, self.lastTeam + 1 ):
            self.teamLastModified[ teamIdx ] = 0;


    # The copy of team directories obtained from the CDS preserves modification
    # times, but git doesn't.  Here, we generate two listing files (long and short)
    # that hold this information and then get committed along with the latest
    # versions of all the other files.
    def genListing( self ):
        tmpFull = tempfile.TemporaryFile()
        tmpShort = tempfile.TemporaryFile()

        format = '%M %10s  %TY-%Tm-%Td %TT  %p\n'

        if os.path.exists( self.LISTINGFULL ):
            os.unlink( self.LISTINGFULL )
        if os.path.exists( self.LISTINGSHORT ):
            os.unlink( self.LISTINGSHORT )

        subprocess.call( [ "find", ".", "-mindepth", "1", "-name", ".git", "-prune",
                           "-o", "-type", "f", "-printf", format ], stdout=tmpFull )
        subprocess.call( [ "find", ".", "-mindepth", "1", "-name", ".*", "-prune",
                           "-o", "-type", "f", "-printf", format ], stdout=tmpShort )

        tmpFull.seek( 0 )
        f = open( self.LISTINGFULL, "w" )
        subprocess.call( [ "sed", "-r", "s/\\.[0-9]{10}  /  /" ], stdin=tmpFull, stdout=f )
        tmpFull.close()
        f.close()

        tmpShort.seek( 0 )
        f = open( self.LISTINGSHORT, "w" );
        subprocess.call( [ "sed", "-r", "s/\\.[0-9]{10}  /  /" ], stdin=tmpShort, stdout=f )
        tmpShort.close()
        f.close()

    # Prepare the repository.
    def prep( self ):
        # Make sure we have a directory for the repository.
        if not os.path.exists( self.gitdir ):
            os.makedirs( self.gitdir )

        os.chdir( self.gitdir )

        # Make sure that gitweb (webserver) has access.
        subprocess.call( [ "chmod", "a+rx", "." ] )

        # Create an initial repository witht just the listings in it.
        subprocess.call( [ "git", "init", "-q", "." ] )
        self.genListing()
        subprocess.call( [ "git", "add", "-f", "--all", "--ignore-errors", "." ] )

        # Set git user name/email to suppress warnings.
        if subprocess.call( [ "git", "config", "--get", "user.name" ] ) != 0 or \
           subprocess.call( [ "git", "config", "--get", "user.email" ] ) != 0:
            subprocess.call( [ "git", "config", "user.name", "ICPC Analytics" ] )
            subprocess.call( [ "git", "config", "user.email", "analyst@example.com" ] )

        # Set gitweb description of this repository.
        os.system("echo 'Homedirectories for ICPC analytics' > .git/description")

        subprocess.call( [ "git", "commit", "-q", "--all", "--allow-empty", "-m",
                           "Initialization of ICPC homedirs at '%s'." % self.gitdir ] )
        subprocess.call( [ "git", "gc", "--aggressive" ] )

        os.chdir( self.origin )

    def pullBackupsCopy( self ):
        os.chdir( self.gitdir )

        # Make sure that source path ends with a '/' to sync its contents,
        # but not the directory itself into the target directory.
        os.system('rsync -a --delete --exclude=.git %s/ .' % self.backupdir)

        # Make sure that permissions are OK for apache/gitweb.
        os.system('chmod 755 %s' % self.gitdir)

        os.chdir( self.origin )

    def pullBackupsSimulate( self, when ):
        """Pass a datetime object for when the repo should be checked out.  Probably computed from
        the start time for the repo"""

        os.chdir( self.sourceRepo )

        timestr = when.strftime( '%Y-%m-%d %H:%M:%S' ) + " " + self.sourceRepoTzone
        print 'git checkout $(git rev-list -n 1 --before="%s" master)' % timestr;
        os.system('git checkout $(git rev-list -n 1 --before="%s" master)' % timestr )

        # Copy everything over to the repo we're checking things into.
        os.system('rsync -a --delete --exclude=.git . %s/' % self.gitdir )

        os.chdir( self.origin )

    def pullBackupsCDS( self ):
        os.chdir( self.gitdir )

        # right now, we're making a new cache directory every time
        # we pull down a batch of team backups. This doesn't seem to
        # make sense.  I'm not sure we want a cache, and, if we do, 
        # don't we want it to persist across all connections?
        d = tempfile.mkdtemp(prefix='githomes')
        # is it a problem to make this object over and over?
        h = httplib2.Http(os.path.join(d,".cache"))
        h.add_credentials( self.CDSUser, self.CDSPass )
        h.disable_ssl_certificate_validation=True

        for teamIdx in range( 1, self.lastTeam + 1 ):
            str = "Polling %s/backups/%d... " % ( self.CDSRoot, teamIdx )
            sys.stdout.write(str)

            #if_modified_since_header = "If-Modified-Since: %s" % (self.teamLastModified[ teamIdx ])
            # pull down the latest backup archive, and unpack it.
            (responseHeader, result) = h.request( "%s/backups/%d" % ( self.CDSRoot, teamIdx ), "GET", headers={"If-Modified-Since" : self.teamLastModified[ teamIdx ]} )
            print(responseHeader)
            #print(responseHeader.status)
            #print(responseHeader["status"])

            if responseHeader["status"] == "200":
                sys.stdout.write("updated, commit to git... ")

                self.teamLastModified[ teamIdx ] = responseHeader["last-modified"]
                f = tempfile.NamedTemporaryFile( delete=False )
                f.write( result )
                f.close()
                teamDir = "team%d" % teamIdx
                if not os.path.exists( teamDir ):
                    os.makedirs( teamDir )
                subprocess.call( [ "tar", "xf", f.name, "--exclude-vcs", "-C", teamDir ] )
                os.unlink( f.name )

                print("done.")
            elif responseHeader["status"] == "304":
                print("no change, done.")
            else:
                print("error %s" % responseHeader)

        shutil.rmtree(d)
        os.chdir( self.origin )

    # Repeatedly download and commit fresh team backups.
    def poll( self ):
        if not os.path.exists( self.gitdir + "/.git" ):
            print "Repository doesn't exist.  Did you run prephomes first?"
            exit( 1 )
        
        while ( True ):
            # Track how long it takes us to process files.
            beforeTime = datetime.now()

            if self.pullmethod == 'CDS':
                self.pullBackupsCDS()
            elif self.pullmethod == 'copy':
                self.pullBackupsCopy()
            elif self.pullmethod == 'simulate':
                elapsed = ( beforeTime - self.simulationStart ).total_seconds()
                delta = timedelta( seconds = ( elapsed * self.simulationRate ) )
                self.pullBackupsSimulate( self.sourceRepoStart + delta )
            else:
                print "Unknown method '" + self.pullmethod + "' to acquire backups."
                exit( 1 )

            os.chdir( self.gitdir )

            self.genListing()

            tag = datetime.now().strftime( "Tag_%H_%M_%S" )

            # First do explicit 'git add' to include new files too
            subprocess.call( [ "git", "add", "-f", "--all", "--ignore-errors", "." ] )
            subprocess.call( [ "git", "commit", "-q", "--all", "--allow-empty",  
                               "-m", "Autocommit" ] )
            subprocess.call( [ "git", "tag", "-f", "-m", "", tag, "HEAD" ],
                             stdout=None, stderr=None )
            subprocess.call( [ "git", "gc", "--auto", "--quiet" ] )

            os.chdir( self.origin )
                
            print "Checking in %s at %s" % ( tag, datetime.now().strftime( "%a %b %d %H:%M:%S %Y") )
            subprocess.call( [ "python", self.analystTop + "/code_analyzer/analyzer.py", tag ] )

            # Rest up to the end of the minute (or whatever the interval is), or zero if it's too late.
            afterTime = datetime.now()

            delay = ( afterTime - beforeTime ).total_seconds()
            sleeptime = self.interval - delay
            if sleeptime < 0:
                sleeptime = 0;

            print "Sleeping: until next pull %f" % ( sleeptime )
            time.sleep( sleeptime )

if __name__ == '__main__':
    gitHomes = GitHomes()
    gitHomes.poll()
