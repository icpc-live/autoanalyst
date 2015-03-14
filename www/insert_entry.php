<?php


$date          =& $_REQUEST["date"];
$user          =& $_REQUEST["user"];
$priority      =& $_REQUEST["priority"];
$text          =& $_REQUEST["text"];

require_once 'icat.php';
$db = init_db();

$_SESSION['entry_username'] = $user;

$result = mysqli_query($db,
    "insert into entries (contest_time, user, priority, text) values " .
    sprintf("((SELECT MAX(contest_time) AS last_submission FROM submissions), '%s', %d, '%s')",
    mysqli_escape_string($db, $user), $priority, mysqli_escape_string($db, $text))
);

if ($result) {
    print("okay");
} else {
    print("error: " . mysqli_error($db));
}

?>
