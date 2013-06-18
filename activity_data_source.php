<?php
include("icat.php");
$db = init_db();

// IDEAS:
//  - turn on/off certain result types

// the resolution of the time-based bins -- FIXME -- make this a parameter
$G_BIN_MINUTES = 5;
$G_BIN_SECONDS = $G_BIN_MINUTES * 60;

// determine if we should limit this to one team
$where_conditions = array();
if (isset($_GET["team_id"]) && $_GET["team_id"] != "") {
    $where_conditions[] = "team_id in (" . $_GET["team_id"] . ")";
}

if (isset($_GET["problem_id"]) && $_GET["problem_id"] != "") {
    $problem_id = preg_replace("/([a-z])/i", '"$1"', $_GET["problem_id"]);
    $where_conditions[] = "problem_id in (" . $problem_id . ")";
}

# where clause for submissions
$where_clause_submissions = $where_conditions ? "where " . implode(" and ", $where_conditions) : "";

# where clause for edit_activity
# don't grab edit_activity rows that have been marked invalid
$where_conditions[] = "valid != 0";
$where_clause_edit_activity = $where_conditions ? "where " . implode(" and ", $where_conditions) : "";

##########################################################
# Edit activity
##########################################################

# Grab the edit_activity rows aggregated by time, binning by every $G_BIN_SECONDS 
# seconds. Convert to contest time (i.e. minutes between 1-300). 
$result = mysql_query(
    /*
    "SELECT ROUND((UNIX_TIMESTAMP(modify_time) - $G_CONTEST_START_TIME_UNIX) / $G_BIN_SECONDS) * $G_BIN_MINUTES AS contest_time_binned "
    . ", problem_id, COUNT(*) AS count "
    . "FROM edit_activity "
    . "$where_clause_edit_activity "
    . "GROUP BY problem_id, contest_time_binned "
    . "ORDER BY problem_id, contest_time_binned "
    */
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
    ,
    $db
);

# go through all the rows, save each, and determine the maximum counts (for 
# scaling of the plot)
$edit_bins = array();
$max_problems_per_bin = 0; // need to know how many problems are solved in any one binned time interval
while ($row = mysql_fetch_assoc($result)) {
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
$result = mysql_query($sql, $db); # grab the submission activity
$num_at_problem_minute = array();
while ($result && ($row = mysql_fetch_assoc($result))) {
    $num_at_problem_minute[$row["problem_minute"]] = intval($row["num_at_problem_minute"] * 1.4); # scale so that we don't hit the ceiling
}

$sql ="SELECT * FROM submissions "
    . "$where_clause_submissions "
    . "ORDER BY contest_time ASC, result ASC ";

$result = mysql_query($sql, $db); # grab the submission activity
$submissions = array();
while ($result && ($row = mysql_fetch_assoc($result))) {
    $submissions[$row["result"]][] = array('problem_id' => strtoupper($row["problem_id"]), 'contest_time' => intval($row["contest_time"]), 'team_id' => intval($row["team_id"]));
}

##########################################################
# Create the datasets in javascript
##########################################################

$datasets = array();

// Create the histograms of edit_activity
$baseline_counter = 0;
$baseline_per_problem = array();
if (isset($_GET["problem_id"]) && $_GET["problem_id"] != "") {
    $problems_used = array_map(function($s) { return strtoupper($s); }, explode(",", $_GET["problem_id"]));
} else {
    $problems_used = array();
    for ($problem_ndx = 0; $problem_ndx < $G_NUM_PROBLEMS; ++$problem_ndx) {
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
    'AC'   => array("label" => 'Accepted',                "color" => '#1a1',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 3,  "fillColor" => false, "fill" => 1)),
    '(CE)' => array("label" => 'Compile Error',           "color" => 'yellow',  "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    '(IF)' => array("label" => 'Illegal Function',        "color" => 'blue',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    'MLE'  => array("label" => 'Memory Limit Exceeded',   "color" => 'pink',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    'OLE'  => array("label" => 'Output Limit Exceeded',   "color" => 'purple',  "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    'PE'   => array("label" => 'Presentation Error',      "color" => 'gray',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    'RTE'  => array("label" => 'Run Time Error',          "color" => 'orange',  "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    'TLE'  => array("label" => 'Time Limit Exceeded',     "color" => 'red',     "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    'WA'   => array("label" => 'Wrong Answer',            "color" => '#333',    "shadowSize" => 0, "points" => array("show" => true, "radius" => 2,  "fillColor" => false, "fill" => 1)),
    // FIXME: do we want a result type for "status: fresh"? (i.e. the submission has not been judged) The status is not a "result"
);


$problem_minute_counter = array();
$row_counter = 0;
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
        $scale = max(10, $num_at_problem_minute[$problem_minute]);
        $dataset["data"][] = array(
            $sub["contest_time"], 
            $baseline_per_problem[$problem_id] + $problem_minute_counter[$problem_minute] * $max_problems_per_bin / $scale
        );
        $problem_minute_counter[$problem_minute]++;
    }
    $dataset["submissionInfo"] = array();
    foreach ($result_submissions as $sub) {
        $dataset["submissionInfo"][] = array("team_id" => $sub["team_id"], "problem_id" => $sub["problem_id"]);
    }
    $datasets[] = $dataset;
    $row_counter++;
}


$response = array(
    "problems_used" => $problems_used,
    "max_problems_per_bin" => $max_problems_per_bin,
    "flot_data" => $datasets
);

die(json_encode($response));
?>
