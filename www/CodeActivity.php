<?php

require_once 'activity_data_source.php';

$db = init_db();

$team_ids = isset($_GET['team_id']) ? csv_to_string_array($_GET['team_id']) : array();
$problem_ids = isset($_GET['problem_id']) ? string_to_alpha_array($_GET['problem_id']) : array();
$granularity = isset($_GET['granularity']) ? intval($_GET['granularity']) : 5;
$granularity = max($granularity, 1);

$response = array();
$response['granularity'] = $granularity;

$problems = array();

$submissions = get_submission_activity($db, $team_ids, $problem_ids);
$sub = array();
foreach ($submissions as $judgment => $submission_list) {
    foreach ($submission_list as $submission) {
        if (!isset($sub[$submission['problem_id']]))
            $sub[$submission['problem_id']] = array();
        if (!isset($sub[$submission['problem_id']][$judgment]))
            $sub[$submission['problem_id']][$judgment] = array();
        $sub[$submission['problem_id']][$judgment][]
            = array(
                    'contest_time' => $submission['contest_time'],
                    'team_id' => $submission['team_id'],
                    'lang_id' => $submission['lang_id'],
                    'submission_id' => $submission['submission_id'],
                    );
    }
}

$edit_activity = get_edit_activity($db, $team_ids, $problem_ids, $granularity);
foreach ($edit_activity as $problem => $raw_activity) {
    $activity = array();
    foreach ($raw_activity as $minute => $edits) {
        $activity[] = array(
                            'contest_time' => $minute,
                            'num_edited_lines' => $edits,
                            );
    }
    $problems[] = array(
                        'problem_id' => $problem,
                        'edit_summary' => $activity,
                        'submissions' => isset($sub[$problem]) ? $sub[$problem] : array(),
                        );
}

$response['problems'] = $problems;

echo json_encode($response);
