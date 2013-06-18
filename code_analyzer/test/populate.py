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
      ( 'a', 'matrix' ),
      ( 'a', 'inverse' ),
      ( 'b', 'b' ),
      ( 'b', 'repeat' ),
      ( 'c', 'c' ),
      ( 'c', 'invert' ),
      ( 'd', 'd' ),
      ( 'd', 'path' )
    """ )
print "Problem name rows inserted: %d" % cursor.rowcount

cursor.execute( """
    INSERT INTO analyzer_parameters (name, value)
    VALUES
      ( 'CONTEST_START', '2012-05-15 7:30:00' )
    """ )
print "Parameter rows inserted: %d" % cursor.rowcount

cursor.close()
dbConn.close()
