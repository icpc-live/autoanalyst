<?php

// This file is used for exposing common data that will not change during the
// contest for both PHP and Javascript needs.

require_once 'config.php';

$db = mysqli_connect($dbhost, $dbuser, $dbpassword, $dbname);
mysqli_set_charset($db,"utf8");

$common_data = array();

// Expose configuration here to make it accessible to JavaScript code.
$common_data['config'] = $config;

function sort_judgement_data(&$arr) {
    @usort($arr, function($a, $b) {
        return $a["sortOrder"] - $b["sortOrder"];
    });
}

// Expose non-configuration elements directly instead of in $config.
$common_data['judgements'] = $config['judgements'];
unset($config['judgements']);

$result = mysqli_query($db, "SELECT problem_id AS id, problem_name AS name, color FROM problems ORDER BY problem_id");
$common_data['problems'] = array();
while ($row = mysqli_fetch_assoc($result)) {
	$common_data['problems'][$row['id']] = $row;
}

$result = mysqli_query($db, "SELECT id, team_name, school_name, school_short, country FROM teams ORDER BY id");
$common_data['teams'] = array();
while ($row = mysqli_fetch_assoc($result)) {
    $common_data['teams'][$row['id']] = $row;
}

$result = mysqli_query($db, "SELECT id, contest_name, start_time, length, freeze FROM contests WHERE id = '{$contestId}'");
while ($row = mysqli_fetch_assoc($result)) {
    $common_data['contest'] = $row;
}

/*
If this script was called (executed) from another source, return a JSON
encoding of the data. Otherwise, assume it was included in another PHP file and
print nothing.
*/
if (preg_match('/\/common_data.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    header('Content-type: application/json');
    print json_encode($common_data);
}

?>
