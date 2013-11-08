<?php


$date          =& $_GET["date"];
$contest_time  =& $_GET["contest_time"];
$user          =& $_GET["user"];
$priority      =& $_GET["priority"];
$text          =& $_GET["text"];

require_once 'icat.php';
$db = init_db();

$_SESSION['entry_username'] = $user;

$result = mysql_query(
    "insert into entries (contest_time, user, priority, text) values " .
    sprintf("(%d, '%s', %d, '%s')",
    $contest_time, mysql_escape_string($user), $priority, mysql_escape_string($text)),
    $db
);

if ($result) {
    print("okay");
} else {
    print("error: " . mysql_error());
}

?>
