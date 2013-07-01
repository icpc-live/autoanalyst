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

// connect to the database
require_once "icat.php";
$db = init_db();

// for responses
function error($msg) { return array("result" => "error", "data" => $msg); }
function success($data) { return array("result" => "success", "data" => $data); }


function feed_query($table, $conditions, $id, $limit) {

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
        "edit_activity_problem" => array(
            'team_id = [0-9]+',
            'problem_id = "[A-Z]"',
            'problem_id = "[A-Z]" and team_id = [0-9]+',
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
    if (! isset($table)) { return error("Please provide the table name"); }
    $table = $table;
    if (! isset($conditions)) { return error("Please provide the conditions"); }
    $conditions = $conditions;
    // if magic quotes is turned on, then strip out slashes from the conditions 
    if (get_magic_quotes_gpc()) { $conditions = stripslashes($conditions); }
    $conditions = stripslashes($conditions);
    if (isset($id)) { $id = $id; }
    if (isset($limit)) { $limit = $limit; }

    // validate that the query is allowed
    if (! array_key_exists($table, $G_ALLOWED_CONDITIONS)) {
        return error("Error: invalid table name or table name not provided.");
    }

    // build a single regular expression to recognize one of the allowed conditions
    $cond_regexp = '(' . implode('|', $G_ALLOWED_CONDITIONS[$table]) . ')';
    // note that we could allow ANDing these together with the following: $cond_regexp . '( and ' . $cond_regexp . ')*
    if (! preg_match('/^' . $cond_regexp . '$/i', $conditions)) {
        return error("That condition is not permitted. Here is what I received: '" . $conditions . "'");
    }

    if (isset($limit) && ! preg_match("/^[0-9]+$/", $limit)) {
        return error("Limit must be an integer.");
    }

    if (isset($id) && ! preg_match("/^[0-9]+$/", $id)) {
        return error("Id must be an integer.");
    }

    // construct the conditions on the query
    $sql_conditions = array();
    if (isset($id)) { $sql_conditions[] = sprintf(' id >= %d ', $id); }
    if (isset($conditions)) { $sql_conditions[] = $conditions; }
    $sql_conditions = (count($sql_conditions) > 0) ? ' where ' . implode(" and ", $sql_conditions) : '';
    if (isset($limit)) { $sql_conditions .= " limit " . $limit; }

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

    return success($toReturn);
}

if (preg_match('/\/feed_query.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    $table = isset($_GET["table"]) ? $_GET["table"] : null;
    $conditions = isset($_GET["conditions"]) ? $_GET["conditions"] : null;
    $id = isset($_GET["id"]) ? $_GET["id"] : null;
    $limit = isset($_GET["limit"]) ? $_GET["limit"] : null;
    $response = feed_query($table, $conditions, $id, $limit);

    header('Content-type: application/json');
    die(json_encode($response));
}

?>
