<?php

$query = "";
$function = 'all_superregions';
if (isset($_GET["region_id"]) && $_GET["region_id"] != "") {
    $query = "region_id = " . intval($_GET["region_id"]);
    $name_field = "region_name";
    $function = 'single_region_or_superregion';
} else if (isset($_GET["super_region_id"]) && $_GET["super_region_id"] != "") {
    $query = "super_region_id = " . intval($_GET["super_region_id"]);
    $name_field = "super_region_name";
    $function = 'single_region_or_superregion';
}

require_once 'icat.php';
$db = init_db();

// get the region/super region names
$result = mysql_query("select * from team_regions where $query", $db);

$team_ids = array();
$name = "(unknown)";
while ($result && $row = mysql_fetch_assoc($result)) {
    $team_ids[] = $row["team_id"];
    $name = $row[$name_field];
}


?>
<!doctype html>
<html>
<head>
<title>Region/Super-region</title>

<link rel="stylesheet" type="text/css" href="feed.css" />
<link rel="stylesheet" type="text/css" href="style.css" />
<link rel="stylesheet" type="text/css" href="katalyze/css/katalyze.css" />
<meta charset="utf-8">
<style type="text/css">

div#team_scoreboard_container { text-align: center; }
div#team_scoreboard_container div.teamscore { width: auto; display: inline-block; }

div#feed_container { display: table; table-layout: fixed; width: 100%; margin: 10px 0; }
div#feed_container > div { display: table-cell; }

div#top_row > div {
    display: inline-block;
    vertical-align: top;
}

div#top_row > div#teams_list {
    width: 30%;
}

div#activity_container {
    width: 65%;
    height: 400px;
    margin: 0px auto;
    background: #ddd;
}

h1 {
    text-align: center;
    margin: 0px;
}
</style>

<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="flot/jquery.flot.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.resize.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.navigate.min.js"></script>
<script type="text/javascript" src="feed.js"></script>
<script type="text/javascript" src="misc.js"></script>
<script type="text/javascript" src="activity.js"></script>

<?php
$function($db, $team_ids, $name);
?>

<?php
function all_superregions() {
    global $db;
?>
    <body>
    <?php navigation_container(); ?>
    <h1>Choose a super-region or region to view:</h1>

    <ul>
    <?php
    $result = mysql_query("select super_region_name, super_region_id, count(*) as team_count from team_regions group by super_region_id order by super_region_name", $db);

    while ($result && $row = mysql_fetch_assoc($result)) {
        printf("<li><a href='region.php?super_region_id=%d'>%s</a> (%s teams)\n", $row["super_region_id"], $row["super_region_name"], $row["team_count"]);
        printf("<ul>\n");
        $region_result = mysql_query(sprintf("select region_name, region_id, count(*) as team_count from team_regions where super_region_id=%d group by region_id order by region_name", $row["super_region_id"]), $db);
        while ($region_result && $region_row = mysql_fetch_assoc($region_result)) {
            printf("<li><a href='region.php?region_id=%d'>%s</a> (%s teams)\n", $region_row["region_id"], $region_row["region_name"], $region_row["team_count"]);
        }
        print("</ul>\n");
    }
    ?>
    </ul>

<?php
}
?>

<?php
function single_region_or_superregion($db, $team_ids, $name) {
?>
    <script type="text/javascript">

    // Set up the feeds.
    $(document).ready(function() {
        var team_ids_regex = '<?php echo implode("|", $team_ids); ?>';
        var team_ids_list  = '<?php echo implode(",", $team_ids); ?>';

        new feed("#entries_feed_container", {
            name: 'Katalyze events',
            conditions: 'text regexp "#t(' + team_ids_regex + ')[[:>:]]"'
        });

        new feed("#edit_activity_feed_container", {
            name: 'Edit activity',
            table: 'edit_activity_problem',
            conditions: 'team_id in (' + team_ids_list + ')'
        });

        new feed("#submissions_feed_container", {
            name: 'Submissions',
            table: 'submissions',
            conditions: 'team_id in (' + team_ids_list + ')'
        });

        new ActivityPlot($("#activity_container"), team_ids_list, '');

    });

    </script>
    </head>
    <body>

    <?php navigation_container(); ?>

    <h1><?php
    if (isset($_GET["region_id"])) {
        printf("Region: %s", $name);
    } else {
        printf("Super region: %s", $name);
    }
    ?></h1>

    <div id="top_row">
    <div id='teams_list'>
    Teams:
    <ul>
    <?php
    $result = mysql_query(sprintf("select * from teams where id in (%s) order by id", implode(",", $team_ids)), $db);

    while ($result && $row = mysql_fetch_assoc($result)) {
        $tid = $row["id"];
        $school_name = $row["school_name"];
        $country = $row["country"];
        printf("<li><a href='team.php?team_id=$tid'>$school_name</a> (<a href='javascript:search_query(\"$country\", \"country\")'>$country</a>)\n");
    }

    ?>
    </ul>
    </div>
    <div id="activity_container"></div>
    </div>

    <?php
    printf("<div class='teamscore' data-source='http://192.168.3.6:8079' data-filter=\"score.team.id in {%s}\"></div>", 
        implode(",", array_map(function($id) { return "$id:1"; }, $team_ids ))
    );
    ?>

    <div id='feed_container'>
        <div id='entries_feed_container'></div>
        <div id='edit_activity_feed_container'></div>
        <div id='submissions_feed_container'></div>
    </div>


    <script type='text/javascript' src='katalyze/web/scores.js'></script>
<?php
}
?>

</body>
</html>

