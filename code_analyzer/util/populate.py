#!/usr/bin/python
# Populate database with example problems, patterns, etc.

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.split(os.path.split(os.path.abspath( inspect.getfile( inspect.currentframe())))[0])[0]
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn

cursor = dbConn.cursor()
cursor.execute( """
    INSERT INTO problem_name (problem_id, name)
    VALUES
( 'A', 'Space Rangers' ),
( 'B', 'Curvy Bottles' ),
( 'C', 'Bus Tour' ),
( 'D', 'Fibonacci' ),
( 'E', 'Infiltrate' ),
( 'F', 'Keys' ),
( 'G', 'Minflow' ),
( 'H', 'Room Service' ),
( 'I', 'Safe Bet' ),
( 'J', 'Shortest Flight' ),
( 'K', 'Stacking Plates' ),
( 'L', 'Takeover' )
    """ )
print "Problem name rows inserted: %d" % cursor.rowcount

cursor = dbConn.cursor()
cursor.execute( """
    INSERT INTO problem_keywords (problem_id, keyword)
    VALUES
( 'A', 'asteroid' ),
( 'A', 'rangers' ),
( 'A', 'mst' ),
( 'B', 'curvy' ),
( 'B', 'bottle' ),
( 'C', 'bustour' ),
( 'C', 'bus' ),
( 'C', 'tour' ),
( 'D', 'fibonacci' ),
( 'D', 'fib' ),
( 'D', 'words' ),
( 'E', 'infiltrat' ),
( 'F', 'keys' ),
( 'G', 'minflow' ),
( 'G', 'minimum' ),
( 'G', 'cost' ),
( 'G', 'flow' ),
( 'H', 'room' ),
( 'H', 'service' ),
( 'I', 'safe' ),
( 'I', 'bet' ),
( 'I', 'mirror' ),
( 'J', 'shortest' ),
( 'J', 'short' ),
( 'J', 'flight' ),
( 'J', 'path' ),
( 'K', 'stacking' ),
( 'K', 'plates' ),
( 'L', 'takeover' ),
( 'L', 'wars' )
    """ )
print "Problem keywords rows inserted: %d" % cursor.rowcount

cursor.execute( """
    INSERT INTO analyzer_parameters (name, value)
    VALUES
      ( 'CONTEST_START', '2013-06-30 8:00:00' )
    """ )
print "Parameter rows inserted: %d" % cursor.rowcount

cursor.close()
dbConn.close()
