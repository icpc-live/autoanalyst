<?php

date_default_timezone_set("Europe/Warsaw");

require_once "edit_activity.php";


function init_db()
{
  $db=mysqli_connect("db.ida.liu.se", "impa", "impa9c4f", "impa");

  // Check connection
  if (mysqli_connect_errno()) {
    echo "Failed to connect to MySQL: " . mysqli_connect_error();
  }
  
  return $db;
}


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
