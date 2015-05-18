<?php


if (isset($_REQUEST["contest_time"])) {
    $ctime     =  intval($_REQUEST["contest_time"]);
} else {
    $ctime     =  null;
}
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

// when picking contest_time:
// 1. choose the contest_time parameter if present
// 2. choose the maximum of contest_time in submissions if present
// 3. otherwise choose 0

$stmt = mysqli_prepare($db, 'INSERT INTO entries (contest_time, user, priority, text, submission_id) VALUES ((SELECT COALESCE(?, MAX(contest_time), 0) AS last_submission FROM submissions), ?, ?, ?, ?)');

mysqli_stmt_bind_param($stmt, 'isisi', $ctime, $user, $priority, $text, $sid);

mysqli_stmt_execute($stmt);

$error = mysqli_stmt_error($stmt);
if ($error === '') {
    print("okay");
} else {
    print("error: " . $error);
}

mysqli_stmt_close($stmt);
