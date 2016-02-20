-- USAGE: Search and replace icpc2014 with the name of the contest

-- Host: gedrix.ida.liu.se
-- Server version: 5.1.49
-- PHP Version: 5.3.9

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `icat`
--

--
-- Table structure for table `icpc2014_teams`
--

DROP TABLE IF EXISTS `icpc2014_teams`;
CREATE TABLE IF NOT EXISTS `icpc2014_teams` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL,
  `team_name` varchar(150) NOT NULL,
  `institution_id` int(11) NOT NULL,
  `site_id` int(11) NOT NULL,
  `school_name` varchar(150) NOT NULL,
  `school_short` varchar(10) NOT NULL,
  `country` varchar(50) NOT NULL,
  `coach_id` int(11) NOT NULL,
  `contestant1_id` int(11) NOT NULL,
  `contestant2_id` int(11) NOT NULL,
  `contestant3_id` int(11) NOT NULL,
  `coach_name` varchar(150) NOT NULL,
  `contestant1_name` varchar(50) NOT NULL,
  `contestant2_name` varchar(50) NOT NULL,
  `contestant3_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

-- --------------------------------------------------------

--
-- Table structure for table `icpc2014_entries`
--

DROP TABLE IF EXISTS `icpc2014_entries`;
CREATE TABLE IF NOT EXISTS `icpc2014_entries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `contest_time` int(11) NOT NULL,
  `priority` int(11) NOT NULL,
  `user` varchar(10) NOT NULL,
  `text` text NOT NULL,
  `submission_id` int(11),
  PRIMARY KEY (`id`),
  UNIQUE KEY `avoid_dups` (`contest_time`,`text`(300))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Dumping data for table `icpc2014_entries`
--

-- --------------------------------------------------------

--
-- Table structure for table `icpc2014_facts`
--

DROP TABLE IF EXISTS `icpc2014_facts`;
CREATE TABLE IF NOT EXISTS `icpc2014_facts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `type` varchar(10) NOT NULL,
  `text` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Dumping data for table `icpc2014_facts`
--



-- --------------------------------------------------------

--
-- Table structure for table `icpc2014_submissions`
--

DROP TABLE IF EXISTS `icpc2014_submissions`;
CREATE TABLE IF NOT EXISTS `icpc2014_submissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `team_id` int(11) NOT NULL,
  `lang_id` varchar(11) NOT NULL,
  `result` varchar(10) NOT NULL,
  `date` timestamp NOT NULL default '0000-00-00 00:00:00' on update CURRENT_TIMESTAMP,
  `contest_time` int(11) NOT NULL,
  `submission_id` int(11) NOT NULL,
  `has_video` boolean DEFAULT false,
  PRIMARY KEY (`id`),
  UNIQUE KEY `avoid_dups` (`submission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2014_problems`
--

DROP TABLE IF EXISTS `icpc2014_problems`;
CREATE TABLE IF NOT EXISTS `icpc2014_problems` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL COMMENT 'The label (typically a single letter) within the contest.',
  `problem_name` varchar(255) NOT NULL,
  `submissions` int(11) NOT NULL DEFAULT '0',
  `first_solved_at` int(11) NOT NULL DEFAULT '0',
  `first_solved_by` int(11) NOT NULL DEFAULT '0',
  `ac` int(11) NOT NULL DEFAULT '0',
  `ce` int(11) NOT NULL DEFAULT '0',
  `if` int(11) NOT NULL DEFAULT '0',
  `mle` int(11) NOT NULL DEFAULT '0',
  `ole` int(11) NOT NULL DEFAULT '0',
  `pe` int(11) NOT NULL DEFAULT '0',
  `rte` int(11) NOT NULL DEFAULT '0',
  `tle` int(11) NOT NULL DEFAULT '0',
  `wa` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;

-- --------------------------------------------------------

--
-- Table structure for table `icpc2014_team_regions`
--

DROP TABLE IF EXISTS `icpc2014_team_regions`;
CREATE TABLE `icpc2014_team_regions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `region_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL,
  `region_name` varchar(100) NOT NULL,
  `super_region_name` varchar(100) NOT NULL,
  `super_region_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2014_top_coder`
--

DROP TABLE IF EXISTS `icpc2014_top_coder`;
CREATE TABLE IF NOT EXISTS `icpc2014_top_coder` (
  `university_name` varchar(150) NOT NULL,
  `coach_tcname` varchar(50) NOT NULL,
  `contestant1_tcname` varchar(50) NOT NULL,
  `contestant2_tcname` varchar(50) NOT NULL,
  `contestant3_tcname` varchar(50) NOT NULL,
  `coach_tcid` int(11) NOT NULL,
  `contestant1_tcid` int(11) NOT NULL,
  `contestant2_tcid` int(11) NOT NULL,
  `contestant3_tcid` int(11) NOT NULL,
  `coach_rating` varchar(100) NOT NULL,
  `contestant1_rating` varchar(100) NOT NULL,
  `contestant2_rating` varchar(100) NOT NULL,
  `contestant3_rating` varchar(100) NOT NULL,
  `coach_rank` varchar(50) NOT NULL,
  `contestant1_rank` varchar(50) NOT NULL,
  `contestant2_rank` varchar(50) NOT NULL,
  `contestant3_rank` varchar(50) NOT NULL,
  `coach_srank` varchar(50) NOT NULL,
  `contestant1_srank` varchar(50) NOT NULL,
  `contestant2_srank` varchar(50) NOT NULL,
  `contestant3_srank` varchar(50) NOT NULL,
  `coach_crank` varchar(50) NOT NULL,
  `contestant1_crank` varchar(50) NOT NULL,
  `contestant2_crank` varchar(50) NOT NULL,
  `contestant3_crank` varchar(50) NOT NULL,
  PRIMARY KEY (`university_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;


-- --------------------------------------------------------

-- 
-- Create views for the current contest
DROP VIEW IF EXISTS entries;
CREATE VIEW entries AS SELECT * FROM icpc2014_entries;
DROP VIEW IF EXISTS facts;
CREATE VIEW facts AS SELECT * FROM icpc2014_facts;
DROP VIEW IF EXISTS submissions;
CREATE VIEW submissions AS SELECT * FROM icpc2014_submissions;
DROP VIEW IF EXISTS teams;
CREATE VIEW teams AS SELECT * FROM icpc2014_teams;
DROP VIEW IF EXISTS team_regions;
CREATE VIEW team_regions AS SELECT * FROM icpc2014_team_regions;
DROP VIEW IF EXISTS top_coder;
CREATE VIEW top_coder AS SELECT * FROM icpc2014_top_coder;



--
-- Mapping from file, team id and path name to problem id and
-- language.  This records any decisions the code analyzer makes about
-- what problem each file represents, and it also includes overrides
-- made by the operator.  Problem_id may be null, to indicate that a
-- particular file should be ignored.
--
-- The lang_id may be null, since we may be tracking files that aren't
-- even source files.  If it is non-null, it should give the source
-- language, but, it's possible to have multiple languages associated
-- with a team's work, if, say, they switch languages.
--
-- override : true if this is inserted by the operator, so the script
-- won't change it.
--

DROP TABLE IF EXISTS `icpc2014_file_to_problem`;
CREATE TABLE IF NOT EXISTS `icpc2014_file_to_problem` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256),
  `problem_id` varchar(10),
  `lang_id` varchar(11),
  `override` tinyint(1),
  PRIMARY KEY (`id`),
  INDEX `team_path_ftp_index` (`team_id`, `path`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Just a record of the modification times for files in team's
-- directories.  This will generally be restricted to source files,
-- but I suppose this wouldn't be required.
--
-- modify_time_utc is the modification time, in utc.
-- modify_time is the minutes since the start of the contest.
--
-- there's a reason we're recording both of these, but I (DBS) don't
-- remember it.  I expect they are both somewhat redundant with the
-- git_tag, since I think that indicates a particular snapshot time.
--
-- Right now, lines changed is the sum of lines removed and lines
-- added, compared to the last revision, as reported by git.
--

DROP TABLE IF EXISTS `icpc2014_edit_activity`;
CREATE TABLE IF NOT EXISTS `icpc2014_edit_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256),
  `modify_time_utc` timestamp,
  `modify_time` int(11),
  `line_count` int(11),
  `file_size_bytes` int(11),
  `lines_changed` int(11),
  `git_tag` varchar(30),
  PRIMARY KEY (`id`),
  INDEX `team_path_ea_index` (`team_id`, `path`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Mapping from file team id and path name to last utc modification
-- time.  This really just exists to make updating the previous table
-- efficient.  We only write a new record for a particular file if it
-- has changed more recently.
--

DROP TABLE IF EXISTS `icpc2014_file_modtime`;
CREATE TABLE IF NOT EXISTS `icpc2014_file_modtime` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256),
  `modify_time_utc` timestamp,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Summary of edit activity, by problem ID.  It's a map from team id and
-- problem id to the last modification of a file for that problem.
-- This is intended to give a quick report of what each team is working
-- on.
--
-- modify_time_utc is the modification time, in utc.

DROP TABLE IF EXISTS `icpc2014_edit_latest`;
CREATE TABLE IF NOT EXISTS `icpc2014_edit_latest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `problem_id` varchar(10) NOT NULL,
  `modify_time_utc` timestamp,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Map from problem id to a list of keywords for the problem.
--

DROP TABLE IF EXISTS `icpc2014_problem_keywords`;
CREATE TABLE IF NOT EXISTS `icpc2014_problem_keywords` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `keyword` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Per-team list of strings to strip, if a team is using one
-- or more special strings as part of their filenames.
--

DROP TABLE IF EXISTS `icpc2014_team_strips`;
CREATE TABLE IF NOT EXISTS `icpc2014_team_strips` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `str` varchar(30),
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Create views for the current contest
--

DROP VIEW IF EXISTS file_to_problem;
CREATE VIEW file_to_problem AS SELECT * FROM icpc2014_file_to_problem;
DROP VIEW IF EXISTS edit_activity;
CREATE VIEW edit_activity AS SELECT * FROM icpc2014_edit_activity;
DROP VIEW IF EXISTS file_modtime;
CREATE VIEW file_modtime AS SELECT * FROM icpc2014_file_modtime;
DROP VIEW IF EXISTS edit_latest;
CREATE VIEW edit_latest AS SELECT * FROM icpc2014_edit_latest;
DROP VIEW IF EXISTS problem_keywords;
CREATE VIEW problem_keywords AS SELECT * FROM icpc2014_problem_keywords;
DROP VIEW IF EXISTS team_strips;
CREATE VIEW team_strips AS SELECT * FROM icpc2014_team_strips;
DROP VIEW IF EXISTS edit_activity_problem;
CREATE VIEW edit_activity_problem AS SELECT edit_activity.*, file_to_problem.problem_id
    FROM edit_activity LEFT JOIN file_to_problem ON (edit_activity.team_id = file_to_problem.team_id AND edit_activity.path = file_to_problem.path)
    WHERE file_to_problem.problem_id IS NOT NULL AND file_to_problem.problem_id != 'none';
