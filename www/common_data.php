<?php

// This file is used for exposing common data that will not change during the
// contest for both PHP and Javascript needs.

require_once 'config.php';

$db = mysqli_connect($dbhost, $dbuser, $dbpassword, $dbname);
mysqli_set_charset($db,"utf8");

$COMMON_DATA = array();

$COMMON_DATA["BALLOON_COLORS"] = array();
$COMMON_DATA["PROBLEM_ID_TO_NAME"] = array();

foreach ( $config['problems'] as $key => $data ) {
	$COMMON_DATA["BALLOON_COLORS"][$key] = $data['color'];
	$COMMON_DATA["PROBLEM_ID_TO_NAME"][$key] = $data['name'];
}

$COMMON_DATA["JUDGEMENTS"] = $config['judgements'];

function sort_judgement_data(&$arr) {
    @usort($arr, function($a, $b) {
        return $a["sortOrder"] - $b["sortOrder"];
    });
}

$result = mysqli_query($db, "SELECT id, team_name, school_name, school_short, country FROM teams ORDER BY id");
$COMMON_DATA['TEAMS'] = array();
while ($row = mysqli_fetch_assoc($result)) {
    $COMMON_DATA['TEAMS'][$row['id']] = $row;
}

$COMMON_DATA["CODEACTIVITY"] = $config['codeActivity'];

/*
If this script was called (executed) from another source, return a JSON
encoding of the data. Otherwise, assume it was included in another PHP file and
print nothing.
*/
if (preg_match('/\/common_data.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    header('Content-type: application/json');
    print json_encode($COMMON_DATA);
}

?>
