
#!/usr/bin/python

# Read configuration information from the CDS, and update what's in our database.
# This is was originally created under the code analyzer, but it probably doesn't belong
# there.

import os, shutil, inspect, subprocess, yaml, tempfile, time, urllib, sys
from common import dbConn, config
from datetime import datetime, timedelta
import httplib2
import re
import json

# Just an object wrappin
class ConfigImporter:
    def __init__( self ):
        # CDS config: base URL and credentials
        self.CDSRoot = config[ "CDS" ][ "baseurl" ]
        self.CDSUser = config[ "CDS" ][ "user" ]
        self.CDSPass = config[ "CDS" ][ "pass" ]

    def cleanTables(self):
        cursor = dbConn.cursor()
        cursor.execute("DELETE FROM teams")
        cursor.execute("DELETE FROM problems")

    def performRequest(self, path, callback):
        d = tempfile.mkdtemp(prefix='import-config')
        h = httplib2.Http(os.path.join(d,".cache"))
        h.add_credentials( self.CDSUser, self.CDSPass )
        h.disable_ssl_certificate_validation=True
        (responseHeader, result) = h.request( "%s/contest/%s" % ( self.CDSRoot, path ), "GET" )
        if responseHeader["status"] == "200":
            for line in result.split('\n'):
                if line:
		    callback(json.loads(line))
        else:
            print("error %s" % responseHeader)

        shutil.rmtree(d)

    def importConfig( self, info ):

        t = info[ "info" ][ "start-time" ]

        match = re.compile( '(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})\.\d*([-+]\d{2})' ).match( t )
        if not match:
            print("Error: can't parse start time")
            exit(1)

        # here, we're truncating seconds, but we could round.
        dt = datetime.strptime( match.group( 1 ), "%Y-%m-%dT%H:%M:%S" )

        # add in the UTC Offset (there should be an easier way to do this)
        start_time = (dt - datetime(1970,1,1)).total_seconds()
        start_time = start_time - int( match.group( 2 ) ) * 3600

        # Parse contest duration
        match = re.compile( '(\d{1,}):(\d{2}):(\d{2})' ).match( info[ "info" ][ "duration" ] )
        if not match:
            print("Error: can't parse start duration")
            exit(1)

        duration = int( match.group( 1 ) ) * 3600 + int( match.group( 3 ) ) * 60 + int( match.group( 2 ) )

        # Parse freeze time.
        match = re.compile( '(\d{1,}):(\d{2}):(\d{2})' ).match( info[ "info" ][ "scoreboard-freeze-duration" ] )
        if not match:
            print("Error: can't parse freeze duration")
            exit(1)

        freeze_duration = int( match.group( 1 ) ) * 3600 + int( match.group( 2 ) ) * 60 + int( match.group( 3 ) )
        freeze_time = duration - freeze_duration

        query = "INSERT INTO contests (contest_name, start_time, length, freeze ) VALUES (%s,%s,%s,%s)"
	params = (info[ "info" ][ "name" ],  start_time, duration, freeze_time)
        cursor = dbConn.cursor()
        cursor.execute(query, params)

    def importTeam(self, team):
        t = team["team"]
        
        query = "INSERT INTO teams (id, team_id, team_name, institution_id, school_name, school_short, country) VALUES (%s,%s,%s,%s,%s,%s,%s)"
        params = (int(t["id"]), int(t["id"]), t["name"], int(t["institution-id"]), t["affiliation"], t["affiliation-short-name"], t["nationality"])
        cursor = dbConn.cursor()
        cursor.execute(query, params)

    def importProblem(self, problem):
        p = problem["problem"]
        query = "INSERT INTO problems (id, problem_id, problem_name, color) VALUES (%s,%s,%s,%s)"
        params =  (int(p["id"]), dbConn.escape_string(p["label"]), dbConn.escape_string(p["name"]), dbConn.escape_string(p["rgb"]))
	cursor = dbConn.cursor()
        cursor.execute(query, params)
	

if __name__ == '__main__':
    imp = ConfigImporter()
    imp.cleanTables()
    imp.performRequest("info", imp.importConfig)
    imp.performRequest("problem", imp.importProblem)
    imp.performRequest("team", imp.importTeam)
