-- USAGE: Search and replace icpc2013 with the name of the contest

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
-- Table structure for table `icpc2013_teams`
--

CREATE TABLE IF NOT EXISTS `icpc2013_teams` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=106 ;

-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_entries`
--

CREATE TABLE IF NOT EXISTS `icpc2013_entries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `contest_time` int(11) NOT NULL,
  `priority` int(11) NOT NULL,
  `user` varchar(10) NOT NULL,
  `text` text NOT NULL,
  `submission_id` int(11),
  PRIMARY KEY (`id`),
  UNIQUE KEY `avoid_dups` (`contest_time`,`text`(300))
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=406 ;

--
-- Dumping data for table `icpc2013_entries`
--

-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_facts`
--

CREATE TABLE IF NOT EXISTS `icpc2013_facts` (
  `team_id` int(11) NOT NULL,
  `type` varchar(10) NOT NULL,
  `text` varchar(500) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=183 ;

--
-- Dumping data for table `icpc2013_facts`
--

-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_regions`
--

CREATE TABLE IF NOT EXISTS `icpc2013_regions` (
  `team_id` int(11) NOT NULL AUTO_INCREMENT,
  `regional_team_id` int(11) NOT NULL,
  `regional_team_name` varchar(50) NOT NULL,
  `region_name` varchar(50) NOT NULL,
  `region_scoreboard_url` varchar(150) NOT NULL,
  `problems_solved` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  `last_problem_time` int(11) NOT NULL,
  `total_time` int(11) NOT NULL,
  PRIMARY KEY (`team_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=90241 ;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_scoreboard`
--

CREATE TABLE IF NOT EXISTS `icpc2013_scoreboard` (
  `team_id` int(11) NOT NULL,
  `rank` int(11) NOT NULL DEFAULT '1',
  `total_time` int(11) NOT NULL DEFAULT '0',
  `num_solutions` int(11) NOT NULL DEFAULT '0',
  `a_submissions` int(11) NOT NULL DEFAULT '0',
  `a_soln_time` int(11) NOT NULL DEFAULT '0',
  `b_submissions` int(11) NOT NULL DEFAULT '0',
  `b_soln_time` int(11) NOT NULL DEFAULT '0',
  `c_submissions` int(11) NOT NULL DEFAULT '0',
  `c_soln_time` int(11) NOT NULL DEFAULT '0',
  `d_submissions` int(11) NOT NULL DEFAULT '0',
  `d_soln_time` int(11) NOT NULL DEFAULT '0',
  `e_submissions` int(11) NOT NULL DEFAULT '0',
  `e_soln_time` int(11) NOT NULL DEFAULT '0',
  `f_submissions` int(11) NOT NULL DEFAULT '0',
  `f_soln_time` int(11) NOT NULL DEFAULT '0',
  `g_submissions` int(11) NOT NULL DEFAULT '0',
  `g_soln_time` int(11) NOT NULL DEFAULT '0',
  `h_submissions` int(11) NOT NULL DEFAULT '0',
  `h_soln_time` int(11) NOT NULL DEFAULT '0',
  `i_submissions` int(11) NOT NULL DEFAULT '0',
  `i_soln_time` int(11) NOT NULL DEFAULT '0',
  `j_submissions` int(11) NOT NULL DEFAULT '0',
  `j_soln_time` int(11) NOT NULL DEFAULT '0',
  `k_submissions` int(11) NOT NULL DEFAULT '0',
  `k_soln_time` int(11) NOT NULL DEFAULT '0',
  `l_submissions` int(11) NOT NULL DEFAULT '0',
  `l_soln_time` int(11) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_submissions`
--

CREATE TABLE IF NOT EXISTS `icpc2013_submissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL,
  `lang_id` int(11) NOT NULL,
  `result` char(10) NOT NULL,
  `submission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_problems`
--

CREATE TABLE IF NOT EXISTS `icpc2013_problems` (
  `problem_id` int(11) NOT NULL,
  `problem_letter` char(10) NOT NULL, 
  `problem_name` char(255) NOT NULL, 
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
  `wa` int(11) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2013_top_coder`
--

CREATE TABLE IF NOT EXISTS `icpc2013_top_coder` (
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
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

-- 
-- Create views for the current contest
CREATE VIEW teams AS SELECT * FROM icpc2013_teams;
CREATE VIEW entries AS SELECT * FROM icpc2013_entries;
CREATE VIEW facts AS SELECT * FROM icpc2013_facts;
CREATE VIEW regions AS SELECT * FROM icpc2013_regions;
CREATE VIEW scoreboard AS SELECT * FROM icpc2013_scoreboard;
CREATE VIEW top_coder AS SELECT * FROM icpc2013_top_coder;
