#!/usr/bin/python

# Add a string to a particular tream's list of strips.

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0])
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn, BACKUP_TOP
from analyzer import Analyzer

def usage():
    print("Usage: teamStrip.py <team_id> <string>")
    exit( 1 )

if len( sys.argv ) != 3:
    usage()

team = int( sys.argv[ 1 ] )
str = sys.argv[ 2 ]

# Make sure this is a valid team id.
analyzer = Analyzer( BACKUP_TOP )
analyzer.loadConfiguration()

if ( team < 1 or team > analyzer.lastTeam ):
    print("Invalid teeam id: %d" % (team))
    usage()

cursor = dbConn.cursor()

cmd = "insert into team_strips ( team_id, str ) values ( '%s', '%s' )" % ( team, str )
cursor.execute( cmd )

cursor.close()
dbConn.close()
