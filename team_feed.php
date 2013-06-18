<?php
$team_id = $_GET["team_id"];
include("icat.php");
$db = init_db();
?>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<title>iCAT feed</title>

<link rel="stylesheet" type="text/css" href="feed.css" />
<link rel="stylesheet" type="text/css" href="style.css" />
<link rel="stylesheet" type="text/css" href="katalyze/css/katalyze.css" />
<style type="text/css">

div#team_facts_container div#members, 
div#team_facts_container div#past_wf_performance,
div#team_facts_container div#fun_facts
{ display: inline-block; vertical-align: text-top; } /* FIXME: yuck */

div#team_scoreboard_container { text-align: center; }
div#team_scoreboard_container div.teamscore { width: auto; display: inline-block; }

img.flag { background: #eee; border: thin solid black; width: 28px; height: 28px; }

div#title { text-align: center; }
div#title > div { display: inline-block; padding: 0 0.5em; vertical-align: bottom; }
div#title div#schoolname { text-align: center; font-size: x-large; font-weight: bold; }
div#title div#teamname,
div#title div#country { text-align: center; font-size: large; }
div#title div#country  img { vertical-align: middle; }

div#feed_container { display: table; table-layout: fixed; width: 100%; margin: 10px 0; }
div#feed_container > div { display: table-cell; }
div#fun_facts { border: thin solid black; padding: 10px; }

div#activity_container { width: 90%; height: 200px; margin: 0px auto; background: #ddd; }
div#video_container { text-align: center; }
div#team_submission_info { text-align: center; }
</style>

<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="feed.js"></script>
<script type="text/javascript" src="flot/jquery.flot.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.resize.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.navigate.min.js"></script>
<script type="text/javascript" src="misc.js"></script>
<script type="text/javascript" src="activity.js"></script>
<script type="text/javascript">

// Set up the feeds.
$(document).ready(function() {
    var team_id = '<?php echo $team_id; ?>';

    new feed("#entries_feed_container", {
        name: 'Katalyze events',
        conditions: 'text regexp "#t' + team_id + '[[:>:]]"'
    });

    new feed("#edit_activity_feed_container", {
        name: 'Edit activity',
        table: 'edit_activity',
        conditions: 'team_id = ' + team_id + ' and valid != 0'
    });

    new feed("#submissions_feed_container", {
        name: 'Submissions',
        table: 'submissions',
        conditions: 'team_id = ' + team_id
    });

    new ActivityPlot($("#activity_container"), '<?php echo $team_id; ?>', '');
});

</script>
</head>
<body>

<?php navigation_container(); ?>


<?php
$result = mysql_query("select * from teams where id = $team_id", $db);
$team_row = mysql_fetch_array($result);
$school_name = $team_row["school_name"];

$result = mysql_query("select * from regions where team_id = $team_id", $db);
$region_row = mysql_fetch_array($result);

$result = mysql_query("select count(*) as cnt, lang_id from submissions where team_id = $team_id group by lang_id", $db);
$language_counts = array();
while ($row = mysql_fetch_assoc($result)) {
    $language_counts[$row["lang_id"]] = $row["cnt"];
}
?>

<!-- Team title information -->

<div id="title">
    <div id="schoolname"> <?php printf("%s (Id: %s)", $school_name, $team_id); ?> </div>
    <div id="teamname">Team: <?php echo $team_row["team_name"]; ?></div>
    <div id="country">Country: <?php $c = $team_row["country"]; printf("%s <img class='flag' src='images/flags/%s.png'>", $c, $c, $c); ?></div>
    <!--
    <div id="region">Region: <?php printf("<a href='%s'>%s</a>", $region_row["region_scoreboard_url"], $region_row["region_name"]); ?> </div>
    -->
</div>

<div id="video_container">
    <?php $padded_team_id = sprintf("%03d", $team_id); ?>
    <a href="vlc://192.168.1.141:58<?php echo $padded_team_id; ?>">Video (low)</a>
    <a href="vlc://192.168.1.141:60<?php echo $padded_team_id; ?>">Video (high)</a>
    <a href="vnc://192.168.1.141:59<?php echo $padded_team_id; ?>">Screen</a>
    <a href="activity.php?team_id=<?php echo $team_id; ?>">Team activity</a>
</div>


<!-- Dynamic content (things that change during the contest) -->

<div id='team_scoreboard_container'>
<?php
printf("<div class='teamscore' data-source='http://192.168.3.6:8079' data-filter=\"score.team.id=='%d'\"></div>", $team_id);
/*scrape_kattis_scoreboard_for_team($team_id);*/
?>
</div>
<div id='team_submission_info'>
    Number of submissions by language:
    <?php
        $printed = false;
        foreach ($language_counts as $lang => $count) {
            if ($printed) { print(", "); }
            print("$lang: $count\n");
            $printed = true;
        }
        if (! $printed) { print("No submissions."); }
    ?>
</div>

<div id='activity_container'></div>

<div id='feed_container'>
    <div id='entries_feed_container'></div>
    <div id='edit_activity_feed_container'></div>
    <div id='submissions_feed_container'></div>
</div>

<?php add_entry_container(); ?>

<!-- Information that is more or less static (does not change during the contest) -->

<div id='team_facts_container'>

    <div id="members">
        <div id="contestants">
            <table border>
                <tr><th>Title</th><th>Name</th><th>Times in WF</th><th>TC Name</th><th>TC Ranking</th></tr>
                <?php
                    $tc_sql = "SELECT * FROM top_coder WHERE university_name LIKE '${school_name}%'";
                    $tc_row = array(); $tc_url = "";
                    if ($result = mysql_query($tc_sql, $db)) {
                        $tc_row = mysql_fetch_array($result);
                        $tc_url = "<a href='http://www.topcoder.com/tc?module=MemberProfile&tab=alg&cr=%s'>%s</a>";
                    }
                    $members = array("coach", "contestant1", "contestant2", "contestant3");
                    foreach ($members as $m) { ?>
                        <tr>
                            <td><?php echo $m;?> </td>
                            <td><?php echo $team_row["{$m}_name"];?> </td>
                            <td><?php echo get_times_in_wf($db, $team_row["{$m}_id"]); ?></td>
                            <td><?php printf($tc_url, $tc_row["{$m}_tcid"], $tc_row["{$m}_tcname"]); ?> </td>
                            <td><?php echo $tc_row["{$m}_rank"]; ?></td>
                        </tr>
              <?php } ?>
            </table>
       </div>
    </div>

    <div id="past_wf_performance">
        <table border>
            <tr> <th>Year</th> <th>Place in WF</th> <th>Solved</th> <th>Time</th> </tr>
        <?php
          $start_year = 1999;
          $end_year = 2011;
          $results_sql = "SELECT * FROM history_results WHERE year < $end_year AND university_name LIKE '${school_name}%'";
          $results = mysql_query($results_sql, $db);
          $p = array();
          $total_times_in_wf = 0;
          while ($row = mysql_fetch_array($results)) {
              if ($row["solved"] == 0) { $row["solved"] = "";   }
              if ($row["place"]  == 0) { $row["place"]  = "hm"; }
              if ($row["time"]   == 0) { $row["time"]   = "";   }
              $p[$row["year"]] = $row;
              $total_times_in_wf++;
          }
          for ($y = $end_year; $y >= $start_year; --$y) {
              printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", $y, $p[$y]["place"], $p[$y]["solved"], $p[$y]["time"]);
          }
          printf("<tr><td colspan='4'>%d world finals the last %d years</td></tr>", $total_times_in_wf, $end_year - $start_year + 1);
        ?>
        </table>
    </div>

    <div id="fun_facts">Fun facts:
    <?php gen_facts($db, $team_id, "fun"); ?>
    </div>
</div>

<script type='text/javascript' src='katalyze/web/scores.js'></script>

</body>
</html>

