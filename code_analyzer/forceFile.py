#!/usr/bin/python

# force a particular file to be a particular problem for a particular team.

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0])
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import config, dbConn, BACKUP_TOP
from analyzer import Analyzer

import re

def usage():
    print "Usage: forceFile.py <abs_path> <problem_id>"
    exit( 1 )

if len( sys.argv ) != 3:
    usage()

path = sys.argv[ 1 ]
prob = sys.argv[ 2 ].upper()

prefix = "%s/team" % BACKUP_TOP
if not path.startswith( prefix ):
    print "Bad path format"
    usage()

# Strip off the front, and extract the team id.
path = path[len(prefix):]
mg = re.match( "^([0-9]+)", path )
if ( mg == None ):
    print "Bad path format, no team id"
    usage()

team = mg.group( 1 )

path = path[ len(team)+1:]

cursor = dbConn.cursor()

# Make sure this is a legal problem_id.
if prob not in config['problems']:
    print "Bad problem id: %s" % prob
    usage()

# Just to get the extension map.
analyzer = Analyzer( BACKUP_TOP )

# See if there is already an entry for this file.
cmd = "SELECT team_id FROM file_to_problem WHERE path='%s' AND team_id='%s'" % ( path, team )
cursor.execute( cmd )

# Just see if it's arealdy there.  There should be a better way to do this.
if cursor.fetchone() == None:
    ( dummy, extension ) = os.path.splitext( path )
    extension = extension.lstrip( '.' )
    lang = 'none'
    if extension in analyzer.extensionMap:
        lang = analyzer.extensionMap[ extension ]

    cmd = "insert into file_to_problem ( team_id, path, problem_id, lang_id, override ) values ( '%s', '%s', '%s', '%s', 1 )" % ( team, path, prob, lang )
    cursor.execute( cmd )
else:
    cmd = "UPDATE file_to_problem SET problem_id='%s',override='1' WHERE path='%s' AND team_id='%s'" % ( prob, path, team )
    cursor.execute( cmd )


#
# Note that the script may still find other files it thinks go with this problem.  That's
# probably OK.  The team may have multiple files that all represent work on a single problem.
# if there are other files that should be ignored, they should be marked as such using
# ignoreFile.py.
#

cursor.close()
dbConn.close()
