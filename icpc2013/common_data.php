<?php

// This file is used for exposing common data that will not change during the 
// contest for both PHP and Javascript needs.


require_once 'dbconfig.php';
#require_once 'icat.php';

mysql_connect($dbhost, $dbuser, $dbpassword);
mysql_select_db("icat");

$COMMON_DATA = array();

$result = mysql_query("SELECT id, team_name, school_name, school_short, country FROM teams ORDER BY id");
$COMMON_DATA['TEAMS'] = array();
while ($row = mysql_fetch_assoc($result)) {
    $COMMON_DATA['TEAMS'][$row['id']] = $row;
}


// TODO: create tables to put the balloon colors and problem names into the database
$COMMON_DATA["BALLOON_COLORS"] = array(
        "A" => "#ffffff",
        "B" => "#ff1546",
        "C" => "#7818a4",
        "D" => "#000000",
        "E" => "#31e113",
        "F" => "#f3b3c8",
        "G" => "#ff8315",
        "H" => "#c6c6c6",
        "I" => "#caf727",
        "J" => "#f3c13b",
        "K" => "#00a0dc",
        "L" => "#f7f417"
    );

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
