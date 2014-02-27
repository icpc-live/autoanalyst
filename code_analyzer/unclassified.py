#!/usr/bin/python

# /home/icpc/team_backups

import os, sys, inspect

# Include this scripts home directory in the search path.
cmd_folder = os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0])
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn, BACKUP_TOP
from analyzer import Analyzer

if __name__ == '__main__':
    analyzer = Analyzer( BACKUP_TOP )

    analyzer.reportUnclassified( BACKUP_TOP )
    
