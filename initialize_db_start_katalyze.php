--------------------------------------------------------
THIS SCRIPT RESETS AND INITIALIZES ICAT AND STARTS KATALYZER

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
DUMP THE ICAT DATABASE
<?php
   require_once 'icpc/config.php';
   $date=date('dMY_hi');
   system("mysqldump -h$dbhost -u$dbuser -p$dbpassword --database icat > icat_$date.sql");
?>

--------------------------------------------------------
TRUNCATING ALL THE RELEVANT TABLES
<?php
$to_truncate = array('icpc2013_entries', 'icpc2013_submissions');
foreach ($to_truncate as $table) {
   $sql = "TRUNCATE TABLE $table";
   print("TRUNCATING TABLE $table\n");
   $qr = mysql_query($sql, $db);
}
?>

--------------------------------------------------------
UPDATING THE REPOSITORY
<?php system("git pull"); ?>

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
