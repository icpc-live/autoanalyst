import MySQLdb
import sys, dbconfig

from dbconfig import dbHost, dbUser, dbPasswd, database

try:
    dbConn = MySQLdb.connect( host = dbHost,
                              user = dbUser,
                              passwd = dbPasswd,
                              db = database )
    
except MySQLdb.Error, e:
    print "Error %d: %s" % ( e.args[ 0 ], e.args[ 1 ])
    sys.exit( 1 )

# path to the top of the backup directory, date and time
# directories start right under this.
# BACKUP_TOP = "/home/analyst6/homedirs"
BACKUP_TOP = "/home/sturgill/homedirs"

# Static time interval for backups, if it matters, in seconds
BACKUP_INTERVAL = 120

