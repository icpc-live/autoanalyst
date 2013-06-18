#!/usr/bin/python
# Populate database with example problems, patterns, etc.

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.split(os.path.split(os.path.abspath( inspect.getfile( inspect.currentframe())))[0])[0]
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn

#
# Last modification time and file size for every team and every problem.
# The modify_time and file_size fields may be null if we haven't found.
# a copy of the file yet.
#
# modify_time_utc is the modification time, in utc.
# modify_time is the minutes since the start of the contest.

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2012_edit_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `problem_id` varchar(10) NOT NULL,
  `path` varchar(256),
  `modify_time_utc` timestamp,
  `modify_time` int(11),
  `line_count` int(11),
  `git_tag` varchar(30),
  `valid` tinyint(1),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Last modification time and file size for every team and every problem.
#
# modify_time_utc is the modification time, in utc.
# modify_time is the minutes since the start of the contest.

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2012_edit_latest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `problem_id` varchar(10) NOT NULL,
  `modify_time_utc` timestamp,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Parameters for the code analyzer.
#
# CONTEST_START: UTC start of contest, YYYY-MM-DD-hh-mm-ss
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2012_analyzer_parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `value` varchar(60),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Map from problem id to a list of names.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2012_problem_name` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `name` varchar(45),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Explicit path name for a source file, if we need to override the
# regular search.  Problem_id may be 'none' if we want to force a
# a particular file to be ignored.  Really, it would be good if we
# just let this be null, but we're live and I can't change it right
# now.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2012_problem_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `problem_id` varchar(10) NOT NULL,
  `path` varchar(256),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Per-team list of strings to strip, if a team is using one
# or more special strings as part of their filenames.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2012_team_strip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `str` varchar(30),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

# 
# Create views for the current contest


cursor = dbConn.cursor()
cursor.execute( """
CREATE VIEW edit_activity AS SELECT * FROM icpc2012_edit_activity;
CREATE VIEW edit_latest AS SELECT * FROM icpc2012_edit_latest;
CREATE VIEW analyzer_parameters AS SELECT * FROM icpc2012_analyzer_parameters;
CREATE VIEW problem_name AS SELECT * FROM icpc2012_problem_name;
CREATE VIEW problem_file AS SELECT * FROM icpc2012_problem_file;
CREATE VIEW team_strip AS SELECT * FROM icpc2012_team_strip;
""" )

cursor.close()
dbConn.close()
