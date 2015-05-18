<?php


$date          =& $_REQUEST["date"];
$user          =& $_REQUEST["user"];
$priority      =& $_REQUEST["priority"];
$text          =& $_REQUEST["text"];
if (isset($_REQUEST["submission"])) {
    $sid       =  intval($_REQUEST["submission"]);
} else {
    $sid       =  null;
}

require_once 'icat.php';
$db = init_db();

$_SESSION['entry_username'] = $user;

$stmt = mysqli_prepare($db, 'INSERT INTO entries (contest_time, user, priority, text, submission_id) VALUES ((SELECT MAX(contest_time) AS last_submission FROM submissions), ?, ?, ?, ?)');

mysqli_stmt_bind_param($stmt, 'sisi', $user, $priority, $text, $sid);

mysqli_stmt_execute($stmt);

$error = mysqli_stmt_error($stmt);
if ($error === '') {
    print("okay");
} else {
    print("error: " . $error);
}

mysqli_stmt_close($stmt);
