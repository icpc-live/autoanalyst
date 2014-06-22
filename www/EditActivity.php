<?php

require_once "icat.php";

$db = init_db();

$from=0;
if ( $_GET["from"] ) $from = $_GET["from"];
$to=999999;
if ( $_GET["to"] ) $to = $_GET["to"];

$to = min(240,$to);

echo edit_activity($db, $from, $to);

?>
