<?php

require_once("../icat.php");

$db = init_db();


$from=0;
if ( array_key_exists("from", $_GET ) ) $from = $_GET["from"];
if(!is_numeric($from)) {
	die("from must be a positive integer");
}
$to=999999;
if ( array_key_exists("to", $_GET ) ) $to = $_GET["to"];
if(!is_numeric($to)) {
	die("to must be a positive integer");
}

$to = min(240,$to);

echo edit_activity($db, $from, $to);
