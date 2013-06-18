#!/usr/bin/python

# force a particular file to be a particular problem for a particular team.

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0])
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn, BACKUP_TOP

import re

def usage():
    print "Usage: ignoreFile.py <abs_path>"
    exit( 1 )

if len( sys.argv ) != 2:
    usage()

prefix = "%s/team" % BACKUP_TOP
if not sys.argv[ 1 ].startswith( prefix ):
    print "Bad path format"
    usage()

path = sys.argv[ 1 ]

# Strip off the front, and extract the team id.
path = path[len(prefix):]
mg = re.match( "([0-9]+)", path )
if ( mg == None ):
    print "Bad path format, no team id"
    usage()

team = mg.group( 1 )

path = path[ len(team)+1:]

cursor = dbConn.cursor()

# See if there is already an entry for this file.
cmd = "SELECT team_id FROM problem_file WHERE path='%s' AND team_id='%s'" % ( path, team )
cursor.execute( cmd )

# Just see if it's arealdy there.  There should be a better way to do this.
if cursor.fetchone() == None:
    cmd = "insert into problem_file ( team_id, problem_id, path ) values ( '%s', 'none', '%s' )" % ( team, path )
    cursor.execute( cmd )
else:
    cmd = "UPDATE problem_file SET problem_id='none' WHERE team_id='%s' AND path='%s'" % ( team, path )
    cursor.execute( cmd )

cmd = "UPDATE edit_activity SET valid='0' WHERE team_id='%s' AND path='%s'" % ( team, path )
cursor.execute( cmd )

cursor.close()
