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
( 'a', 'a' ),
( 'a', 'asteroid' ),
( 'a', 'rangers' ),
( 'a', 'mst' ),
( 'b', 'b' ),
( 'b', 'curvy' ),
( 'b', 'bottle' ),
( 'c', 'c' ),
( 'c', 'bustour' ),
( 'c', 'bus' ),
( 'c', 'tour' ),
( 'd', 'd' ),
( 'd', 'fibonacci' ),
( 'd', 'fib' ),
( 'd', 'words' ),
( 'e', 'e' ),
( 'e', 'infiltrat' ),
( 'f', 'f' ),
( 'f', 'keys' ),
( 'g', 'g' ),
( 'g', 'minflow' ),
( 'g', 'minimum' ),
( 'g', 'cost' ),
( 'g', 'flow' ),
( 'h', 'h' ),
( 'h', 'room' ),
( 'h', 'service' ),
( 'i', 'i' ),
( 'i', 'safe' ),
( 'i', 'bet' ),
( 'i', 'mirror' ),
( 'j', 'j' ),
( 'j', 'shortest' ),
( 'j', 'short' ),
( 'j', 'flight' ),
( 'j', 'path' ),
( 'k', 'k' ),
( 'k', 'stacking' ),
( 'k', 'plates' ),
( 'l', 'l' ),
( 'l', 'takeover' ),
( 'l', 'wars' )
    """ )
print "Problem name rows inserted: %d" % cursor.rowcount

cursor.execute( """
    INSERT INTO analyzer_parameters (name, value)
    VALUES
      ( 'CONTEST_START', '2012-05-17 8:00:00' )
    """ )
print "Parameter rows inserted: %d" % cursor.rowcount

cursor.close()
dbConn.close()
