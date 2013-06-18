--------------------------------------------------------
THIS SCRIPT RESETS AND INITIALIZES ICAT

IN A FEW SECONDS IT WILL EMPTY SQL TABLES AND REMOVE GRAPHS, ETC.

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
LINK config.php TO THE RIGHT CONFIG FILE
<?php system("rm config.php"); ?>
<?php system("ln -s icpc2012/config_may16_dress_rehearsal.php config.php"); ?>

--------------------------------------------------------
MAKE SURE WE CAN CONNECT TO THE DATABASE
<?php
include('icat.php');
$db = init_db();
if (! $db) {
    print("ERROR: couldn't connect to database\n");
}
?>

--------------------------------------------------------
DUMP THE ICAT DATABASE
<?php
   include("dbconfig.php";
   $date=date('dMY_hi');
   system("mysqldump -h$dbhost -u$dbuser -p$dbpassword --database icat > icat_$date.sql");
?>

--------------------------------------------------------
TRUNCATING ALL THE RELEVANT TABLES
<?php
$to_truncate = array('icpc2012_entries', 'icpc2012_tagnames', 'icpc2012_scoreboard', 'icpc2012_submissions');
foreach ($to_truncate as $table) {
   $sql = "TRUNCATE TABLE $table";
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
