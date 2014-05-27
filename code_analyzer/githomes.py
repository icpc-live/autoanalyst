#!/usr/bin/python

import os, inspect, subprocess, yaml, tempfile, time, urllib, sys
from datetime import datetime
import httplib2

# Just an object wrapping up the githomes functioanlity.
class GitHomes:
    def __init__( self ):
        # Find the top-level directory for the analyzer
        self.analystTop = os.path.dirname(os.path.dirname(os.path.abspath( inspect.getfile( inspect.currentframe()))))
        
        # Use top-level directory to load the config file.
        f = open( self.analystTop + "/config.yaml" )
        config = yaml.load( f )
        f.close()

        # interval for updating team backups.
        self.interval = config[ "teambackup" ][ "interval" ]

        # location for the repository of team backup snapshots
        self.gitdir = config[ "teambackup" ][ "gitdir" ]

        # index of the last team in the competition.
        self.lastTeam = config[ "teambackup" ][ "lastTeam" ]

        # base URL for the CDS
        self.CDSRoot = config[ "CDSRoot" ]

        # user and password for CDS access
        self.CDSUser = config[ "teambackup" ][ "CDSUser" ]
        self.CDSPass = config[ "teambackup" ][ "CDSPass" ]

        # figure out a start time in the format we will get from the CDS.
        startTime = time.strptime( config['analyzer']['contestStart'], "%Y-%m-%d %H:%M:%S" )
        startTime = time.strftime( "%a, %d %b %Y %H:%M:%S GMT", startTime )
        self.teamLastModified = {};
        for teamIdx in range( 1, self.lastTeam + 1 ):
            self.teamLastModified[ teamIdx ] = startTime;

        # files listing modification times, etc for the contents of the repository.
        self.LISTINGFULL = "listing_full.txt"
        self.LISTINGSHORT = "listing.txt"

        # remember where we were when the program was run.  We keep returning to here
        # after temporarily working in the gitdir.
        self.origin = os.getcwd()

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
        subprocess.call( [ "git", "add", "-f", "--all", 
                           "--ignore-errors", "." ] )
        subprocess.call( [ "git", "commit", "-q", "--all", "--allow-empty", "-m",
                           "Initialization of ICPC homedirs at '%s'." % self.gitdir ] )
        subprocess.call( [ "git", "gc", "--aggressive" ] )

        os.chdir( self.origin )

    def pullBackups( self ):
        os.chdir( self.gitdir )

        d = tempfile.mkdtemp(prefix='githomes')
        # is it a problem to make this object over and over?
        h = httplib2.Http(os.path.join(d,".cache"))
        h.add_credentials( self.CDSUser, self.CDSPass )
        h.disable_ssl_certificate_validation=True

        for teamIdx in range( 1, self.lastTeam + 1 ):
            str = "Polling %sbackups/%d... " % ( self.CDSRoot, teamIdx )
            sys.stdout.write(str)

            #if_modified_since_header = "If-Modified-Since: %s" % (self.teamLastModified[ teamIdx ])
            # pull down the latest backup archive, and unpack it.
            (responseHeader, result) = h.request( "%sbackups/%d" % ( self.CDSRoot, teamIdx ), "GET", headers={"If-Modified-Since" : self.teamLastModified[ teamIdx ]} )
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

        os.chdir( self.origin )

    # Repeatedly download and commit fresh team backups.
    def poll( self ):
        if not os.path.exists( self.gitdir + "/.git" ):
            print "Repository doesn't exist.  Did you run prephomes first?"
            exit( 1 )
        
        while ( True ):
            self.pullBackups()

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
            
            time.sleep( self.interval )

if __name__ == '__main__':
    gitHomes = GitHomes()
    gitHomes.poll()
