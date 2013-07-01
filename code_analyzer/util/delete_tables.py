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
DROP VIEW IF EXISTS `file_to_problem`;
DROP VIEW IF EXISTS `edit_activity`;
DROP VIEW IF EXISTS `file_modtime`;
DROP VIEW IF EXISTS `edit_latest`;
DROP VIEW IF EXISTS `analyzer_parameters`;
DROP VIEW IF EXISTS `problem_name`;
DROP VIEW IF EXISTS `problem_keywords`;
DROP VIEW IF EXISTS `team_strips`;
DROP VIEW IF EXISTS `edit_activity_problem`;
""" )

cursor = dbConn.cursor()
cursor.execute( """
DROP TABLE IF EXISTS `icpc2013_file_to_problem`;
DROP TABLE IF EXISTS `icpc2013_edit_activity`;
DROP TABLE IF EXISTS `icpc2013_file_modtime`;
DROP TABLE IF EXISTS `icpc2013_edit_latest`;
DROP TABLE IF EXISTS `icpc2013_analyzer_parameters`;
DROP TABLE IF EXISTS `icpc2013_problem_name`;
DROP TABLE IF EXISTS `icpc2013_problem_keywords`;
DROP TABLE IF EXISTS `icpc2013_team_strips`;
""" )

cursor.close();
dbConn.close();
