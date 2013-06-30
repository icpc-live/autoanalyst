<?php
require_once 'icat.php';

function query_language() {
    $db = init_db();

    $where_conditions = array();
    $response = array();

    $fields = array("problem_id", "team_id", "lang_id", "result");
    foreach ($fields as $field) {
        if (isset($_GET[$field])) {
            $where_conditions[] = $field . " = '" . mysql_escape_string($_GET[$field]) . "'";
            $response[$field] = $_GET[$field];
        }
    }
    if ($where_conditions) {
        $where_conditions = "WHERE " . implode(" AND ", $where_conditions);
    } else {
        $where_conditions = "";
    }

    $sql = "SELECT * FROM submissions $where_conditions ORDER BY team_id, id";
    $q = mysql_query($sql);

    while ($q && ($row = mysql_fetch_assoc($q))) {
        $response['submissions'][$row['team_id']][] = $row['submission_id'];
    }

    return $response;
}

#if (preg_match('/\/language_submission_query.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    $response = query_language();
    header('Content-type: application/json');
    print json_encode($response);
#}

?>
