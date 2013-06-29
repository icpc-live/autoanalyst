<?php

require_once "icpc/config.php";
require_once "icpc/common_data.php";

session_start();

function init_db()
{
    global $dbuser;
    global $dbhost;
    global $dbpassword;
    $db = mysql_connect($dbhost, $dbuser, $dbpassword);
    echo mysql_error();
    mysql_select_db("icat", $db);
    echo mysql_error();
    mysql_set_charset("utf8");
    echo mysql_error();
    return $db;
}

function get_times_in_wf($db, $pid)
{
  $sql = "SELECT COUNT(pid) AS times_in_wf FROM history_attendees WHERE pid='$pid' AND (role_type LIKE 'Contestant%' OR role_type LIKE 'Coach%')";
  if ( $res = mysql_query($sql, $db) ) {
    if ( $row = mysql_fetch_array($res) ) {
      $times_in_wf = $row["times_in_wf"];
    }
  }
  return $times_in_wf;
}

function gen_facts($db, $team_id, $type)
{
  $facts_sql = "SELECT * FROM facts WHERE type='$type' AND team_id=$team_id";
  if ( $facts_res = mysql_query($facts_sql, $db) ) {
    printf("<table id='%s_fact'>", $type);
    while ( $facts_row = mysql_fetch_array($facts_res) ) {
      printf("<tr><td>%s</td></tr>", $facts_row["text"]);
    }
    echo "</table>";
  }
}

function scrape_kattis_scoreboard_for_team($team_id)
{
	global $TEAM_ID_TO_NAME;
	$team_name = $TEAM_ID_TO_NAME[$team_id];
	//$raw_html = file_get_contents("http://cm.baylor.edu/scoreboard");
	//$raw_html = file_get_contents("http://scrool.se/icpc/wf2011/");
	$raw_html = file_get_contents("http://www.ida.liu.se/~frehe/scoreboard");

	#$raw_html = file_get_contents("index.html"); // for testing

	# remove all newlines
	$raw_html = preg_replace("/[\n\r]/", " ", $raw_html);
	# remove everything before and after the standings table
	$raw_html = preg_replace("/^.*<table id='standings'>/", "", $raw_html);
	$raw_html = preg_replace("/<\/table>.*$/", "", $raw_html);

	# replace image url references to go to the server we are taking them from
	$raw_html = preg_replace("/\/images/", "http://www.ida.liu.se/~frehe/icat/images", $raw_html);

	# split into table rows, one per team
	$rows = preg_split("/<tr[^>]*>/", $raw_html);

	# print the header row
	printf("<table id='standings'><tr>%s\n", $rows[1]);

	# look for the team we are searching for
	$found = false;
	foreach ($rows as $row) {
        # make sure to look for a complete team name (hence the angle brackets requiring that it goes all the way from open to close tag)
        if (strpos($row, ">" . $team_name . "<")) {
			# if found, print it, and stop looking...
			printf("<tr>%s\n", $row);
			$found = true;
			break;
		}
	}

	if (! $found) { print("could not get scoreboard for $team_name"); }
	print("</table>\n");
}


function navigation_container() {
    global $COMMON_DATA;
?>
<div id="navigation_container">
    <div id='searchbox_container'>
    <label for='searchbox'>Search:</label><input type='text' id='searchbox' name="query">
    </div>
    
    <div id='problem_list_container'>
    <?php
    for ($i = 0; $i < count($COMMON_DATA['PROBLEM_ID_TO_NAME']); ++$i) {
        $c = chr(ord('A') + $i);
        print("<a href='problem.php?problem_id=$c'>$c</a>&nbsp;");
    }
    ?>
    </div>

    <div id='link_container'>
        <a href='overview.php'>Overview</a>
        <a href='activity.php'>Activity graph</a>
        <a href='scoreboard.php'>Scoreboard</a>
        <a href='language.php'>Languages</a>
        <a href='region.php'>Regions</a>
    </div>
</div>
<div id='searchbox_chooser'></div>
<?php
}

function add_entry_container() {
?>
<div id='add_entry_container'>
    <form class='add_entry_form' action=''>
    <table>
        <tr> <th></th> <th>contest_time</th> <th>user</th> <th>priority</th> <th>text (use #tN and #pX to indicate team/problem tags)</th> </tr>
        <tr>
            <td><input type="submit" value="Add entry"    class="add_entry_button"></td>
            <?php
            $result = mysql_query("SELECT MAX(contest_time) AS last_submission FROM submissions");
            $last_submission = 0;
            if ($result) {
                $row = mysql_fetch_assoc($result);
                $last_submission = $row['last_submission'];
            }

            $entry_username = "frehe";
            if (isset($_SESSION['entry_username'])) {
                $entry_username = $_SESSION['entry_username'];
            }
            ?>
            <td><input type="text"   name="contest_time"  size="8" value="<?php echo $last_submission; ?>"></td>
            <td><input type="text"   name="user"          size="8" value="<?php echo $entry_username; ?>"></td>
            <td><input type="text"   name="priority"      size="8" value="0"></td>
            <td><input type="text"   name="text"          size="80"></td>
        </tr>
    </table>
    </form>
</div>
<?php
}

?>
