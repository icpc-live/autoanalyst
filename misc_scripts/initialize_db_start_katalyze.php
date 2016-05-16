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
GRABBING THE CONTEST INFO FROM THE CDS AND STORING IT IN THE DATABASE
<?php

system(sprintf("curl --insecure -u %s:%s %s/config/contest.yaml > contest.yaml", $config['CDS']['user'], $config['CDS']['pass'], $config['CDS']['baseurl']));
$contest = Spyc::YAMLLoad("contest.yaml");

// Use Unix timestamp to work around unknown MySQL server timezone.
$contest_start = date_create_from_format('Y-m-d*G:i:sT', $contest['start-time'],
                                         new DateTimeZone('UTC'));
$contest_start = $contest_start->getTimestamp();

function parse_duration($str)
{
	sscanf($str, '%d:%d:%d', $h, $m, $s);
	return 3600*$h + 60*$m + $s;
}

$stmt = mysqli_prepare($db, "INSERT INTO contests (contest_name, start_time, length, freeze) "
                       . " VALUES (?, FROM_UNIXTIME(?), ?, ?)");
if (mysqli_stmt_error($stmt)) { printf("ERROR IN PREPARE: %s\n", mysqli_stmt_error($stmt)); }

mysqli_stmt_bind_param($stmt, "ssdd", $contest['name'], $contest_start,
                       parse_duration($contest['duration']),
                       parse_duration($contest['scoreboard-freeze-length']));

if (mysqli_stmt_error($stmt)) { printf("ERROR IN BIND: %s\n", mysqli_stmt_error($stmt)); }
mysqli_stmt_execute($stmt);
if (mysqli_stmt_error($stmt)) { printf("ERROR IN EXECUTE: %s\n", mysqli_stmt_error($stmt)); }
mysqli_stmt_close($stmt);

?>

--------------------------------------------------------
GRABBING THE PROBLEMS FROM THE CDS AND POPULATING THE PROBLEMS TABLE IN THE DATABASE
<?php

system(sprintf("curl --insecure -u %s:%s %s/config/problemset.yaml > problemset.yaml", $config['CDS']['user'], $config['CDS']['pass'], $config['CDS']['baseurl']));
$problems = Spyc::YAMLLoad("problemset.yaml");

// FIXME: problemset.yaml only has the problem short names, not the
// full names. For this we should query the JSON scoreboard API call,
// but this may only be available after contest start.

foreach ( $problems['problems'] as $problem ) {

    $stmt = mysqli_prepare($db, "INSERT INTO problems (problem_id, problem_name, color) "
	                       . " VALUES (?, ?, ?)");
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN PREPARE: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_bind_param($stmt, "sss", $problem['letter'], $problem['short-name'], $problem['rgb']);
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN BIND: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_execute($stmt);
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN EXECUTE: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_close($stmt);
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
    #'institution_id'  => 7
);

while ($row = fgetcsv($f, 0, "\t")) {
    $team_id         =  $row[$team_tsv_mapping['team_id']];
    $team_name       =  $row[$team_tsv_mapping['team_name']];
    $school_name     =  $row[$team_tsv_mapping['school_name']];
    $school_short    =  $row[$team_tsv_mapping['school_short']];
    $country         =  $row[$team_tsv_mapping['country']];
    #$institution_id  =  preg_replace('/INST-/', '', $row[$team_tsv_mapping['institution_id']]);

    $stmt = mysqli_prepare($db, "INSERT INTO teams (id, team_id, team_name, school_name, school_short, country) "
        . " VALUES (?, ?, ?, ?, ?, ?)");
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN PREPARE: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_bind_param($stmt, "ddssss", $team_id, $team_id, $team_name, $school_name, $school_short, $country);
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN BIND: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_execute($stmt);
    if (mysqli_stmt_error($stmt)) { printf("ERROR IN EXECUTE: %s\n", mysqli_stmt_error($stmt)); }
    mysqli_stmt_close($stmt);
}
