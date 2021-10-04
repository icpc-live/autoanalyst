-- phpMyAdmin SQL Dump
-- version 4.2.12deb2+deb8u2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 11, 2017 at 01:28 AM
-- Server version: 10.0.30-MariaDB-0+deb8u1
-- PHP Version: 5.6.30-0+deb8u1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `icat`
--

CREATE DATABASE IF NOT EXISTS `icat` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `icat`;

--
-- Table structure for table `analyzer_parameters`
--

DROP TABLE IF EXISTS `analyzer_parameters`;
CREATE TABLE IF NOT EXISTS `analyzer_parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `value` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `contests`
--

-- NOTE: currently the database scheme does not support multiple
-- contests yet, so this table should contain only a single contest.
DROP TABLE IF EXISTS `contests`;
CREATE TABLE IF NOT EXISTS `contests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `contest_name` varchar(150) NOT NULL,
  `start_time` int(11) DEFAULT NULL COMMENT 'Contest start time as Unix Epoch seconds.',
  `length` int(11) DEFAULT NULL COMMENT 'Contest length in seconds.',
  `freeze` int(11) DEFAULT NULL COMMENT 'Seconds into contest when scoreboard is frozen.',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `edit_activity`
--

--
-- Just a record of the modification times for files in team's
-- directories.  This will generally be restricted to source files,
-- but I suppose this wouldn't be required.
--
-- modify_timestamp is the modification time, in Unix Epoch.
-- modify_time is the minutes since the start of the contest.
--
-- there's a reason we're recording both of these, but I (DBS) don't
-- remember it.  I expect they are both somewhat redundant with the
-- git_tag, since I think that indicates a particular snapshot time.
--
-- Right now, lines changed is the sum of lines removed and lines
-- added, compared to the last revision, as reported by git.
--

DROP TABLE IF EXISTS `edit_activity`;
CREATE TABLE IF NOT EXISTS `edit_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256) DEFAULT NULL,
  `modify_timestamp` int(11) DEFAULT NULL,
  `modify_time` int(11) DEFAULT NULL,
  `line_count` int(11) DEFAULT NULL,
  `file_size_bytes` int(11) DEFAULT NULL,
  `lines_changed` int(11) DEFAULT NULL,
  `git_tag` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `team_path_ea_index` (`team_id`, `path`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `edit_latest`
--

--
-- Summary of edit activity, by problem ID.  It's a map from team id and
-- problem id to the last modification of a file for that problem.
-- This is intended to give a quick report of what each team is working
-- on.
--
-- modify_timestamp is the modification time, in Unix Epoch.

DROP TABLE IF EXISTS `edit_latest`;
CREATE TABLE IF NOT EXISTS `edit_latest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `problem_id` varchar(10) NOT NULL,
  `modify_timestamp` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `entries`
--

DROP TABLE IF EXISTS `entries`;
CREATE TABLE IF NOT EXISTS `entries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `contest_time` int(11) NOT NULL,
  `priority` int(11) NOT NULL,
  `user` varchar(10) NOT NULL,
  `text` text NOT NULL,
  `submission_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `avoid_dups` (`contest_time`,`text`(300))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `facts`
--

DROP TABLE IF EXISTS `facts`;
CREATE TABLE IF NOT EXISTS `facts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `type` varchar(10) NOT NULL,
  `text` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `file_modtime`
--

--
-- Mapping from file team id and path name to last modification
-- timestamp.  This really just exists to make updating the previous table
-- efficient.  We only write a new record for a particular file if it
-- has changed more recently.
--

DROP TABLE IF EXISTS `file_modtime`;
CREATE TABLE IF NOT EXISTS `file_modtime` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256) DEFAULT NULL,
  `modify_timestamp` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `file_to_problem`
--

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

DROP TABLE IF EXISTS `file_to_problem`;
CREATE TABLE IF NOT EXISTS `file_to_problem` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `path` varchar(256) DEFAULT NULL,
  `problem_id` varchar(10) DEFAULT NULL,
  `lang_id` varchar(11) DEFAULT NULL,
  `override` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `team_path_ftp_index` (`team_id`, `path`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `problems`
--

DROP TABLE IF EXISTS `problems`;
CREATE TABLE IF NOT EXISTS `problems` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL COMMENT 'The label (typically a single letter) within the contest.',
  `problem_name` varchar(255) NOT NULL,
  `color` varchar(10) DEFAULT NULL COMMENT 'Hex RGB color specification of the problem.',
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

--
-- Table structure for table `problem_keywords`
--

--
-- Map from problem id to a list of keywords for the problem.
--

DROP TABLE IF EXISTS `problem_keywords`;
CREATE TABLE IF NOT EXISTS `problem_keywords` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `keyword` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `problem_name`
--

DROP TABLE IF EXISTS `problem_name`;
CREATE TABLE IF NOT EXISTS `problem_name` (
  `id` int(11) NOT NULL,
  `problem_id` varchar(10) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
CREATE TABLE IF NOT EXISTS `teams` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) DEFAULT NULL,
  `team_id` int(11) NOT NULL,
  `team_name` varchar(150) NOT NULL,
  `institution_id` int(11) DEFAULT NULL,
  `site_id` int(11) DEFAULT NULL,
  `school_name` varchar(150) DEFAULT NULL,
  `school_short` varchar(32) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `coach_id` int(11) DEFAULT NULL,
  `contestant1_id` int(11) DEFAULT NULL,
  `contestant2_id` int(11) DEFAULT NULL,
  `contestant3_id` int(11) DEFAULT NULL,
  `coach_name` varchar(150) DEFAULT NULL,
  `contestant1_name` varchar(50) DEFAULT NULL,
  `contestant2_name` varchar(50) DEFAULT NULL,
  `contestant3_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;


DROP TABLE IF EXISTS 'teammembers';
CREATE TABLE teammembers(
  id int(11) NOT NULL,
  team_id int(11) NOT NULL,
  full_name varchar(50) DEFAULT NULL,
  role varchar(30) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;


--
-- Table structure for table `team_regions`
--

DROP TABLE IF EXISTS `team_regions`;
CREATE TABLE IF NOT EXISTS `team_regions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `region_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL,
  `region_name` varchar(100) NOT NULL,
  `super_region_name` varchar(100) NOT NULL,
  `super_region_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `team_strips`
--

--
-- Per-team list of strings to strip, if a team is using one
-- or more special strings as part of their filenames.
--

DROP TABLE IF EXISTS `team_strips`;
CREATE TABLE IF NOT EXISTS `team_strips` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL,
  `str` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 ;

--
-- Table structure for table `top_coder`
--

-- FIXME: university_name is not a good primary key, nor does it
-- uniquely identify a team in contests other than ICPC WFs.
DROP TABLE IF EXISTS `top_coder`;
CREATE TABLE IF NOT EXISTS `top_coder` (
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

--
-- Table structure for table `submissions`
--

DROP TABLE IF EXISTS `submissions`;
CREATE TABLE IF NOT EXISTS `submissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL,
  `team_id` int(11) NOT NULL,
  `lang_id` varchar(11) NOT NULL,
  `result` varchar(10) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contest_time` int(11) NOT NULL,
  `submission_id` int(11) NOT NULL,
  `has_video` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `avoid_dups` (`submission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ;

--
-- Create views
--

DROP VIEW IF EXISTS `edit_activity_problem`;
CREATE VIEW edit_activity_problem AS SELECT edit_activity.*, file_to_problem.problem_id
    FROM edit_activity LEFT JOIN file_to_problem ON (edit_activity.team_id = file_to_problem.team_id AND edit_activity.path = file_to_problem.path)
    WHERE file_to_problem.problem_id IS NOT NULL AND file_to_problem.problem_id != 'none';

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
