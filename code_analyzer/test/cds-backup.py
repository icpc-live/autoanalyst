#!/usr/bin/python
from flask import Flask, jsonify, abort, request, Response
from datetime import datetime
from functools import wraps
import time;
import random;
import os;
import subprocess;
import hashlib;

app = Flask(__name__)

# Last time when we last updated everything.
lastUpdate = 0; # to force the initial update

# Where we build simulated team directories.
TEAM_ROOT = "/tmp/backups/";

# Line counter for writing files.
lineCounter = 0;

pathList = [
    "team1/self.c",
    "team1/gettor.cpp",
    "team2/tiles.java",
    "team2/pirate.java",
    "team3/power/solution.cpp",
    "team3/tree/solution.cpp",
    "team3/congest.cpp",
    "team4/A/A.java",
    "team4/C/C.java",
    "team4/C/dummy.txt",
    "team5/pollution.c",
    "team5/factors.c",
    "team5/assembly.c",
];

# List of team directories.
teamList = [
    "team1",
    "team2",
    "team3",
    "team4",
    "team5",
];

# Tentative modification times for team directories.
teamModtime = [ "Mon, 1 Jan 2014 00:00:00 GMT" ] * len( teamList );

def writeFiles():
    global lineCounter;
    for i in range( 0, 3 ):
        # Choose a file to append to
        fname = random.choice( pathList );
        path = "%s%s" % ( TEAM_ROOT, fname );

        # Make sure we have a directory for this file.
        dname = os.path.dirname( path )
        if not os.path.exists( dname ):
            os.makedirs( dname )

        # append some meaningless text.
        f = open( path, 'a');
        f.write( "  -- line %d --\n" % lineCounter );
        lineCounter += 1;
        f.close();

    origin = os.getcwd()
    os.chdir( TEAM_ROOT )
    for i in range( 0, len( teamList ) ):
        teamDir = teamList[ i ]
        if not os.path.exists( teamDir ):
            os.makedirs( teamDir )
        
        tgzFile = "%s.tar.gz" % teamDir
        subprocess.call( [ "tar",  "-C", teamDir, "-czf", tgzFile, "." ] )
        
        # this part isn't really right.  This team's files may not have changed
        # since the last update.  Need to be more selective in how we build the
        # archive.
        teamModtime[ i ] = datetime.utcnow().strftime( "%a, %d %b %Y %H:%M:%S GMT" )

    os.chdir( origin )

def updateSimulation():
    global lastUpdate;
    # Only update every 30 seconds.
    if time.time() - lastUpdate < 30:
        return;
    print "Update: %s" % time.time();
    writeFiles();
    lastUpdate = time.time();

#############################################

def check_auth(username, password):
    """This function is called to check if a username /
    password combination is valid.
    """
    return username == 'user' and password == 'THISISNOTAPASSWORD'

def authenticate():
    """Sends a 401 response that enables basic auth"""
    return Response(
    'Could not verify your access level for that URL.\n'
    'You have to login with proper credentials', 401,
    {'WWW-Authenticate': 'Basic realm="Login Required"'})

def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not auth or not check_auth(auth.username, auth.password):
            return authenticate()
        return f(*args, **kwargs)
    return decorated

#############################################

@app.route('/backups/<int:teamIdx>', methods = ['GET'])
@requires_auth
def getBackup(teamIdx):
    updateSimulation()
    name = "team%d" % teamIdx
    for i in range( 0, len( teamList ) ):
        if teamList[ i ] == name:
            tgzFile = "%s%s.tar.gz" % ( TEAM_ROOT, teamList[ i ] );
            f = open( tgzFile );
            s = f.read();
            f.close();

            return Response( s, 200, {'Last-Modified': teamModtime[ i ] } )
    abort(404)

if __name__ == '__main__':
    updateSimulation();
    app.debug = True
    app.run()
