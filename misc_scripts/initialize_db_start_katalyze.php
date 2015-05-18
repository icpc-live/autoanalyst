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
set_include_path(dirname(__FILE__) . "/../www");
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
$to_truncate = array('entries', 'submissions', 'teams');
foreach ($to_truncate as $table) {
   $sql = "DELETE FROM $table;\n";
   print("CLEARING TABLE $table;\n");
   $qr = mysqli_query($db, $sql);
   if (mysqli_error($db)) {
       print("ERROR: " . mysqli_error($db) . "\n");
   } 
}
?>

--------------------------------------------------------
GRABBING THE TEAMS FROM THE CDS AND POPULATING THE TEAMS TABLE IN THE DATABASE
<?php

system(sprintf("curl --insecure -u %s:%s %s/config/teams.tsv > teams.tsv", $config['CDS']['user'], $config['CDS']['pass'], $config['CDS']['baseurl']));
$f = fopen("teams.tsv", "r");
$header = fgetcsv($f, 0, "\t"); # ignore the header...

$team_tsv_mapping = Array(
    'team_id'         => 0,
    'team_name'       => 3,
    'school_name'     => 4,
    'school_short'    => 5,
    'country'         => 6,
    'institution_id'  => 7
);

while ($row = fgetcsv($f, 0, "\t")) {
    $team_id         =  $row[$team_tsv_mapping['team_id']];
    $team_name       =  $row[$team_tsv_mapping['team_name']];
    $school_name     =  $row[$team_tsv_mapping['school_name']];
    $school_short    =  $row[$team_tsv_mapping['school_short']];
    $country         =  $row[$team_tsv_mapping['country']];
    $institution_id  =  preg_replace('/INST-/', '', $row[$team_tsv_mapping['institution_id']]);

    $stmt = mysqli_prepare($db, "INSERT INTO teams (team_id, team_name, school_name, school_short, country, institution_id) "
        . " VALUES (?, ?, ?, ?, ?, ?)");
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN PREPARE: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_bind_param($stmt, "dssssd", $team_id, $team_name, $school_name, $school_short, $country, $institution_id);
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN BIND: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_execute($stmt);
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN EXECUTE: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_close($stmt);
}

?>
