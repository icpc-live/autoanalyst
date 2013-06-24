<?php

// This file is used for exposing common data that will not change during the 
// contest for both PHP and Javascript needs.

require_once 'config.php';

mysql_connect($dbhost, $dbuser, $dbpassword);
mysql_select_db("icat");
mysql_set_charset("utf8");

$COMMON_DATA = array();

$result = mysql_query("SELECT id, team_name, school_name, school_short, country FROM teams ORDER BY id");
$COMMON_DATA['TEAMS'] = array();
while ($row = mysql_fetch_assoc($result)) {
    $COMMON_DATA['TEAMS'][$row['id']] = $row;
}


// 2013 ballon colors
$COMMON_DATA["BALLOON_COLORS"] = array(
        "A" => "#FF0000",
        "B" => "#9500AD",
        "C" => "#F5F500",
        "D" => "#000000",
        "E" => "#FFFFFF",
        "F" => "#FBA1B2",
        "G" => "#FF8000",
        "H" => "#A4A4A4",
        "I" => "#0080EB",
        "J" => "#FF54E8",
        "K" => "#03DD3E",
        "L" => "#808080"
    );

// TODO: add problem names
$COMMON_DATA["PROBLEM_ID_TO_NAME"] = array(
        "A" => "Preludes",
        "B" => "Limited Correspondence",
        "C" => "Heliocentric",
        "D" => "In or Out",
        "E" => "Polish Notation",
        "F" => "Prime Spiral",
        "G" => "Sierpinski circumference",
        "H" => "Tower of Powers 2: Power Harder",
        "I" => "Matrix Inverse",
        "J" => "Reversing Roads",
        "K" => "Counting Stars",
        "L" => "Statistics",
    );

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
