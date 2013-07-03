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
        "A" => "#FF0000", // Red
        "B" => "#000000", // Black
        "C" => "#40E0D0", // Turquoise
        "D" => "#FFFF00", // Yellow
        "E" => "#FF1493", // Dark Pink
        "F" => "#0000FF", // Blue
        "G" => "#800020", // Burgundy
        "H" => "#00FF00", // Green
        "I" => "#FFA500", // Orange
        "J" => "#FFC0CB", // Pink
        "K" => "#008080", // Teal
    );

$COMMON_DATA["JUDGEMENTS"] = array(
    'AC'   => array("label" => 'AC' , "label_long" => 'Accepted'              , "color" => '#10a010' /* green  */   , "sortOrder" => 1),
    'WA'   => array("label" => 'WA' , "label_long" => 'Wrong Answer'          , "color" => '#333333' /* dark gray */, "sortOrder" => 2),
    'TLE'  => array("label" => 'TLE', "label_long" => 'Time Limit Exceeded'   , "color" => '#ff0000' /* red    */   , "sortOrder" => 3),
    'RTE'  => array("label" => 'RTE', "label_long" => 'Run Time Error'        , "color" => '#ffaa00' /* orange */   , "sortOrder" => 4),
    '(CE)' => array("label" => 'CE' , "label_long" => 'Compile Error'         , "color" => '#ffff00' /* yellow */   , "sortOrder" => 5),
    '(IF)' => array("label" => 'IF' , "label_long" => 'Illegal Function'      , "color" => '#0000ff' /* blue   */   , "sortOrder" => 6),
    // The remaining three judgements (MLE, OLE, PE) are not used anymore, I think. (Hamerly 2013)                                   
    'MLE'  => array("label" => 'MLE', "label_long" => 'Memory Limit Exceeded' , "color" => '#ffffaa' /* pink   */   , "sortOrder" => 7),
    'OLE'  => array("label" => 'OLE', "label_long" => 'Output Limit Exceeded' , "color" => '#ff00ff' /* purple */   , "sortOrder" => 8),
    'PE'   => array("label" => 'PE' , "label_long" => 'Presentation Error'    , "color" => '#aaaaaa' /* gray   */   , "sortOrder" => 9),
);

function sort_judgement_data(&$arr) {
    @usort($arr, function($a, $b) {
        return $a["sortOrder"] - $b["sortOrder"];
    });
}


// TODO: add problem names
$COMMON_DATA["PROBLEM_ID_TO_NAME"] = array(
        "A" => "Self-Assembly",
        "B" => "Hey, Better Bettor",
        "C" => "Surely You Congest",
        "D" => "Factors",
        "E" => "Harvard",
        "F" => "Low Power",
        "G" => "Map Tiles",
        "H" => "Maтрëшкa",
        "I" => "Pirate Chest",
        "J" => "Pollution Solution",
        "K" => "Up a Tree"
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
