#!/usr/bin/python
# Populate database with example problems, patterns, etc.

import os, sys, inspect

# Include the parent directory in the search path.
cmd_folder = os.path.split(os.path.split(os.path.abspath( inspect.getfile( inspect.currentframe())))[0])[0]
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from common import dbConn

#
# Mapping from file team id and path name to problem id.  This records
# any decisions the code analyzer makes about what problem each file
# represents, and it also includes overrides made by the operator.
# Problem_id may be null, to indicate that a particular file should be
# ignored.
#
# override : true if this is inserted by the operator, so the script won't change it.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_file_to_problem` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256),
  `problem_id` varchar(10),
  `override` tinyint(1),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Just a record of the modification times for files in team's directories.  This will
# generally be restricted to source files, but I suppose this wouldn't be required.
#
# modify_time_utc is the modification time, in utc.
# modify_time is the minutes since the start of the contest.
# 
# there's a reason we're recording both of these, but I (DBS) don't
# remember it.  I expect they are both somewhat redundant with the
# git_tag, since I think that indicates a particular snapshot time.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_edit_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256),
  `modify_time_utc` timestamp,
  `modify_time` int(11),
  `line_count` int(11),
  `git_tag` varchar(30),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Mapping from file team id and path name to last utc modification
# time.  This really just exists to make updating the previous table
# efficient.  We only write a new record for a particular file if it
# has changed more recently.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_file_modtime` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256),
  `modify_time_utc` timestamp,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Summary of edit activity, by problem ID.  It's a map from team id and
# problem id to the last modification of a file for that problem.
# This is intended to give a quick report of what each team is working
# on.
#
# modify_time_utc is the modification time, in utc.

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_edit_latest` (
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
CREATE TABLE IF NOT EXISTS `icpc2013_analyzer_parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `value` varchar(60),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Map from problem id to a problem name.  We have this elswehere, but
# I think it's just php.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_problem_name` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Map from problem id to a list of keywords for the problem.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_problem_keywords` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `keyword` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
""" )

#
# Per-team list of strings to strip, if a team is using one
# or more special strings as part of their filenames.
#

cursor = dbConn.cursor()
cursor.execute( """
CREATE TABLE IF NOT EXISTS `icpc2013_team_strips` (
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
CREATE VIEW file_to_problem AS SELECT * FROM icpc2013_file_to_problem;
CREATE VIEW edit_activity AS SELECT * FROM icpc2013_edit_activity;
CREATE VIEW file_modtime AS SELECT * FROM icpc2013_file_modtime;
CREATE VIEW edit_latest AS SELECT * FROM icpc2013_edit_latest;
CREATE VIEW analyzer_parameters AS SELECT * FROM icpc2013_analyzer_parameters;
CREATE VIEW problem_name AS SELECT * FROM icpc2013_problem_name;
CREATE VIEW problem_keywords AS SELECT * FROM icpc2013_problem_keywords;
CREATE VIEW team_strips AS SELECT * FROM icpc2013_team_strips;
CREATE VIEW edit_activity_problem AS SELECT edit_activity.*, file_to_problem.problem_id FROM edit_activity LEFT JOIN file_to_problem ON (edit_activity.team_id = file_to_problem.team_id AND edit_activity.path = file_to_problem.path);
""" )

cursor.close()
dbConn.close()
