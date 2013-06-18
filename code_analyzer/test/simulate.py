#!/usr/bin/python
# Simulate problem activity.

import os, sys, inspect

# Include the source file directory in the module search path
cmd_folder = os.path.split(os.path.abspath( inspect.getfile( inspect.currentframe())))[0]
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

# Include the source file parent directory in the module search path
cmd_folder = os.path.split( cmd_folder )[ 0 ]
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from RandomSource import RandomSource
from common import dbConn, BACKUP_TOP
from datetime import datetime
import time
import shutil
import random

class Simulator:
    def __init__( self, basePath ):
        # top-level path for simulated backup content
        self.basePath = basePath

        # map from team ID and problem id to a current file location.
        self.probMap = {}

        # list of all team ids.  we should get this from the database.
        self.teamList = range( 1, 106 )

        self.randomSource = RandomSource( dbConn )
        
    def run( self ):
        current = "%s/current" % self.basePath

        while ( True ):
            now = datetime.now().timetuple()
        
            scratch = "%s/%04d%02d%02d/%02d/%02d%02d" % ( self.basePath, now[ 0 ], now[ 1 ], now[ 2 ], now[ 3 ], now[ 4 ], now[ 5 ] )

            if os.path.exists( current ):
                shutil.copytree( current, scratch )
            else:
                if not os.path.exists( scratch ):
                    os.makedirs( scratch )

            # Edit some old problems.
            count = random.randrange( 1, 11 )
            for i in range( count ):
                klist = self.probMap.keys()
                if len( klist ) > 0:
                    # Sleep, to give some variability in modification times
                    time.sleep( random.randrange( 1, 2 ) )

                    ( team, problem ) = random.choice( klist )
                    path = self.probMap[ ( team, problem ) ]

                    # Make the directories, then add on the filename.
                    prefix = "%s/team%d/" % ( scratch, team )
                    pfxLen = len( prefix )
                    path = "%s%s" % ( prefix, path )

                    # Split into directory and file.
                    ( dirName, fileName ) = os.path.split( path )
                    
                    # Choose whether to update the file, or one of the autosaves.
                    style = random.choice( [ 'file',
                                             '#file#',
                                             '.file.swp' ] )

                    if style == 'file':
                        self.randomSource.appendSourceFile( path )
                        print "Appending (%3d %s) : %s" % (team, problem, path[pfxLen:] )
                    elif style == '#file#':
                        path = "%s/#%s#" % ( dirName, fileName )
                        print "Touching (%3d %s) : %s" % (team, problem, path[pfxLen:] )
                        self.randomSource.writeSourceFile( path )
                    elif style == '.file.swp':
                        path = "%s/.%s.swp" % ( dirName, fileName )
                        print "Touching (%3d %s) : %s" % (team, problem, path[pfxLen:] )
                        self.randomSource.writeSourceFile( path )
                    
            count = random.randrange( 1, 11 )
            for i in range( count ):
                # Sleep, to give some variability in modification times
                time.sleep( random.randrange( 1, 2 ) )

                team = random.choice( self.teamList )
                ( path, fileName, problem ) = self.randomSource.chooseRandomSourceFile()
                
                # Make the directories, then add on the filename.
                scratchPath = "%s/team%d/%s" % ( scratch, team, path )
                if not os.path.exists( scratchPath ):
                    os.makedirs( scratchPath )
                scratchPath = "%s%s" % ( scratchPath, fileName )

                self.randomSource.writeSourceFile( scratchPath )

                print "Creating (%3d %s) : %s%s" % (team, problem, path, fileName )
                self.probMap[ ( team, problem ) ] = "%s%s" % ( path, fileName )

            # create the symbolic link for it, just like we would expect.
            if os.path.exists( current ):
                os.remove( current )
            os.symlink( scratch, current )
            
            # time.sleep( 30 + random.randrange( 0, 30 ) )
            print "Continue?"
            sys.stdin.readline()



if __name__ == '__main__':
    sim = Simulator( BACKUP_TOP )
    sim.run()

dbConn.close()
