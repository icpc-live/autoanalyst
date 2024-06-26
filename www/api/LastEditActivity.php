<?php

require_once("../icat.php");

$db = init_db();

$min = 5;
if ( array_key_exists("min", $_GET) ) $min = $_GET["min"];
if (!is_numeric($min)) {
	die("Min must be a positive integer");
}
$from=0;
$to=$min;
$sql = "SELECT max(modify_time) AS max_time FROM edit_activity";
if ( $res=mysqli_query($db, $sql) ) {
  if ( $row = mysqli_fetch_array($res, MYSQLI_ASSOC) ) {
    if ( $row["max_time"] != "" ) {
      $to = $row["max_time"];
      $from = $to - $min;
    }
  }
}

# echo "min=$min, from=$from, to=$to";
echo edit_activity($db, $from, $to);
