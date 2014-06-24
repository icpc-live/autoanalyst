<?php

require_once "config.php";
require_once "common_data.php";

session_start();

if (file_exists("contest/sql_cache.php")) {
    require_once "contest/sql_cache.php";
}

function init_db()
{
    global $SQL_CACHE;
    global $dbhost, $dbuser, $dbname, $dbpassword;
    if (isset($SQL_CACHE)) {
        return "";
    } else {
        $db = mysqli_connect($dbhost, $dbuser, $dbpassword, $dbname);
        echo mysqli_connect_error();
        mysqli_set_charset($db,"utf8");
        echo mysqli_error($db);
        return $db;
    }
}

function mysql_query_cacheable($db, $sql) {
    global $SQL_CACHE;
    if (isset($SQL_CACHE)) {
        $sql = strtolower($sql);
        if (array_key_exists($sql, $SQL_CACHE)) {
            return $SQL_CACHE[$sql];
        }
        return null;
    }

    # non-cached version
    $result = mysqli_query($db, $sql);
    $rows = array();
    while ($result && ($row = mysqli_fetch_assoc($result))) {
        $rows[] = $row;
    }
    return $rows;
}

function get_times_in_wf($db, $pid)
{
  $sql = "SELECT COUNT(pid) AS times_in_wf FROM history_attendees WHERE pid='$pid' AND (role_type LIKE 'Contestant%' OR role_type LIKE 'Coach%')";
  $times_in_wf = '';
  $rows = mysql_query_cacheable($db, $sql);
  foreach ($rows as $row) {
      $times_in_wf = $row["times_in_wf"];
  }
  return $times_in_wf;
}

function gen_facts($db, $team_id, $type)
{
  $facts_sql = "SELECT * FROM facts WHERE type='$type' AND team_id=$team_id";
  $rows = mysql_query_cacheable($db, $facts_sql);
  if ($rows) {
      printf("<table id='%s_fact'>", $type);
      foreach ($rows as $facts_row) {
          printf("<tr><td>%s</td></tr>", $facts_row["text"]);
      }
      echo "</table>";
  }
}

function edit_activity($db, $from, $to) 
{
  // Make sure we don't provide information about the last hour
  $to = min(240, $to);

  $from = max(0, $from);
  $from = min($from, $to);

  #echo "from=$from, to=$to";
  
  #{ "id": 213, "modify_time": 83, 
  #  "team_id": 56, "problem_id": "A", 
  #  "language": "C++", "line_count": 49, "diff_line_count": 10, "file_size_bytes": 4277
  #}
  #$sql = "SELECT id, modify_time AS time, team_id, path AS problem_id, language, line_count, diff_line_count, file_size_bytes FROM edit_activity_tmp WHERE $from <= modify_time AND modify_time <= $to";

  $sql = "SELECT edit_activity.id, edit_activity.modify_time AS time, edit_activity.team_id, file_to_problem.problem_id AS problem_id, file_to_problem.lang_id AS language, edit_activity.line_count, edit_activity.lines_changed AS diff_line_count, edit_activity.file_size_bytes FROM edit_activity JOIN file_to_problem ON (edit_activity.team_id = file_to_problem.team_id AND edit_activity.path = file_to_problem.path) WHERE $from <= edit_activity.modify_time AND edit_activity.modify_time <= $to ORDER BY edit_activity.modify_time";

  if ( $res=mysqli_query($db, $sql) ) {
    $arr = array();
    while( $row = mysqli_fetch_array($res, MYSQLI_ASSOC) ) {
      $arr[] = $row;
    }
      
    return json_encode($arr);
  } else {
    return mysqli_error($db);
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


function navigation_container($additional_links = '') {
    global $COMMON_DATA;
?>
<div id="navigation_container">
    <div id='searchbox_container' title="Search by school name, team number, problem letter, or three-letter country">
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
        <?php if ($additional_links) { print $additional_links; } ?>
    </div>
    <div id='hide_uninteresting_teams_container'><input type='checkbox' id='hide_uninteresting_teams_checkbox'><label for='hide_uninteresting_teams_checkbox'>Hide uninteresting teams</label></div>
</div>
<div id='searchbox_chooser'></div>
<?php
}

function add_entry_container($db) {
    $tags = array();
    if (isset($_GET["problem_id"]) && $_GET["problem_id"] != "") {
        $problem_ids = preg_replace("/[^a-z]+/i", ' ', $_GET["problem_id"]);
        $tags[] = preg_replace("/([a-z])/i", '#p$1', $problem_ids);
    }
    if (isset($_GET["team_id"]) && $_GET["team_id"] != "") {
        $team_ids = preg_replace("/[^0-9]+/", ' ', $_GET["team_id"]);
        $tags[] = preg_replace("/([0-9]+)/", '#t$1', $team_ids);
    }
    $tags = implode(" ", $tags);
?>
<div id='add_entry_container'>
    <form class='add_entry_form' action=''>
    <table>
        <tr> <th></th> <th>user</th> <th>priority</th> <th>text (use #tN and #pX to indicate team/problem tags)</th> </tr>
        <tr>
            <td><input type="submit" value="Add entry"    class="add_entry_button"></td>
            <?php
            $entry_username = "frehe";
            if (isset($_SESSION['entry_username'])) {
                $entry_username = $_SESSION['entry_username'];
            }
            ?>
            <td><input type="text"   name="user"          size="8" value="<?php echo $entry_username; ?>"></td>
            <td><input type="text"   name="priority"      size="8" value="0"></td>
            <td><input type="text"   name="text"          size="80" value="<?php echo $tags; ?>"></td>
        </tr>
    </table>
    </form>
</div>
<?php
}

?>
