<?php
/*
 * Simple backend connection between the database and the javascript on the page powering a 
 * feed. Expects three parameters:
 *    table: the name of the table to pull rows from
 *    id: the least id to obtain (to avoid pulling old data that has already been sent)
 *    conditions: additional conditions on the sql query specific to the feed, 
 *        such as "team_id = 3". Should NOT be prefixed by "where", as the code 
 *        adds that in.
 *
 * This script returns a json-encoded object with the rows that are the results 
 * of the query.
 *
 * Author: Greg Hamerly (hamerly@cs.baylor.edu)
 */

// determine if a GET variable is set to a usable value
function my_isset($x) { return isset($x) and $x != ''; }

// connect to the database
include("icat.php");
$db = init_db();

// for responses
function error($msg) { die(json_encode(array("result" => "error", "data" => $msg))); }
function success($data) { die(json_encode(array("result" => "success", "data" => $data))); }


# Only allow certain queries. Add a query here that you would like to use. This 
# table is given as "table" => array(query1, query2, ...), where the query is a
# regular expression that will be matched to the conditions string.
$G_ALLOWED_CONDITIONS = array(
    "entries" => array(
        'priority <= [0-9]+',
        'text regexp "#p[A-Z]\[\[:>:\]\]"',
        'text regexp "#t[0-9]+\[\[:>:\]\]"',
        'text regexp "the first team to solve"',
        'user != "katalyzer"',
        'text regexp "#t\([0-9]+(\|[0-9]+)+\)\[\[:>:\]\]"',
        '1', # to fulfill an empty "where" clause
    ),
    "edit_activity" => array(
        'team_id = [0-9]+ and valid != 0',
        'problem_id = "[A-Z]" and valid != 0',
        'problem_id = "[A-Z]" and team_id = [0-9]+ and valid != 0',
        'team_id in \([0-9]+(,[0-9]+)+\)',
        '1',
    ),
    "submissions" => array(
        'team_id = [0-9]+',
        'problem_id = "[A-Z]"',
        'team_id in \([0-9]+(,[0-9]+)+\)',
        '1',
    ),
);

// get the parameters
$table = $_GET["table"];
$conditions = $_GET["conditions"];
// if magic quotes is turned on, then strip out slashes from the conditions 
if (get_magic_quotes_gpc()) { $conditions = stripslashes($conditions); }
$conditions = stripslashes($conditions);
$id = $_GET["id"];
$limit = $_GET["limit"];

// validate that the query is allowed
if (! array_key_exists($table, $G_ALLOWED_CONDITIONS)) {
    error("Error: invalid table name or table name not provided.");
}

// build a single regular expression to recognize one of the allowed conditions
$cond_regexp = '(' . implode('|', $G_ALLOWED_CONDITIONS[$table]) . ')';
// note that we could allow ANDing these together with the following: $cond_regexp . '( and ' . $cond_regexp . ')*
if (! preg_match('/^' . $cond_regexp . '$/i', $conditions)) {
    error("That condition is not permitted. Here is what I received: '" . $conditions . "'");
}

if (my_isset($limit) && ! preg_match("/^[0-9]+$/", $limit)) {
    error("Limit must be an integer.");
}

if (my_isset($id) && ! preg_match("/^[0-9]+$/", $id)) {
    error("Id must be an integer.");
}

// construct the conditions on the query
$sql_conditions = array();
if (my_isset($id)) { $sql_conditions[] = sprintf(' id >= %d ', $id); }
if (my_isset($conditions)) { $sql_conditions[] = $conditions; }
$sql_conditions = (count($sql_conditions) > 0) ? ' where ' . implode(" and ", $sql_conditions) : '';
if (my_isset($limit)) { $sql_conditions .= " limit " . $limit; }

// construct the query; do the query
$query = sprintf("select * from %s %s", $table, $sql_conditions);
$results = mysql_query($query);

// grab the results
$toReturn = array();
if ($results) {
    while ($row = mysql_fetch_assoc($results)) {
        $toReturn[] = $row;
    }
}

// send the results back in json format, and exit.
success($toReturn);

?>
