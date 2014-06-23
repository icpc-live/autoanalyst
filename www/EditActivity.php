<?php

require_once "icat.php";

$db = init_db();


$from=0;
if ( array_key_exists("from", $_GET ) ) $from = $_GET["from"];
$to=999999;
if ( array_key_exists("to", $_GET ) ) $to = $_GET["to"];

$to = min(240,$to);

echo edit_activity($db, $from, $to);

?>
