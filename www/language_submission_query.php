<?php
require_once 'icat.php';

function query_language($team_id, $problem_id, $lang_id, $result) {
    $db = init_db();

    $where_conditions = array();
    $response = array();

    $fields = array("problem_id" => $problem_id, "team_id" => $team_id, "lang_id" => $lang_id, "result" => $result);
    foreach ($fields as $name => $value) {
        if (isset($value)) {
            $where_conditions[] = $name . " = '" . mysql_escape_string($value) . "'";
            $response[$name] = $value;
        }
    }
    if ($where_conditions) {
        $where_conditions = "WHERE " . implode(" AND ", $where_conditions);
    } else {
        $where_conditions = "";
    }

    $sql = "SELECT * FROM submissions $where_conditions ORDER BY team_id, id";
    $rows = mysql_query_cacheable($db, $sql);

    foreach ($rows as $row) {
        $response['submissions'][$row['team_id']][] = array('submission_id' => $row['submission_id'], 'problem_id' => $row['problem_id']);
    }

    return $response;
}

if (preg_match('/\/language_submission_query.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    $team_id = isset($_GET["team_id"]) ? $_GET["team_id"] : null;
    $problem_id = isset($_GET["problem_id"]) ? $_GET["problem_id"] : null;
    $lang_id = isset($_GET["lang_id"]) ? $_GET["lang_id"] : null;
    $result = isset($_GET["result"]) ? $_GET["result"] : null;
    $response = query_language($team_id, $problem_id, $lang_id, $result);
    header('Content-type: application/json');
    print json_encode($response);
}

?>
