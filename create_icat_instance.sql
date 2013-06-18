-- USAGE: Search and replace icpc2011 with the name of the contest

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
-- Table structure for table `icpc2011`
--

CREATE TABLE IF NOT EXISTS `icpc2011` (
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
-- Table structure for table `icpc2011_entries`
--

CREATE TABLE IF NOT EXISTS `icpc2011_entries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `contest_time` int(11) NOT NULL,
  `priority` int(11) NOT NULL,
  `user` varchar(10) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `avoid_dups` (`contest_time`,`text`(300))
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=406 ;

--
-- Dumping data for table `icpc2011_entries`
--

-- --------------------------------------------------------

--
-- Table structure for table `icpc2011_facts`
--

CREATE TABLE IF NOT EXISTS `icpc2011_facts` (
  `team_id` int(11) NOT NULL,
  `type` varchar(10) NOT NULL,
  `text` varchar(500) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=183 ;

--
-- Dumping data for table `icpc2011_facts`
--

-- --------------------------------------------------------

--
-- Table structure for table `icpc2011_regions`
--

CREATE TABLE IF NOT EXISTS `icpc2011_regions` (
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

--
-- Dumping data for table `icpc2011_regions`
--

INSERT INTO `icpc2011_regions` (`team_id`, `regional_team_id`, `regional_team_name`, `region_name`, `region_scoreboard_url`, `problems_solved`, `rank`, `last_problem_time`, `total_time`) VALUES
(90197, 81960, 'Proof', 'ACM-ICPC Asia Amritapuri Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=610&cid=15864', 7, 2, 243, 674),
(90198, 82622, 'phoenix', 'ACM-ICPC Asia Amritapuri Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=610&cid=15864', 6, 3, 156, 525),
(90199, 86611, 'OpenLegend', 'ACM-ICPC Asia Chengdu Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=658&cid=15864', 8, 5, 289, 1521),
(90201, 81217, 'reverse_iterator', 'ACM-ICPC Asia Daejeon Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=608&cid=15864', 7, 4, 225, 748),
(90203, 88375, 'Code_Geass', 'ACM-ICPC Asia Fuzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=659&cid=15864', 8, 4, 0, 1031),
(90204, 88290, 'DPS', 'ACM-ICPC Asia Fuzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=659&cid=15864', 8, 5, 0, 1196),
(90205, 88558, 'ecnu_puzzle', 'ACM-ICPC Asia Fuzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=659&cid=15864', 8, 7, 0, 1457),
(90206, 84142, 'APTX4869', 'ACM-ICPC Asia Hangzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=660&cid=15864', 6, 4, 264, 945),
(90207, 84571, 'ErBao', 'ACM-ICPC Asia Hangzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=660&cid=15864', 5, 5, 211, 532),
(90208, 84786, 'PpG_LongPo', 'ACM-ICPC Asia Hangzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=660&cid=15864', 5, 6, 223, 650),
(90209, 81931, 'TJU-Zero', 'ACM-ICPC Asia Harbin Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=597&cid=15864', 8, 3, 231, 1001),
(90210, 82131, 'BJTU-ACMagic', 'ACM-ICPC Asia Harbin Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=597&cid=15864', 8, 5, 281, 1420),
(90211, 89377, 'Last War of PMP', 'ACM-ICPC Asia Tehran Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=606&cid=15864', 8, 1, 0, 1029),
(90212, 89849, '3gespenek', 'ACM-ICPC Asia Tehran Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=606&cid=15864', 8, 2, 0, 1194),
(90213, 83256, 'OpenGL', 'ACM-ICPC Asia Tianjin Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=664&cid=15864', 7, 5, 299, 1086),
(90214, 83116, 'Blue Sky', 'ACM-ICPC Asia Tianjin Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=664&cid=15864', 7, 6, 264, 1143),
(90215, 82985, 'HDU-Knuth', 'ACM-ICPC Asia Tianjin Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=664&cid=15864', 6, 8, 258, 858),
(90216, 74736, 'd3sxp', 'ACM-ICPC Asia Tokyo Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=600&cid=15864', 6, 6, 0, 904),
(90217, 90144, 'Deep Thought', 'ACM-ICPC Asia Kanpur Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=601&cid=15864', 5, 3, 267, 655),
(90218, 74563, 'USAGI Code', 'ACM-ICPC Asia Tokyo Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=600&cid=15864', 10, 1, 0, 1439),
(90219, 87927, 'HKUST_Optimus Prime', 'ACM-ICPC Asia Kuala Lumpur Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=663&cid=15864', 8, 3, 0, 661),
(90221, 89145, 'Equanimity', 'ACM-ICPC Asia Kuala Lumpur Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=663&cid=15864', 8, 4, 0, 674),
(90222, 86283, 'SYSU_Calvados', 'ACM-ICPC Asia Chengdu Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=658&cid=15864', 9, 1, 283, 1432),
(90223, 84876, 'Luminar', 'ACM-ICPC Asia Hangzhou Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=660&cid=15864', 8, 1, 229, 805),
(90224, 84261, 'HexHeaven', 'ACM-ICPC Asia Amritapuri Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=610&cid=15864', 7, 1, 236, 661),
(90225, 82920, 'Dubhe', 'ACM-ICPC Asia Tianjin Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=664&cid=15864', 9, 1, 289, 1369),
(90226, 84851, 'ArcOfDream', 'ACM-ICPC Asia Hanoi Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=661&cid=15864', 10, 1, 0, 741),
(90228, 83974, '+1 ironwood branch', 'ACM-ICPC Asia Jakarta Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=607&cid=15864', 9, 1, 170, 670),
(90229, 87730, 'NTU Pigeons', 'ACM-ICPC Asia Kuala Lumpur Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=663&cid=15864', 9, 2, 0, 809),
(90233, 89367, 'ManiAC', 'ACM-ICPC Asia Kuala Lumpur Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=663&cid=15864', 10, 1, 0, 1211),
(90234, 89472, 'PKU_JiangYou', 'ACM-ICPC Asia Chengdu Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=658&cid=15864', 9, 2, 296, 1748),
(90238, 90157, 'ANY Dream', 'ACM-ICPC Asia Kanpur Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=601&cid=15864', 7, 2, 227, 791),
(90239, 86292, 'UESTC-Melody', 'ACM-ICPC Asia Chengdu Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=658&cid=15864', 7, 6, 273, 897),
(90240, 81464, 'RoyalRoader', 'ACM-ICPC Asia Daejeon Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=608&cid=15864', 10, 1, 165, 733),
(89596, 85584, 'RazÃ£o Cruzada', 'South America/Brazil Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=593&cid=15864', 9, 1, 196, 946),
(89597, 85526, 'ITA - Carteado', 'South America/Brazil Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=593&cid=15864', 9, 2, 228, 1195),
(89598, 85520, 'SUDO', 'South America/Brazil Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=593&cid=15864', 8, 4, 283, 1077),
(89599, 85531, 'RGA', 'South America/Brazil Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=593&cid=15864', 8, 5, 286, 1251),
(89600, 85669, 'Isso Ã© tudo pessoal', 'South America/Brazil Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=593&cid=15864', 7, 6, 148, 422),
(89601, 85564, 'Grito da Trypanosoma', 'South America/Brazil Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=593&cid=15864', 7, 7, 193, 680),
(89602, 85355, 'UniValleException', 'South America/North Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=589&cid=15864', 6, 1, 250, 829),
(89603, 85554, 'Loperamida Clorhidrato 2mg', 'South America/North Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=589&cid=15864', 4, 2, 129, 248),
(89604, 86363, 'UN-03', 'South America/North Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=589&cid=15864', 4, 3, 149, 296),
(89605, 84838, 'AJI', 'South America/South Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=584&cid=15864', 8, 1, 194, 723),
(89606, 81454, 'HaCkErMaTh', 'South America/South Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=584&cid=15864', 8, 2, 272, 823),
(89607, 84158, 'aWARush', 'South America/South Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=584&cid=15864', 8, 3, 197, 837),
(89608, 86321, 'ACM-1PT', 'South America/South Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=584&cid=15864', 4, 9, 212, 342),
(89632, 84219, 'Manowar', 'Mexico & Central America Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=586&cid=15864', 8, 1, 274, 1176),
(89633, 88785, 'Olimpo', 'Mexico & Central America Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=586&cid=15864', 6, 2, 211, 855),
(89634, 88786, 'UH++', 'Mexico & Central America Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=586&cid=15864', 5, 3, 133, 416),
(89610, 81695, 'TTL300', 'Southeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=613&cid=15864', 9, 1, 0, 909),
(90032, 87491, 'Warsaw Eagles', 'Central Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=635&cid=15864', 8, 1, 0, 792),
(90033, 89251, 'SPbSU ITMO 2', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 11, 1, 0, 1230),
(90034, 75420, 'deFAUlt', 'Northwestern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=614&cid=15864', 8, 1, 254, 1257),
(90035, 75280, 'Dirt collector', 'Southwestern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=633&cid=15864', 5, 1, 256, 904),
(90045, 89325, 'MSU Unpredictable', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 10, 2, 0, 1070),
(90046, 89329, 'MIPT Waterogers', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 8, 5, 0, 997),
(90047, 89252, 'SPb SU 1: DrinkLess', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 9, 6, 0, 1018),
(90048, 89269, 'NNSU', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 9, 7, 0, 1128),
(90049, 89268, 'Saratov SU 2', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 9, 8, 0, 1146),
(90050, 89289, 'SUrSU 1', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 9, 9, 0, 1442),
(90051, 89286, 'Ural SU Lynx', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 9, 10, 0, 1606),
(90052, 89310, 'BelarusianSU 1', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 8, 12, 0, 788),
(90053, 89301, 'Orel STU 1', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 8, 15, 0, 1220),
(90054, 89287, 'Perm SU 1', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 8, 16, 0, 1291),
(90055, 89822, 'Kazakh-British TU 1', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 7, 21, 0, 1046),
(90056, 89703, 'Novosibirsk SU 1', 'Northeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=617&cid=15864', 7, 22, 0, 1063),
(90057, 83365, 'Exploitless', 'Southeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=613&cid=15864', 9, 2, 0, 974),
(90058, 84115, 'LNU United', 'Southeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=613&cid=15864', 8, 6, 0, 951),
(90059, 75087, 'DonNU_United', 'Southeastern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=613&cid=15864', 8, 7, 0, 959),
(90060, 84531, 'Jagiellonian Infinity', 'Central Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=635&cid=15864', 7, 2, 0, 842),
(90061, 88273, 'UWr4', 'Central Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=635&cid=15864', 6, 7, 0, 979),
(90062, 87114, 'Bubble Sorters', 'Northwestern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=614&cid=15864', 7, 2, 271, 993),
(90063, 88332, 'Johan''s Angels', 'Northwestern Europe Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=614&cid=15864', 7, 3, 291, 1332),
(89586, 80448, 'Waterloo Black', 'East Central NA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=644&cid=15864', 8, 1, 278, 1104),
(89587, 83515, 'Princeton', 'Greater NY Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=642&cid=15864', 8, 1, 187, 710),
(89590, 79088, 'Duke 0', 'Mid-Atlantic USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=645&cid=15864', 5, 1, 239, 813),
(89591, 76496, 'Bardeen', 'Mid-Central USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=655&cid=15864', 9, 1, 284, 948),
(89592, 84489, 'Blue', 'North Central NA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=643&cid=15864', 8, 1, 285, 1166),
(89593, 89153, 'MIT Engineers', 'Northeast North America Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=648&cid=15864', 7, 1, 0, 1183),
(89594, 86823, 'Alberta 1', 'Rocky Mountain Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=646&cid=15864', 9, 1, 0, 1494),
(89595, 82890, 'OU A', 'South Central USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=657&cid=15864', 8, 1, 299, 800),
(89635, 86393, 'UM Hard Boiled', 'Southeast USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=654&cid=15864', 7, 1, 0, 874),
(89904, 81320, 'Dragons', 'East Central NA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=644&cid=15864', 8, 2, 293, 1237),
(89905, 83460, 'Victors', 'East Central NA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=644&cid=15864', 7, 3, 293, 1314),
(89906, 79099, 'Bar', 'Mid-Atlantic USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=645&cid=15864', 4, 3, 244, 474),
(89907, 85641, 'UMCP Terps 1', 'Mid-Atlantic USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=645&cid=15864', 4, 5, 237, 666),
(89908, 75870, 'Works in Theory', 'Mid-Central USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=655&cid=15864', 9, 2, 286, 1175),
(89909, 87004, 'Wrong Answer', 'North Central NA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=643&cid=15864', 7, 2, 285, 769),
(89910, 86835, 'SDSMT Blue', 'North Central NA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=643&cid=15864', 7, 3, 281, 1051),
(89911, 84945, 'HMC 42', 'Southern California Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=656&cid=15864', 7, 1, 230, 667),
(89912, 87973, 'UCSD Papyrus', 'Southern California Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=656&cid=15864', 7, 2, 217, 799),
(90023, 75864, 'Onward and Upward', 'Mid-Central USA Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=655&cid=15864', 7, 4, 150, 488),
(90071, 88550, 'WildCat 1', 'Pacific Northwest Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=647&cid=15864', 8, 1, 299, 1109),
(90072, 84589, 'SFU Cardinal', 'Pacific Northwest Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=647&cid=15864', 7, 3, 255, 849),
(89585, 80540, 'Chimera', 'South Pacific Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=577&cid=15864', 6, 6, 236, 641),
(89631, 79414, 'Macrohard', 'South Pacific Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=577&cid=15864', 8, 1, 249, 834),
(89611, 85007, 'Maties Team 2', 'South Africa Regional Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=579&cid=15864', 7, 1, 0, 44062),
(90064, 89431, 'Dirichlet''s Principle', 'Arab Collegiate Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=580&cid=15864', 7, 1, 271, 797),
(90065, 89113, 'MMAW + s2++', 'Arab Collegiate Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=580&cid=15864', 7, 2, 273, 1035),
(90066, 86614, 'Alex CSE 1', 'Arab Collegiate Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=580&cid=15864', 6, 3, 234, 625),
(90067, 87824, 'Unique', 'Arab Collegiate Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=580&cid=15864', 6, 6, 269, 934),
(90068, 84655, 'Null Terminated#4Qn*&&$$$', 'Arab Collegiate Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=580&cid=15864', 5, 7, 152, 553),
(90186, 89445, 'AUS Leopards', 'Arab Collegiate Programming Contest', 'http://cm.baylor.edu/public/worldMap/publicStandings.icpc?contestId=580&cid=15864', 6, 5, 221, 755);

-- --------------------------------------------------------

--
-- Table structure for table `icpc2011_scoreboard`
--

CREATE TABLE IF NOT EXISTS `icpc2011_scoreboard` (
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
-- Table structure for table `icpc2011_submissions`
--

CREATE TABLE IF NOT EXISTS `icpc2011_submissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL,
  `lang_id` int(11) NOT NULL,
  `result` char(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2011_problems`
--

CREATE TABLE IF NOT EXISTS `icpc2011_problems` (
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
-- Table structure for table `icpc2011_tagnames`
--

CREATE TABLE IF NOT EXISTS `icpc2011_tagnames` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=20 ;

--
-- Dumping data for table `icpc2011_tagnames`
--

INSERT INTO `icpc2011_tagnames` (`id`, `name`) VALUES
(1, 'pA'),
(2, 'pB'),
(3, 'pC'),
(4, 'pD'),
(5, 'pE'),
(6, 'pF'),
(7, 'pG'),
(8, 'pH'),
(9, 'pI'),
(10, 'pJ'),
(11, 'pK');

-- --------------------------------------------------------

--
-- Table structure for table `icpc2011_tags`
--

CREATE TABLE IF NOT EXISTS `icpc2011_tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entry` int(11) NOT NULL,
  `tag` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;


-- --------------------------------------------------------

--
-- Table structure for table `icpc2011_top_coder`
--

CREATE TABLE IF NOT EXISTS `icpc2011_top_coder` (
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
-- Table structure for table `icpc2011_users`
--

CREATE TABLE IF NOT EXISTS `icpc2011_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstname` text NOT NULL,
  `lastname` text NOT NULL,
  `login` text NOT NULL,
  `password` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `icpc2011_users`
--

INSERT INTO `icpc2011_users` (`id`, `firstname`, `lastname`, `login`, `password`) VALUES
(1, 'Fredrik', 'Heintz', 'frehe', 'frehe');

-- --------------------------------------------------------

-- 
-- Create views for the current contest
CREATE VIEW teams AS SELECT * FROM icpc2011_teams;
CREATE VIEW entries AS SELECT * FROM icpc2011_entries;
CREATE VIEW facts AS SELECT * FROM icpc2011_facts;
CREATE VIEW regions AS SELECT * FROM icpc2011_regions;
CREATE VIEW scoreboard AS SELECT * FROM icpc2011_scoreboard;
CREATE VIEW tagnames AS SELECT * FROM icpc2011_tagnames;
CREATE VIEW tags AS SELECT * FROM icpc2011_tags;
CREATE VIEW top_coder AS SELECT * FROM icpc2011_top_coder;
CREATE VIEW users AS SELECT * FROM icpc2011_users;
