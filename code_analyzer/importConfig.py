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

    def importConfig( self ):
        d = tempfile.mkdtemp(prefix='import-config')
        h = httplib2.Http(os.path.join(d,".cache"))
        h.add_credentials( self.CDSUser, self.CDSPass )
        h.disable_ssl_certificate_validation=True

        # if_modified_since_header = "If-Modified-Since: %s" % (self.teamLastModified[ teamIdx ])
        # pull down the latest backup archive, and unpack it.
        (responseHeader, result) = h.request( "%s/contest/info" % ( self.CDSRoot ), "GET" )

        if responseHeader["status"] == "200":
            print result
            info = json.loads(result)
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

                
            update = "INSERT INTO contests (contest_name, start_time, length, freeze ) VALUES ( '%s', '%s', '%s', '%s' )" % ( dbConn.escape_string( info[ "info" ][ "name" ] ),  start_time, duration, freeze_time )
                
            cursor = dbConn.cursor()
            cursor.execute( update )
            print update

        else:
            print("error %s" % responseHeader)

        shutil.rmtree(d)

if __name__ == '__main__':
    imp = ConfigImporter()
    imp.importConfig()
