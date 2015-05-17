<?php

require_once('../common_data.php');
require_once('../activity_data_source.php');

function problem_id_column($submissions) {
    // pick the problem_id column (can be done with array_column in PHP >= 5.5.0)
    $problems = array();
    foreach ($submissions as $submission) {
        $problems[] = $submission['problem_id'];
    }
    return array_unique($problems);
}

$db = init_db();

if (!isset($_GET['team_id'])) {
    echo 'missing team_id parameter';
    return;
}
$team_id = intval($_GET['team_id']);

// get a list of submissions to eventually solved problems
$submissions = get_submission_activity($db, array($team_id), array(), $COMMON_DATA['CODEACTIVITY']['scoreboardFreezeMinutes']);
$accepted_submissions = isset($submissions['AC']) ? $submissions['AC'] : array();
$accepted_problems = problem_id_column($accepted_submissions);

$edit_activity = get_edit_activity($db, array($team_id), array(), 1, $COMMON_DATA['CODEACTIVITY']['scoreboardFreezeMinutes']);

// remove solved problems from the list
foreach ($accepted_problems as $problem) {
    unset($edit_activity[$problem]);
}

// create an array of the problems that were edited last
$last_edited_problems = array();
$last_edited_time = -1;
foreach ($edit_activity as $problem_id => $submission_times) {
    $edited_time = max(array_keys($submission_times));
    if ($edited_time > $last_edited_time) {
        $last_edited_problems = array($problem_id);
        $last_edited_time = $edited_time;
    } elseif ($edited_time == $last_edited_time) {
        $last_edited_problems[] = $problem_id;
    }
}
sort($last_edited_problems);

if ($last_edited_time < 0) {
    $last_edited_time = null;
}

echo json_encode(array('team_id' => $team_id,
                       'problem_id' => $last_edited_problems,
                       'last_edit_time' => $last_edited_time,
                       ));
return;
