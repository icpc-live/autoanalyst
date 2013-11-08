<?php
require_once 'icat.php';

function result_per_problem($problem_id) {    
    global $COMMON_DATA;
    $result = mysql_query("select result, count(*) as count from submissions where problem_id = '" . $problem_id . "' group by result");
    $proportion_data = array();
    while ($result && ($row = mysql_fetch_assoc($result))) {
        $judgement_info = $COMMON_DATA['JUDGEMENTS'][$row['result']];
        $count = (int)$row['count'];
        $proportion_data[] = array(
            "label" => $judgement_info['label'] . " ($count)",
            "color" => $judgement_info['color'],
            "data" => $count,
            "sortOrder" => (int)$judgement_info['sortOrder'],
        );
    }
    sort_judgement_data($proportion_data);

    return $proportion_data;
}

if (preg_match('/\/result_per_problem.php$/', $_SERVER["SCRIPT_FILENAME"])) {
    $problem_id = isset($_GET["problem_id"]) ? $_GET["problem_id"] : null;
    $response = result_per_problem($problem_id);
    header('Content-type: application/json');
    print json_encode($response);
}
?>
