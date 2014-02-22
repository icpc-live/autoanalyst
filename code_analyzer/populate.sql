-- This data should be moved as much as possible
-- to ../config.yaml or read from other sources.

INSERT INTO problem_name (problem_id, name) VALUES
( 'A', "Absurdistan Roads"      ),
( 'B', "Battle for Silver"      ),
( 'C', "Card Trick"             ),
( 'D', "Diagrams & Tableaux"    ),
( 'E', "Exponential Towers"     ),
( 'F', "First Date"             ),
( 'G', "Grachten"               ),
( 'H', "Highway of the Future"  ),
( 'I', "Infix to Prefix"        ),
( 'J', "Jingle Balls"           );

INSERT INTO problem_keywords (problem_id, keyword) VALUES
( 'A', 'absurdistan' ),
( 'A', 'roads' ),
( 'B', 'battle' ),
( 'B', 'silver' ),
( 'C', 'card' ),
( 'C', 'trick' ),
( 'C', 'cardtrick' ),
( 'D', 'diagrams' ),
( 'E', 'exponential' ),
( 'E', 'towers' ),
( 'F', 'firstdate' ),
( 'F', 'date' ),
( 'G', 'grachten' ),
( 'H', 'highway' ),
( 'I', 'infix' ),
( 'I', 'prefix' ),
( 'J', 'jingle' ),
( 'J', 'balls' );

--
-- Contest start in UTC
--
INSERT INTO analyzer_parameters (name, value) VALUES
( 'CONTEST_START', '2013-11-24 08:30:00' );
