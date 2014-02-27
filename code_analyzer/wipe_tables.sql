-- Clear out the tables used by the code analyzer.  Used mostly
-- during development / testing.

DELETE FROM file_to_problem;
DELETE FROM edit_activity;
DELETE FROM file_modtime;
DELETE FROM edit_latest;
DELETE FROM analyzer_parameters;
DELETE FROM problem_name;
DELETE FROM problem_keywords;
DELETE FROM team_strips;
