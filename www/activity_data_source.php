<?php
require_once "icat.php";

function where_clause($team_ids, $problem_ids, $conditions = array()) {
    // determine if we should limit this to one team
    $team_ids = array_unique(array_map('intval', $team_ids));
    if (!empty($team_ids)) {
        $conditions[] = "team_id in (" . implode(',', $team_ids) . ")";
    }

    // remove everything from the array but single upper case chars A-Z
    $problem_ids = array_values(array_filter($problem_ids, function ($elem) {
                return preg_match('/^[A-Z]$/', $elem);
            }));
    if (!empty($problem_ids)) {
        $conditions[] = 'problem_id IN ("' . preg_replace('/,/', '","', implode(',', $problem_ids)) . '")';
    }
    return $conditions ? 'WHERE ' . implode(' AND ', $conditions) : '';
}


##########################################################
# Edit activity
##########################################################

function get_edit_activity($db, $team_ids, $problem_ids, $bin_minutes, $until_minutes) {
    # where clause for edit_activity_problem
    $where_clause_edit_activity_problem = where_clause($team_ids, $problem_ids);

    # Grab the edit_activity_problem rows aggregated by time, binning by every $bin_minutes
    # seconds. Convert to contest time (i.e. minutes between 1-300). 
    $rows = mysql_query_cacheable($db,
        /*
        // This query gives all edits (even if a single team makes many edits)
        "SELECT FLOOR(modify_time / $bin_minutes) * $bin_minutes AS contest_time_binned "
        . ", problem_id, COUNT(*) AS count "
        . "FROM edit_activity_problem "
        . "$where_clause_edit_activity_problem "
        . "GROUP BY problem_id, contest_time_binned "
        . (isset($until_minutes) ? (" HAVING contest_time_binned < " . intval($until_minutes) . " ") : "")
        . "ORDER BY problem_id, contest_time_binned "
        */
        // This query gives per-problem counts, grouped by team
        "SELECT contest_time_binned, problem_id, count(*) as count FROM "
        . " (SELECT FLOOR(modify_time / $bin_minutes) * " . intval($bin_minutes) . " AS contest_time_binned, "
        . " problem_id, team_id, COUNT(*) AS count "
        . " FROM edit_activity_problem "
        . $where_clause_edit_activity_problem
        . " GROUP BY problem_id, contest_time_binned, team_id "
        . " HAVING contest_time_binned >= 0 and contest_time_binned <= 300 "
        . (isset($until_minutes) ? (" AND contest_time_binned < " . intval($until_minutes) . " ") : "")
        . " ORDER BY problem_id, contest_time_binned, team_id "
        . " ) as FOO "
        . " group by problem_id, contest_time_binned "
    );

    $edit_bins = array();
    foreach ($rows as $row) {
        $count = intval($row["count"]);
        $edit_bins[strtoupper($row["problem_id"])][intval($row["contest_time_binned"])] = $count;
    }
    return $edit_bins;
}

function get_max_problems_per_bin($edit_bins) {
    // need to know how many problems are solved in any one binned time interval
    if (empty($edit_bins))
        return 1;
    else
        return max(array_map('max', $edit_bins));
}

##########################################################
# Submission activity
##########################################################

function get_num_at_problem_minute($db, $team_ids, $problem_ids, $until_minutes) {
    # where clause for submissions
    if (isset($until_minutes)) {
        $where_clause_submissions = where_clause($team_ids, $problem_ids,
                                                 array('contest_time < ' . intval($until_minutes)));
    } else {
        $where_clause_submissions = where_clause($team_ids, $problem_ids);
    }

    # get the number of submissions of each problem per minute
    $sql = "select concat(problem_id, '_', contest_time) as problem_minute, count(*) as num_at_problem_minute " .
           " from submissions $where_clause_submissions group by problem_id, contest_time";
    $rows = mysql_query_cacheable($db, $sql); # grab the submission activity
    $num_at_problem_minute = array();
    foreach ($rows as $row) {
        $num_at_problem_minute[$row["problem_minute"]] = intval($row["num_at_problem_minute"] * 1.4); // scale so that we don't hit the ceiling
    }
    return $num_at_problem_minute;
}

function get_submission_activity($db, $team_ids, $problem_ids, $until_minutes) {
    # where clause for submissions
    if (isset($until_minutes)) {
        $where_clause_submissions = where_clause($team_ids, $problem_ids,
                                                 array('contest_time < ' . intval($until_minutes)));
    } else {
        $where_clause_submissions = where_clause($team_ids, $problem_ids);
    }

    $sql ="SELECT * FROM submissions "
        . "$where_clause_submissions "
        . "ORDER BY contest_time ASC, result ASC ";

    $rows = mysql_query_cacheable($db, $sql); # grab the submission activity
    $submissions = array();
    foreach ($rows as $row) {
        $submissions[$row["result"]][] = array(
            'problem_id' => strtoupper($row["problem_id"]),
            'contest_time' => intval($row["contest_time"]),
            'team_id' => intval($row["team_id"]),
            'lang_id' => $row['lang_id'],
            'submission_id' => $row['submission_id']
            );
    }
    return $submissions;
}

function get_activity_data($team_id, $problem_id, $bin_minutes) {
    global $common_data;
    $db = init_db();

    // IDEAS:
    //  - turn on/off certain result types

    $edit_bins = get_edit_activity($db, $team_id, $problem_id, $bin_minutes);
    $max_problems_per_bin = get_max_problems_per_bin($edit_bins);

    $num_at_problem_minute = get_num_at_problem_minute($db, $team_id, $problem_id);
    $submissions = get_submission_activity($db, $team_id, $problem_id);

    ##########################################################
    # Create the datasets in javascript
    ##########################################################

    $datasets = array();

    // Create the histograms of edit_activity_problem
    $baseline_counter = 0;
    $baseline_per_problem = array();
    if (!empty($problem_id)) {
        $problems_used = $problem_id;
    } else {
        $problems_used = array_keys($common_data['problems']);
    }


    foreach ($problems_used as $problem_id) {
        $problem_bins = isset($edit_bins[$problem_id]) ? $edit_bins[$problem_id] : array();
        $dataset = array();
        $dataset["bars"] = array("show" => true, "fill" => 1, "fillColor" => false, "barWidth" => intval($bin_minutes * 0.9));
        $dataset["legend"] = array("show" => false);
        $dataset["data"] = array();
        foreach ($problem_bins as $time => $count) {
            $dataset["data"][] = array($time, $baseline_counter + $count, $baseline_counter);
        }
        $datasets[] = $dataset;
        $baseline_per_problem[$problem_id] = $baseline_counter;
        $baseline_counter += $max_problems_per_bin;
    }

    // Create series of points for submissions, one per type of result
    $point_options_by_result = $common_data['judgements'];
    foreach (array_keys($point_options_by_result) as $result) {
        $point_options_by_result[$result]["shadowSize"] = 0;
        $point_options_by_result[$result]["points"] = array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1);
    }
    $point_options_by_result['AC']["points"]["radius"] = 3;


    $problem_minute_counter = array();
    foreach ($submissions as $result => $result_submissions) {
        $dataset = array();
        $dataset["legend"] = array("show" => true);
        $dataset["lines"] = array("show" => false);
        foreach ($point_options_by_result[$result] as $k => $v) {
            $dataset[$k] = $v;
        }
        $dataset["data"] = array();
        foreach ($result_submissions as $sub) {
            $problem_id = strtoupper($sub["problem_id"]);
            $problem_minute = sprintf("%s_%d", $problem_id, $sub["contest_time"]);
            if (! isset($problem_minute_counter[$problem_minute])) {
                $problem_minute_counter[$problem_minute] = 0;
            }
            $n = isset($num_at_problem_minute[$problem_minute]) ? $num_at_problem_minute[$problem_minute] : 0;
            $scale = max(10, $n);
            $dataset["data"][] = array(
                $sub["contest_time"],
                // avoid problems with misconfigurations by not assuming that we know all IDs
                (isset($baseline_per_problem[$problem_id]) ? $baseline_per_problem[$problem_id] : 0)
                + $problem_minute_counter[$problem_minute] * $max_problems_per_bin / $scale
            );
            $problem_minute_counter[$problem_minute]++;
        }
        $dataset["submissionInfo"] = array();
        foreach ($result_submissions as $sub) {
            $dataset["submissionInfo"][] = array("team_id" => $sub["team_id"], "problem_id" => $sub["problem_id"], "lang_id" => $sub["lang_id"], "submission_id" => $sub["submission_id"]);
        }
        $datasets[] = $dataset;
    }

    sort_judgement_data($datasets);

    $response = array(
        "problems_used" => $problems_used,
        "max_problems_per_bin" => $max_problems_per_bin,
        "flot_data" => $datasets
    );

    return $response;
}

function csv_to_string_array($csv) {
    return array_filter(explode(',', $csv));
}

function string_to_alpha_array($string) {
    return preg_split('//', strtoupper($string));
}

if (preg_match('/\/activity_data_source.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    $team_id = isset($_GET["team_id"]) ? csv_to_string_array($_GET["team_id"]) : array();
    $problem_id = isset($_GET["problem_id"]) ? string_to_alpha_array($_GET["problem_id"]) : array();
    // the resolution of the time-based bins -- FIXME -- make this parameter work from the interface
    $bin_minutes = 5;
    if (isset($_GET['bin_minutes']) && intval($_GET['bin_minutes']) > 0)
      $bin_minutes = intval($_GET['bin_minutes']);
    $response = get_activity_data($team_id, $problem_id, $bin_minutes);
    header('Content-type: application/json');
    print json_encode($response);
}
