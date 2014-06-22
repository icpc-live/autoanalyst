<?php

require_once "icat.php";

$db = init_db();

$min = 5;
if ( $_GET["min"] ) $min = $_GET["min"];

$from=0;
$to=$min;
$sql = "SELECT max(modify_time) AS max_time FROM icpc2014_edit_activity";
if ( $res=mysqli_query($db, $sql) ) {
  if ( $row = mysqli_fetch_array($res, MYSQLI_ASSOC) ) {
    $to = $row["max_time"];
    $from = $to - $min;
  }
}

#echo "min=$min, from=$from, to=$to";
echo edit_activity($db, $from, $to);

?>
