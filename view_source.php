<?php

/*
 * Show multiple files according to the edit_activity table.
 *
 * Author: Greg Hamerly (hamerly@cs.baylor.edu)
 */
include("icat.php");
$db = init_db();

$edit_id = $_GET["id"];
$edit_ids = explode(",", $edit_id);
$row = mysql_fetch_assoc(mysql_query(sprintf("select * from edit_activity where id = %d", $edit_ids[0]), $db));
$team_id = $row["team_id"];
$problem_id = strtoupper($row["problem_id"]);


$BACKUP_ROOT = ''; # FIXME -- this should point to the root of the backups -- maybe it should be in icat.php

/*
    What sorts of source views do we want to see?
        * a single source file
        - diff of the two most recent versions of a file
            - either use system diff, or use php's xdiff_file_diff (requires libxdiff)
        - source highlighting?
        - other?
 */
?>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<title>iCAT -- problems</title>

<link rel="stylesheet" type="text/css" href="feed.css" />
<style type="text/css">
h1 { text-align: center; }

div.source_display {
    display: inline-block;
    border: thin solid black;
    width: <?php printf("%d", 100 / count($edit_ids) - 1); ?>%;
    overflow: scroll;
    vertical-align: text-top;
}

div.source_display table td {
    white-space: pre;
    font-size: small;
    font-family: monospace;
    overflow: hidden;
}
</style>

<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="feed.js"></script>
<script type="text/javascript">
var team_id = '<?php echo $team_id; ?>';
var problem_id = '<?php echo $problem_id; ?>';

// Set up the feeds.
$(document).ready(function() {

    new feed("#other_edits_feed", {
        name: 'Edit activity for team ' + team_id + ' on problem ' + problem_id,
        table: 'edit_activity',
        conditions: 'team_id = ' + team_id + ' and problem_id = "' + problem_id + '" order by modify_time asc',
        formatter: function(row) {
            var add = document.URL + ',' + row.id;
            return "<a href='problem.php?problem_id=" + row.problem_id + "'>Problem " + row.problem_id + "</a> &mdash; " +
                "<a href='view_source.php?id=" + row.id + "'>" + row.path + "</a> " +
                "(<a href='" + add + "'>add</a>) &mdash; " + row.modify_time;
        }
    });
});

</script>
</head>
<body>

<h1>Source file ID <?php echo implode(", ", $edit_ids); ?>; Problem <?php echo $problem_id; ?>; Team <?php echo $team_id; ?></h1>

<?php 
function source_display($edit_id) {
    global $BACKUP_ROOT, $db;

    $row = mysql_fetch_assoc(mysql_query(sprintf("select * from edit_activity where id = %d", $edit_id), $db));

    $file_path = $BACKUP_ROOT . "/" . $row["path"];
    ///////////////////////////
    // TEST CODE -- REMOVE THIS WHEN THE SOURCE FILES ARE ACTUALLY AVAILABLE AT $ROW["PATH"]
    /*
    $temp_files = array('top.php', 'create_initial_scoreboard.php',  // These are just placeholders...
        'feed_example.php', 'feed_query.php', 'generate_teams.php', 'icat.php', 
        'index.php', 'initialize_db_start_katalyze.php', 'preprocess.php', 
        'problem.php', 'problem_progress.php', 'problem_progress_image.php', 
        'static_problem_names.php', 'static_team_names.php', 'tc.php', 
        'team.php', 'team_feed.php', 'test.php', 'top.php', 'view_source.php');
    $file_path = $temp_files[array_rand($temp_files)];
     */
    ///////////////////////////

    // get the source, count the number of lines, and create a line number prefix
    $source = htmlentities(file_get_contents($file_path));
    $num_lines = count(explode("\n", $source));
    $numbers = '';
    for ($i = 1; $i < $num_lines; ++$i) { $numbers .= "$i:\n"; }

    // Then create the displayed source, with enclosing divs
    $html = "<div class='source_display'>";
    $html .= sprintf("<span class='source_metadata'><a href='view_source.php?id=%d'>%s (edit id %d; modified %s)</a></span>\n", $edit_id, $file_path, $edit_id, $row["modify_time"]);
    $html .= "<table>\n<tr>\n";
    $html .= sprintf("<td>%s</td>\n<td>%s</td>\n", $numbers, $source);
    $html .= "</tr>\n</table>\n</div>\n";
    return $html;
}

foreach ($edit_ids as $id) {
    print(source_display($id));
}
?>

<div id='other_edits_feed'></div>

</body>
</html>

