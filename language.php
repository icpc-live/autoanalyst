<?php
require_once 'icat.php';
$db = init_db();
$team_id = isset($_GET["team_id"]) ? $_GET["team_id"] : null;
$problem_id = isset($_GET["problem_id"]) ? $_GET["problem_id"] : null;
?>
<html>
<head>
<meta charset="utf-8">
<link rel="stylesheet" type="text/css" href="style.css" />
<style type="text/css">
div#language_count_plot, div#language_percent_plot {
    width: 100%;
    height: 100%;
    margin: 0px auto;
    background: #ddd;
}

body {  }
div.plot_outer_container { margin-left: auto; margin-right: auto; text-align: center; }
div.plot_container { position: relative; display: inline-block; width: 45%; height: 70%;  padding: 20px; }
div.flot_plot div.ticklabel { font-size: 150%; }
div.flot_plot_x_axis_label { text-align: center; }
div.title { text-align: center; }
</style>
<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="flot/jquery.flot.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.resize.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.stack.min.js"></script>
<script type="text/javascript" src="misc.js"></script>
<?php

$where_clause = "";
$where_description = "(all teams, all problems)";
if ($team_id || $problem_id) {
    $clauses = array();
    $descriptions = array();
    if ($team_id) {
        $ids = preg_split('/[^0-9]+/', $team_id);
        $clauses[] = "team_id IN (" . implode(",", $ids) . ")";
        $team = (count($ids) > 1) ? "teams" : "team";
        $ids = array_map(function ($id) {
            global $COMMON_DATA;
            return "<a href='team.php?team_id=$id'>" . $COMMON_DATA["TEAMS"][$id]["school_short"] . "</a>";
        }, $ids);
        $descriptions[] = "$team: " . implode(", ", $ids);
    }
    if ($problem_id) {
        $ids = str_split(strtolower(preg_replace('/[^a-zA-Z]/', '', $problem_id)));
        $clauses[] = "problem_id IN ('" . implode("','", $ids) . "')";
        $problem = (count($ids) > 1) ? "problems" : "problem";
        $descriptions[] = "$problem: " . strtoupper(implode(", ", $ids));
    }

    $where_clause = "WHERE " . implode(" AND ", $clauses);
    $where_description = "(" . implode(" and ", $descriptions) . ") (<a href='language.php'>see all teams, all problems</a>)";
}
$sql = "SELECT lang_id, COUNT(*) AS count FROM submissions $where_clause GROUP BY lang_id";
$result_total = mysql_query($sql);
while ($result_total && $row = mysql_fetch_assoc($result_total)) {
    $count_per_language[$row['lang_id']] = $row['count'];
}

$flot_count_data = array();
$preferred_order = array(
    "AC" => "Accepted",
    "WA" => "Wrong Answer",
    "TLE" => "Time Limit Exceeded",
    "RTE" => "Run Time Error",
    "(CE)" => "Compile Error",
    "(IF)" => "Illegal Function"
);

$sql = "SELECT lang_id, result, COUNT(*) AS count FROM submissions $where_clause GROUP BY lang_id, result";
$per_result = mysql_query($sql);

$language_location = array("C" => 0, "C++" => 1, "Java" => 2);
while ($per_result && $row = mysql_fetch_assoc($per_result)) {
    $submission_result = $row['result'];
    $lang_id = $row['lang_id'];
    $count = (int)($row['count']);

    if (! array_key_exists($submission_result, $flot_count_data)) {
        $new_row = array(
            "label" => $preferred_order[$submission_result],
            "data" => array(),
        );
        $flot_count_data[$submission_result] = $new_row;
        $flot_percent_data[$submission_result] = $new_row;
    }

    $flot_count_data[$submission_result]['data'][] = array($language_location[$lang_id], $count);
    $flot_percent_data[$submission_result]['data'][] = array($language_location[$lang_id], $count / $count_per_language[$lang_id] * 100.0);
    $subInfo = array("lang_id" => $lang_id, "result" => $submission_result);
    $flot_count_data[$submission_result]['submissionInfo'][] = $subInfo;
    $flot_percent_data[$submission_result]['submissionInfo'][] = $subInfo;
}

$flot_count_data_ordered = array();
$flot_percent_data_ordered = array();
foreach ($preferred_order as $short => $long) {
    if (array_key_exists($short, $flot_count_data)) {
        $flot_count_data_ordered[] = $flot_count_data[$short];
        $flot_percent_data_ordered[] = $flot_percent_data[$short];
    }
}
$flot_count_data = $flot_count_data_ordered;
$flot_percent_data = $flot_percent_data_ordered;

$xticks = array();
foreach ($language_location as $k => $v) { $xticks[] = array($v, $k); }
$xticks = json_encode($xticks);
?>

<script type="text/javascript">
var COMMON_DATA = get_json_synchronous("icpc/common_data.php");
$(document).ready(
    function() {
        var series_common = {
            bars: { show: true, barWidth: 0.7, align: "center", fill: true, fillColor: { colors: [ {opacity: 0.9}, {opacity: 0.9} ] } },
            stack: true,
        };
        var count_plot = $('#language_count_plot');
        $.plot(count_plot,
            <?php echo json_encode($flot_count_data); ?>,
            {
                series: series_common,
                legend: { backgroundOpacity: 0.4, position: "nw" },
                grid: { hoverable: true, clickable: true },
                xaxis: { ticks: <?php echo $xticks; ?> },
            }
        );


        var percent_plot = $('#language_percent_plot'); 
        $.plot(percent_plot,
            <?php echo json_encode($flot_percent_data); ?>,
            {
                series: series_common,
                legend: { backgroundOpacity: 0.4, position: "nw" },
                grid: { hoverable: true, clickable: true },
                yaxis: {
                    ticks: [
                       [  0,   '0%'],
                       [ 10,  '10%'],
                       [ 20,  '20%'],
                       [ 30,  '30%'],
                       [ 40,  '40%'],
                       [ 50,  '50%'],
                       [ 60,  '60%'],
                       [ 70,  '70%'],
                       [ 80,  '80%'],
                       [ 90,  '90%'],
                       [100, '100%']
                    ]
                },
                xaxis: { ticks: <?php echo $xticks; ?> }
            }
        );

        count_plot.bind("plothover", hover);
        count_plot.bind("plotclick", showsubmissions);
        count_plot.attr("suffix", " submissions");
        percent_plot.bind("plothover", hover);
        percent_plot.bind("plotclick", showsubmissions);
        percent_plot.attr("suffix", "%");
    }
);


function showsubmissions(event, position, item) {
    if (item) {
        var subInfo = item.series.submissionInfo[item.dataIndex];
        var problem_match = window.location.search.match(/problem_id=[a-z]/i);
        var team_match = window.location.search.match(/team_id=[0-9]+/i);
        var problem_selector = problem_match ? "&" + problem_match[0] : "";
        var team_selector = team_match ? "&" + team_match[0] : "";
        console.log(window.location.search);
        console.log(problem_selector);
        console.log(team_selector);

        var url = 'language_submission_query.php?lang_id=' + encodeURI(subInfo.lang_id) + "&result=" + encodeURI(subInfo.result) + problem_selector + team_selector;

        $.ajax({
            url: url,
            data: { lang_id: subInfo.lang_id, result: subInfo.result },
            success: displaySubmissions,
            error: function(jqXHR, err) { console.log("updatePlot failed for " + url + ": " + jqXHR + ", " + err); },
            dataType: "json"
        });
    }
}

function displaySubmissions(result) {
    var text = [];
    var submissions = result.submissions;
    console.log(result);
    for (t_ndx in submissions) {
        var school_name = t_ndx;
        try {
            // FIXME -- shouldn't need this try/catch if all the school names are properly in the database
            school_name = COMMON_DATA['TEAMS'][t_ndx]['school_name'];
        } catch (e) {}

        var team_submissions = [];
        for (s_ndx in submissions[t_ndx]) {
            var s_id = submissions[t_ndx][s_ndx];
            team_submissions.push("<a href='http://KATTIS_OR_DOMJUDGE/submission.php?ext_id=" + s_id + "'>" + s_id + "</a>");
        }
        text.push("<a href='team.php?team_id=" + t_ndx + "'>" + school_name + "</a>: (" + team_submissions.join(", ") + ")");
    }
    var title = [];
    for (field in result) {
        if (field != 'submissions') {
            title.push(field + ": " + result[field]);
        }
    }
    //if (result.team_id) { title.push('Team ID: ' + result.team_id); }
    //if (result.lang_id) { title.push('Language: ' + result.lang_id); }
    //if (result.result) { title.push('Result: ' + result.result); }
    $("#submissioninfo").html("<h2>" + title.join(", ") + "</h2><ol><li>" + text.join("<li> ") + "</ol>");
}

function hover(event, position, item) {
    if (item) {
        var target = $(event.target);
        target.find("div.hoverinfo").remove();
        var offset = target.offset();
        var value = item.series.data[item.dataIndex][1].toFixed(1);
        var content = item.series.label + ": " + item.series.data[item.dataIndex][1].toFixed(1) + target.attr("suffix");
        $("<div class='hoverinfo'>" + content + "</div>").css(
        {
            position: 'absolute',
            top: position.pageY - offset.top + 10,
            left: position.pageX - offset.left + 10,
            border: '1px solid green',
            padding: '2px',
            'background-color': '#efe',
            'z-index': 100,
            opacity: 0.90
        }).appendTo(target);
    }
}
</script>
</head>
<body>
<?php navigation_container() ?>

<h2>Language use <?php echo $where_description; ?></h2>

<div class="plot_outer_container">
<div class="plot_container">
<div class="title">Number of submissions per language and result</div>
<div class='flot_plot' id="language_count_plot"></div>
<div class='flot_plot_x_axis_label'>Language</div>
</div>
<div class="plot_container">
<div class="title">Percentage of results per language</div>
<div class='flot_plot' id="language_percent_plot"></div>
<div class='flot_plot_x_axis_label'>Language</div>
</div>
</div>

<div clear="both"></div>

<div id="submissioninfo"></div>

</body>
</html>

