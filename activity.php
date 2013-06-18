<?php
include('icat.php');
$db = init_db();
$team_id = isset($_GET["team_id"]) ? $_GET["team_id"] : "";
$problem_id = isset($_GET["problem_id"]) ? $_GET["problem_id"] : "";
?>
<html>
<head>
<link rel="stylesheet" type="text/css" href="style.css" />
<style type="text/css">
div#activity_plot {
    width: 90%;
    height: 80%;
    margin: 0px auto;
    background: #ddd;
}
div#plot_container { position: relative; }
div#activity_plot div.ticklabel { font-size: 150%; }
div.flot_plot_x_axis_label { text-align: center; }
div#title { text-align: center; }
</style>
<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="flot/jquery.flot.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.resize.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.navigate.min.js"></script>
<script type="text/javascript" src="misc.js"></script>
<script type="text/javascript" src="activity.js"></script>
<script type="text/javascript">
$(function() {
    new ActivityPlot($("#activity_plot"), '<?php echo $team_id; ?>', '<?php echo $problem_id; ?>');
});
</script>
</head>
<body>
<?php navigation_container() ?>

<div id="title">Submission and file edit activity for
<?php
if (isset($team_id) && $team_id != "") {
    if (preg_match('/,/', $team_id)) {
        $team_ids = explode(",", $team_id);
    } else {
        $team_ids = array($team_id);
    }
    $sql = sprintf("select id, school_short from teams where id in (%s)", $team_id);
    $result = mysql_query($sql, $db);
    while ($result && $row = mysql_fetch_assoc($result)) {
        $school_short[$row["id"]] = $row["school_short"];
    }
    print("Team: " . 
        implode(",", 
            array_map(function($tid) {
                global $school_short;
                return "<a href='team_feed.php?team_id=$tid'>" . $school_short[$tid] . "</a>";
            }, $team_ids)
        )
    );
    print(" (<a href='activity.php'>see all teams</a>)");
} else {
    print("all teams");
}
?>
</div>

<div id="plot_container">
<div class='flot_plot' id="activity_plot"></div>
<div class='flot_plot_x_axis_label'>Contest time (minutes)</div>
</div>

<div id="instructions">
Instructions: 
The dots represent submissions that have been judged, colored by result type. 
The bars represent number of teams that have edited a related file within 5 minute windows.
The maximum number of edits for a problem in any given window was <?php echo $max_problems_per_bin; ?>.
Clicking on a dot will lead to a view for that team alone.
Hovering will give information about the item.
You can zoom in by double-clicking (not on a dot), or using the scroll wheel.
</div>
</body>
</html>

