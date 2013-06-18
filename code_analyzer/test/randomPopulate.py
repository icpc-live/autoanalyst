#!/usr/bin/python
# Fill the database with random problems and modification times.  To give something
# for the front-end to work with.
# This assumes the database was previously empty.  It doesn't check to see if anything
# is already there, so it may introduce duplicates if run on a database that already
# has something in it.

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
import random

class RandomPopulate:
    def __init__( self, basePath ):
        # top-level path for simulated backup content
        self.basePath = basePath

        # map from team ID and problem id to a current file location.
        self.probMap = {}

        # list of all team ids.  we should get this from the database.
        self.teamList = range( 1, 106 )

        self.randomSource = RandomSource( dbConn )
        
    def run( self ):
        count = random.randrange( 30, 40 )
        for i in range( count ):
            team = random.choice( self.teamList )
            ( path, fileName, problem ) = self.randomSource.chooseRandomSourceFile()
                
            self.probMap[ ( team, problem ) ] = "%s%s" % ( path, fileName )

        # Dump these problems into the database
        cursor = dbConn.cursor()
        for k, v in self.probMap.iteritems():
            print "( %d, %s ) -> %s" % ( k[ 0 ], k[ 1 ], v )

            # invent a modification time.
            now = time.time()
            mtime = now - random.randrange( 30, 3600 )
            tstr = time.strftime( "%Y-%m-%d %H:%M:%S", time.localtime( mtime ) )

            # invent a random file length
            flen = random.randrange( 30, 250 )

            update = "INSERT INTO edit_activity (team_id, problem_id, path, modify_time, line_count) VALUES ( '%s', '%s', '%s', '%s', '%s' )" % ( k[ 0 ], k[ 1 ], v, tstr, flen )
            cursor.execute( update )

        cursor.close()


if __name__ == '__main__':
    pop = RandomPopulate( BACKUP_TOP )
    pop.run()

dbConn.close()
