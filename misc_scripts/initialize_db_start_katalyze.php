--------------------------------------------------------
THIS SCRIPT RESETS AND INITIALIZES ICAT

IN A FEW SECONDS IT WILL EMPTY SQL TABLES 

IF YOU DON'T WANT TO DO THIS, *KILL IT NOW*

ALSO, REMEMBER TO UPDATE "config.yaml"
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
MAKE SURE WE CAN CONNECT TO THE DATABASE
<?php
set_include_path("/home/icpclive/autoanalyst/www"); # FIXME -- this may change year to year
require_once 'icat.php';
$db = init_db();
if (! $db) {
    print("ERROR: couldn't connect to database\n");
}
?>

--------------------------------------------------------
DUMP THE ICAT DATABASE
<?php
   require_once 'config.php';
   $date=date('dMY_hi');
   system("mysqldump -h$dbhost -u$dbuser -p$dbpassword --database icat > icat_backup_$date.sql");
?>

--------------------------------------------------------
TRUNCATING ALL THE RELEVANT TABLES
<?php
$to_truncate = array('entries', 'submissions');
foreach ($to_truncate as $table) {
   $sql = "CLEARING TABLE $table";
   print("DELETE FROM TABLE $table\n");
   $qr = mysqli_query($db, $sql);
}
?>
