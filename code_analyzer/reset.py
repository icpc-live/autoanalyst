#!/usr/bin/python

# This script resets the code analyzer database structure and data.
# Note that 'create_tables.sql' contains duplicate structure that is
# also present in ../create_icat_instance.sql; it might be better to
# refactor this.

import os, sys
from common import config

os.chdir(os.path.dirname(sys.argv[0]))

db = config['database']

os.system('cat delete_tables.sql create_tables.sql populate.sql | ' +
          'mysql --host=%s --database=%s --user=%s --password=%s'%
          (db['host'],db['name'],db['user'],db['password']))
