<?php
require_once "icat.php";

function get_activity_data($team_id, $problem_id) {
    global $COMMON_DATA;
    $db = init_db();

    // IDEAS:
    //  - turn on/off certain result types

    // the resolution of the time-based bins -- FIXME -- make this a parameter
    $G_BIN_MINUTES = 5;

    // determine if we should limit this to one team
    $where_conditions = array();
    if (isset($team_id) && $team_id != "") {
        # FIXME -- check that this SQL is safe. Written on a plane without php references. Should be simpler.
        $team_ids = array_unique(preg_split("/,/", $team_id));
        $team_ids_safe = array();
        foreach ($team_ids as $tid) {
            if (preg_match("/^[0-9]+$/", $tid)) {
                $team_ids_safe[] = $tid;
            }
        }
        $team_ids_safe = implode(",", $team_ids_safe);
        $where_conditions[] = "team_id in (" . $team_ids_safe . ")";
    }

    if (isset($problem_id) && $problem_id != "") {
        # FIXME -- IMPROVE THIS TERRIBLE PHP -- WRITTEN ON A PLANE WITHOUT AN API REFERENCE
        $problem_id = preg_split("//", preg_replace("/[^a-z]/i", "", $problem_id));
        array_pop($problem_id);
        array_shift($problem_id);
        $problem_id = array_unique($problem_id);
        $problem_id = implode(",", $problem_id);
        $where_conditions[] = 'problem_id IN ("' . preg_replace('/,/', '","', $problem_id) . '")';
    }

    # where clause for submissions
    $where_clause_submissions = $where_conditions ? "WHERE " . implode(" AND ", $where_conditions) : "";

    # where clause for edit_activity
    $where_clause_edit_activity = $where_conditions ? "WHERE " . implode(" AND ", $where_conditions) : "";

    ##########################################################
    # Edit activity
    ##########################################################

    # Grab the edit_activity rows aggregated by time, binning by every $G_BIN_MINUTES 
    # seconds. Convert to contest time (i.e. minutes between 1-300). 
    $rows = mysql_query_cacheable(
        /*
        // This query gives all edits (even if a single team makes many edits)
        "SELECT FLOOR(modify_time / $G_BIN_MINUTES) * $G_BIN_MINUTES AS contest_time_binned "
        . ", problem_id, COUNT(*) AS count "
        . "FROM edit_activity "
        . "$where_clause_edit_activity "
        . "GROUP BY problem_id, contest_time_binned "
        . "ORDER BY problem_id, contest_time_binned "
        */
        // This query gives per-problem counts, grouped by team
        "SELECT contest_time_binned, problem_id, count(*) as count FROM "
        . " (SELECT FLOOR(modify_time / $G_BIN_MINUTES) * $G_BIN_MINUTES AS contest_time_binned, "
        . " problem_id, team_id, COUNT(*) AS count "
        . " FROM edit_activity  "
        . $where_clause_edit_activity
        . " GROUP BY problem_id, contest_time_binned, team_id "
        . " HAVING contest_time_binned >= 0 and contest_time_binned <= 300 "
        . " ORDER BY problem_id, contest_time_binned, team_id "
        . " ) as FOO "
        . " group by problem_id, contest_time_binned "
    );

    # go through all the rows, save each, and determine the maximum counts (for 
    # scaling of the plot)
    $edit_bins = array();
    $max_problems_per_bin = 0; // need to know how many problems are solved in any one binned time interval
    foreach ($rows as $row) {
        $count = intval($row["count"]);
        $edit_bins[strtoupper($row["problem_id"])][intval($row["contest_time_binned"])] = $count;
        $max_problems_per_bin = max($max_problems_per_bin, $count);
    }
    if ($max_problems_per_bin == 0) {
        $max_problems_per_bin = 1;
    }

    ##########################################################
    # Submission activity
    ##########################################################

    # get the number of submissions of each problem per minute
    $sql = "select concat(problem_id, '_', contest_time) as problem_minute, count(*) as num_at_problem_minute " .
           " from submissions $where_clause_submissions group by problem_id, contest_time";
    $rows = mysql_query_cacheable($sql); # grab the submission activity
    $num_at_problem_minute = array();
    foreach ($rows as $row) {
        $num_at_problem_minute[$row["problem_minute"]] = intval($row["num_at_problem_minute"] * 1.4); # scale so that we don't hit the ceiling
    }

    $sql ="SELECT * FROM submissions "
        . "$where_clause_submissions "
        . "ORDER BY contest_time ASC, result ASC ";

    $rows = mysql_query_cacheable($sql); # grab the submission activity
    $submissions = array();
    foreach ($rows as $row) {
        $submissions[$row["result"]][] = array('problem_id' => strtoupper($row["problem_id"]), 'contest_time' => intval($row["contest_time"]), 'team_id' => intval($row["team_id"]), 'lang_id' => $row['lang_id']);
    }

    ##########################################################
    # Create the datasets in javascript
    ##########################################################

    $datasets = array();

    // Create the histograms of edit_activity
    $baseline_counter = 0;
    $baseline_per_problem = array();
    if (isset($problem_id) && $problem_id != "") {
        $problems_used = array_map(function($s) { return strtoupper($s); }, explode(",", $problem_id));
    } else {
        $problems_used = array();
        for ($problem_ndx = 0; $problem_ndx < count($COMMON_DATA['PROBLEM_ID_TO_NAME']); ++$problem_ndx) {
            $problems_used[] = chr(ord('A') + $problem_ndx);
        }
    }


    foreach ($problems_used as $problem_id) {
        $problem_bins = isset($edit_bins[$problem_id]) ? $edit_bins[$problem_id] : array();
        $dataset = array();
        $dataset["bars"] = array("show" => true, "fill" => 1, "fillColor" => false, "barWidth" => intval($G_BIN_MINUTES * 0.9));
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
    $point_options_by_result = array(
        'AC'   => array("label" => 'AC'   /* 'Accepted'              */,   "color" => '#1a1',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 3,  "fillColor" => false, "fill" => 1)),
        '(CE)' => array("label" => 'CE'   /* 'Compile Error'         */,   "color" => 'yellow',  "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        '(IF)' => array("label" => 'IF'   /* 'Illegal Function'      */,   "color" => 'blue',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        'MLE'  => array("label" => 'MLE'  /* 'Memory Limit Exceeded' */,   "color" => 'pink',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        'OLE'  => array("label" => 'OLE'  /* 'Output Limit Exceeded' */,   "color" => 'purple',  "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        'PE'   => array("label" => 'PE'   /* 'Presentation Error'    */,   "color" => 'gray',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        'RTE'  => array("label" => 'RTE'  /* 'Run Time Error'        */,   "color" => 'orange',  "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        'TLE'  => array("label" => 'TLE'  /* 'Time Limit Exceeded'   */,   "color" => 'red',     "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        'WA'   => array("label" => 'WA'   /* 'Wrong Answer'          */,   "color" => '#333',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
        // FIXME: do we want a result type for "status: fresh"? (i.e. the submission has not been judged) The status is not a "result"
    );


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
                $baseline_per_problem[$problem_id] + $problem_minute_counter[$problem_minute] * $max_problems_per_bin / $scale
            );
            $problem_minute_counter[$problem_minute]++;
        }
        $dataset["submissionInfo"] = array();
        foreach ($result_submissions as $sub) {
            $dataset["submissionInfo"][] = array("team_id" => $sub["team_id"], "problem_id" => $sub["problem_id"], "lang_id" => $sub["lang_id"]);
        }
        $datasets[] = $dataset;
    }


    $response = array(
        "problems_used" => $problems_used,
        "max_problems_per_bin" => $max_problems_per_bin,
        "flot_data" => $datasets
    );

    return $response;
}


if (preg_match('/\/activity_data_source.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    $team_id = isset($_GET["team_id"]) ? $_GET["team_id"] : null;
    $problem_id = isset($_GET["problem_id"]) ? $_GET["problem_id"] : null;
    $response = get_activity_data($team_id, $problem_id);
    header('Content-type: application/json');
    print json_encode($response);
}

?>
