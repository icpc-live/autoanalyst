--------------------------------------------------------
THIS SCRIPT STARTS KATALYZER

IN A FEW SECONDS IT WILL EMPTY SQL TABLES, 
REMOVE GRAPHS, START KATALYZE, ETC.

IF YOU DON'T WANT TO DO THIS, *KILL IT NOW*

ALSO, REMEMBER TO UPDATE "config.php"
WITH THE CORRECT INFORMATION FOR THIS CONTEST 
(IF YOU HAVEN'T ALREADY)
--------------------------------------------------------
<?php
for ($i = 5; $i >= 1; --$i) {
    sleep(1);
    print("starting in $i seconds...\n");
}
?>

--------------------------------------------------------
UPDATING TO LATEST SVN
<?php system("svn update"); ?>

--------------------------------------------------------
LINK TO THIS YEAR'S FILES
<?php system("rm icpc"); ?>
<?php system("ln -s icpc2013 icpc"); ?>

--------------------------------------------------------
MAKE SURE WE CAN CONNECT TO THE DATABASE
<?php
require_once 'icat.php';
$db = init_db();
if (! $db) {
    print("ERROR: couldn't connect to database\n");
}
?>

--------------------------------------------------------
TRUNCATING ALL THE RELEVANT TABLES
<?php
$to_truncate = array('icpc2013_entries', 'icpc2013_scoreboard', 'icpc2013_submissions');
foreach ($to_truncate as $table) {
   $sql = "truncate table $table";
   print("TRUNCATING TABLE $table\n");
   $qr = mysql_query($sql, $db);
}
?>

--------------------------------------------------------
POPULATING TEAMS IN SCOREBOARD
<?
$sql = "SELECT id, school_name FROM `teams` order by id";
$qr = mysql_query($sql, $db);
$sql = "insert into scoreboard (team_id) values ";
$first = true;
while ($row = mysql_fetch_assoc($qr)) {
	if (! $first) { $sql .= ", "; }
	$sql .= "(" . $row["id"] . ")";
	$first = false;
}
printf("populating scoreboard with teams:\n%s\n", $sql);
mysql_query($sql, $db);
?>


--------------------------------------------------------
INITIALIZE CODEALYZER
<?php system("code_analyzer/util/reset.sh"); ?>


--------------------------------------------------------
REMOVING GRAPHS IN OUTPUT DIRECTORY
<?php system("rm -rf katalyze/output"); ?>

--------------------------------------------------------
BUILDING KATALYZE
<?php system("make -C katalyze"); ?>


--------------------------------------------------------
STARTING KATALYZE
<?php system("/bin/bash start_all_katalyzers.sh"); ?>
