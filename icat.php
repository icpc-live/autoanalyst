<?php

include("config.php");
include("dbconfig.php");

date_default_timezone_set("Europe/Warsaw");

function init_db()
{
  $db = mysql_connect($dbhost, $dbuser, $dbpassword);
   echo mysql_error();
   mysql_select_db("icat", $db);
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

function gen_team_profile($db, $team_id)
{
  $sql = "SELECT * FROM teams JOIN regions ON teams.team_id=regions.team_id WHERE id='$team_id'";
  if ( $team_res = mysql_query($sql, $db) ) {
    if ( $team_row = mysql_fetch_array($team_res) ) {
      $country = $team_row["country"];
      $country_sql = "SELECT COUNT(team_id) AS country_teams_in_wf FROM teams WHERE country='$country'";
      if ( $country_res = mysql_query($country_sql, $db) ) {
	if ( $country_row = mysql_fetch_array($country_res) ) {
	  $country_teams_in_wf = $country_row["country_teams_in_wf"];
	}
      }

      $region_name = $team_row["region_name"];
      $region_sql = "SELECT COUNT(team_id) AS regional_teams_in_wf FROM regions WHERE region_name='$region_name'";
      if ( $region_res = mysql_query($region_sql, $db) ) {
	if ( $region_row = mysql_fetch_array($region_res) ) {
	  $regional_teams_in_wf = $region_row["regional_teams_in_wf"];
	}
      }

      $school_name = $team_row["school_name"];
      $coach_wfs = get_times_in_wf($db, $team_row["coach_id"]);
      $contestant1_wfs = get_times_in_wf($db, $team_row["contestant1_id"]);
      $contestant2_wfs = get_times_in_wf($db, $team_row["contestant2_id"]);
      $contestant3_wfs = get_times_in_wf($db, $team_row["contestant3_id"]);

      $tc_sql = "SELECT * FROM top_coder WHERE university_name LIKE '${school_name}%'";
      if ( $tc_res = mysql_query($tc_sql, $db) ) {
	if ( $tc_row = mysql_fetch_array($tc_res) ) {
	  $coach_tcid = $tc_row["coach_tcid"];
	  $coach_tcname = $tc_row["coach_tcname"];
	  $coach_tcrating = $tc_row["coach_rating"];
	  $coach_tcrank = $tc_row["coach_rank"];
	  $contestant1_tcid = $tc_row["contestant1_tcid"];
	  $contestant1_tcname = $tc_row["contestant1_tcname"];
	  $contestant1_tcrating = $tc_row["contestant1_rating"];
	  $contestant1_tcrank = $tc_row["contestant1_rank"];
	  $contestant2_tcid = $tc_row["contestant2_tcid"];
	  $contestant2_tcname = $tc_row["contestant2_tcname"];
	  $contestant2_tcrating = $tc_row["contestant2_rating"];
	  $contestant2_tcrank = $tc_row["contestant2_rank"];
	  $contestant3_tcid = $tc_row["contestant3_tcid"];
	  $contestant3_tcrating = $tc_row["contestant3_rating"];
	  $contestant3_tcname = $tc_row["contestant3_tcname"];
	  $contestant3_tcrank = $tc_row["contestant3_rank"];
	}
      }
      
      echo "<h2 id='schoolname'>$school_name</h2>";
      echo "<table id='static_summary'><tr valign=\"top\">";
      echo "<td><table id='team_region_info'>";
      echo "<tr><th>Team number</th><th>Name</th><th>Country</th></tr>";
      printf("<tr><td align=\"center\">%s</td><td align=\"center\">%s</td><td align=\"center\">%s<img class='flag' src='images/flags/%s'></td></tr>", $team_row["id"], $team_row["team_name"], $team_row["country"], '' . $team_row["country"] . ".png");
      //printf("<tr><td align=\"center\">%s</td><td align=\"center\">%s</td><td align=\"center\">%s<img class='flag' src='%s'></td></tr>", $team_row["id"], $team_row["team_name"], $team_row["country"], 'http://spc11.contest.scrool.se/finals/flags/' . $team_row["country"] . ".png");
      //printf("<tr><td align=\"center\">%s</td><td align=\"center\">%s</td><td align=\"center\">%s<img class='flag' src='%s'></td></tr>", $team_row["id"], $team_row["team_name"], $team_row["country"], 'http://scrool.se/images/flags/' . $team_row["country"] . ".png");
      echo "<tr><th></th><th></th><th>Times in WF</th><th>TC Name</th><th>TC Rating</th></tr>";
      printf("<tr><th>Coach</th><td>%s</td><td align=\"center\">%s</td><td align=\"center\"><a href=\"http://www.topcoder.com/tc?module=MemberProfile&tab=alg&cr=%s\">%s</a></td><td align=\"center\">%s</td></tr>", $team_row["coach_name"], $coach_wfs, $coach_tcid, $coach_tcname, $coach_tcrating);
      printf("<tr><th>Contestants</th><td>%s</td><td align=\"center\">%s</td><td align=\"center\"><a href=\"http://www.topcoder.com/tc?module=MemberProfile&tab=alg&cr=%s\">%s</a></td><td align=\"center\">%s</td></tr>", $team_row["contestant1_name"], $contestant1_wfs, $contestant1_tcid, $contestant1_tcname, $contestant1_tcrating);
      printf("<tr><th></th><td>%s</td><td align=\"center\">%s</td><td align=\"center\"><a href=\"http://www.topcoder.com/tc?module=MemberProfile&tab=alg&cr=%s\">%s</a></td><td align=\"center\">%s</td></tr>", $team_row["contestant2_name"], $contestant2_wfs, $contestant2_tcid, $contestant2_tcname, $contestant2_tcrating);
      printf("<tr><th></th><td>%s</td><td align=\"center\">%s</td><td align=\"center\"><a href=\"http://www.topcoder.com/tc?module=MemberProfile&tab=alg&cr=%s\">%s</a></td><td align=\"center\">%s</td></tr>", $team_row["contestant3_name"], $contestant3_wfs, $contestant3_tcid, $contestant3_tcname, $contestant3_tcrating);
      echo "<tr><th></th><th></th><th>WF Teams</th><th></th><th></th></tr>";
      printf("<tr><th>Region</th><td><a href=\"%s\">%s</a></td><td>%s WF teams</td><td></td><td></td></tr>", $team_row["region_scoreboard_url"], $team_row["region_name"], $regional_teams_in_wf);
      printf("<tr><th></th><td>Rank %s; solved %s problems in %s minutes</td><td></td><td></td><td></td></tr>", $team_row["rank"], $team_row["problems_solved"], $team_row["total_time"]);
      echo "<tr></tr>";
      printf("<tr><th>Country</th><td>%s</td><td>%s WF teams</td><td></td><td></td></tr>", $team_row["country"], $country_teams_in_wf);
      echo "</table></td>";

      $start_year = 1999;
      $end_year = 2012;
      $years = $end_year - $start_year +1;
      $year = $end_year - 1;
      $results_sql = "SELECT * FROM history_results WHERE year < $end_year AND university_name LIKE '${school_name}%' ORDER BY year DESC";
      if ( $results_res = mysql_query($results_sql, $db) ) {
	$total_times_in_wf = mysql_num_rows($results_res)+1;
	echo "<td><table id='previous_wf_performance'>";
	echo "<tr><th>Year</th><th>Place in WF</th><th>Solved problems</th><th>Time</th></tr>";
	while ( $results_row = mysql_fetch_array($results_res) ) {
	  $prev_year = $results_row["year"];
	  $place = $results_row["place"];
	  $solved = $results_row["solved"];
	  $time = $results_row["time"];
	  if ( $place == 0 ) {
	    $place = "hm";
	  }
	  if ( $solved == 0 ) {
	    $solved = "";
	  }
	  if ( $time == 0 ) {
	    $time = "";
	  }
	  while ( $year > $prev_year ) {
	    printf("<tr><td align=\"center\">%s</td><td align=\"center\">-</td><td align=\"center\"></td><td align=\"center\"></td></tr>", $year);
	    $year--;
	  }
	  printf("<tr><td align=\"center\">%s</td><td align=\"center\">%s</td><td align=\"center\">%s</td><td align=\"center\">%s</td></tr>", $year, $place, $solved, $time);
	  $year--;
	}
	while ( $year >= $start_year ) {
	  printf("<tr><td align=\"center\">%s</td><td align=\"center\">-</td><td align=\"center\"></td><td align=\"center\"></td></tr>", $year);
	  $year--;
	}
	echo "<tr><td colspan=\"4\">$total_times_in_wf world finals the last $years years ($start_year-$end_year)</td></tr>";
	echo "</table></td>";
	echo "</tr></table>";

	# Print fun and hard facts
	echo "<hr />";
	echo "Fun facts:<br>";
	gen_facts($db, $team_id, "fun");
	//gen_facts($db, $team_id, "hard");
	//echo "</tr></table>";
	
	
	echo "<br /><hr />";
	//gen_rank_for_team($db, $team_id);
	//gen_team_problems_solved($db, $team_id);
	scrape_kattis_scoreboard_for_team($team_id);

	echo "<br /><hr />";
	$team_tag = "t" . $team_id;
	gentbl_tagname($db, $team_tag);
      }

    }
  }
}

function gen_rank_for_team($db, $team_id)
{
	# wish we could do this without iterating over all the rows
	$sql = "SELECT team_id, num_solutions, total_time FROM scoreboard ORDER BY num_solutions DESC, total_time ASC";
	$result = mysql_query($sql, $db);
	$min_rank = 0;
	$max_rank = 0;
	$row_rank = 0;
	if ($result) {
		$last_num_solved = 0;
		$last_total_time = 0;
		$found_team_id = false;
		while ($row = mysql_fetch_assoc($result)) {
			++$row_rank;
			if (($last_num_solved == $row["num_solutions"]) && ($last_total_time == $row["total_time"])) {
				$max_rank = $row_rank;
			} else {
				if ($found_team_id) {
					break;
				}
				$min_rank = $row_rank;
				$max_rank = $row_rank;
			}

			if ($row["team_id"] == $team_id) {
				$found_team_id = true;
			}
			$last_num_solved = $row["num_solutions"];
			$last_total_time = $row["total_time"];
		}
	}
	if ($min_rank == $max_rank) {
		if ($min_rank == 0) {
			$min_rank = "unknown";
		}
		print("Rank: $min_rank,\n");
	} else {
		print("Rank range (tied on # problems & time): $min_rank - $max_rank,\n");
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

function gen_team_problems_solved($db, $team_id)
{
	global $PROBLEM_ID_TO_NAME;

	$sql = "SELECT * FROM scoreboard WHERE team_id = $team_id";
	$result = mysql_query($sql, $db);

	if ($result) {
		$row = mysql_fetch_assoc($result);

		printf("Total solved: %d,\n", $row["num_solutions"]);
		printf("Total time: %d\n", $row["total_time"]);
		echo "<table border>\n";

		printf("<tr><td>Problem</td>");
		for ($problem = 'A'; $problem <= 'Z'; ++$problem) {
			if (array_key_exists($problem, $PROBLEM_ID_TO_NAME)) {
				print("<td>$problem</td>");
			}
		}
		print("</tr>\n");
		print("<tr><td>Time of solution</td>\n");
		for ($problem = 'A'; $problem <= 'Z'; ++$problem) {
			if (array_key_exists($problem, $PROBLEM_ID_TO_NAME)) {
				$st = $row[strtolower($problem) . "_soln_time"];
				$subs = $row[strtolower($problem) . "_submissions"];
				$css = "";
				if ($st > 0) { $css = " class='solved'"; }
				else if ($subs > 0) { $css = " class='attempted'"; }
				printf("<td%s>%d</td>", $css, $st);
			}
		}
		print("</tr>\n");
		print("<tr><td># Submissions</td>\n");
		for ($problem = 'A'; $problem <= 'Z'; ++$problem) {
			if (array_key_exists($problem, $PROBLEM_ID_TO_NAME)) {
				$st = $row[strtolower($problem) . "_soln_time"];
				$subs = $row[strtolower($problem) . "_submissions"];
				$css = "";
				if ($st > 0) { $css = " class='solved'"; }
				else if ($subs > 0) { $css = " class='attempted'"; }
				printf("<td%s>%d</td>", $css, $subs);
			}
		}
		print("</tr>\n");
		echo "</table>\n";
	}
}

function gentbl_last_entries($db, $num_entries, $max_prio)
{
  echo "<table id='recent_events' class='event_feed'>";
  echo "<tr><th colspan=\"5\">Last $num_entries entries</th></tr>";

  $limit = ($num_entries > 0) ? " LIMIT $num_entries" : "";
  $sql = "SELECT * FROM entries WHERE priority <= $max_prio ORDER BY date DESC" . $limit;

  $entry_res = mysql_query($sql, $db);
  print_entries($entry_res);

  echo "</table>";
}

function gentbl_tags($db)
{
  echo "Tags:";

  $sql = "SELECT id, name FROM tagnames ORDER BY name";
  $tagname_res = mysql_query($sql, $db);
  if ( $tagname_res ) {
    while ( $tagname_row = mysql_fetch_array($tagname_res) ) {
      printf(" <a href=\"%s?tagid=%s\">%s</a>", $_SERVER['PHP_SELF'], $tagname_row["id"], $tagname_row["name"]);
    }
  } else {
    printf("%s<br /><br />%s<br />", mysql_error(), $sql);
  }
}

function gentbl_tag_old($db, $tag_id)
{
  $tagname = get_tag_name($db, $tag_id);
  
  //$sql = "SELECT e.date AS date, e.contest_time AS contest_time, e.user AS user, e.text AS text, t.name AS name FROM entries e INNER JOIN tags ON tags.entry_id = e.id INNER JOIN tagnames t ON t.id = tags.tag_id ORDER BY date";
  $sql = "SELECT e.date AS date, e.contest_time AS contest_time, e.user AS user, e.text AS text FROM entries e INNER JOIN tags ON tags.entry = e.id AND tags.tag = $tag_id ORDER BY date";
  $entry_res = mysql_query($sql, $db);
  if ( $entry_res ) {
    echo "<table>";
    echo "<tr><td>Entries for #$tagname</td></tr>";
    print_entries($entry_res);
    echo "</table>";
  } else {
    printf("%s<br /><br />%s", mysql_error(), $sql);      
  }
}

function gentbl_tag($db, $tag_id)
{
  gentbl_tagname($db, get_tag_name($db, $tag_id));
}

function gentbl_tagname($db, $tagname) {
  $sql = "SELECT * FROM `entries` WHERE text REGEXP '$tagname($|[[:space:][:punct:]])' ORDER BY date DESC";
  $entry_res = mysql_query($sql, $db);
  if ( $entry_res ) {
    echo "<table id='tagged_events' class='event_feed'>";
    echo "<tr><td>Entries for #$tagname</td></tr>";
    print_entries($entry_res);
    echo "</table>";
  } else {
    printf("%s<br /><br />%s", mysql_error(), $sql);      
  }
}

function print_entries($entry_res)
{
  if ( $entry_res ) {
    while ( $entry_row = mysql_fetch_array($entry_res) ) {
      print_entry($entry_row["id"], $entry_row["date"], $entry_row["contest_time"], $entry_row["priority"], $entry_row["user"], $entry_row["text"]);
    }
  } else {
    printf("<tr><td colspan=\"5\">%s</td></tr>", mysql_error(), $sql);
  }
}

function print_entry($entry_id, $date, $contest_time, $priority, $user, $text)
{
  # timestamps are expected in swedish time; contest is in florida; change the
  # timestamp by 6 hours
  $date = date('d M Y H:i:s', strtotime('-6 hours', strtotime($date)));

  $page=$_SERVER['PHP_SELF'];
  $edit_button = "<form method=\"post\" action=\"$page\">
    <input type=\"hidden\" name=\"cmd\" value=\"edit_entry\" />
    <input type=\"hidden\" name=\"entry_id\" value=\"$entry_id\" />
    <input type=\"submit\" value=\"edit\" />
    </form>";
  $remove_button = "<form method=\"post\" action=\"$page\">
    <input type=\"hidden\" name=\"cmd\" value=\"remove_entry\" />
    <input type=\"hidden\" name=\"entry_id\" value=\"$entry_id\" />
    <input type=\"submit\" value=\"rm\" />
    </form>";

  if ( $priority == 0 ) {
    printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s<td><b>%s</b></td></tr>", $date, $edit_button, $remove_button, $contest_time, $user, $priority, replace_tags_with_links(stripslashes($text)));
  } else {
    printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s<td>%s</td></tr>", $date, $edit_button, $remove_button, $contest_time, $user, $priority, replace_tags_with_links(stripslashes($text)));
  }
}

function replace_tags_with_links($text)
{
  global $TEAM_ID_TO_NAME;
  global $PROBLEM_ID_TO_NAME;
  $res = $text;
  $tags = extract_tags($text);
  foreach ( $tags as $tag ) {
    $tagname = ltrim($tag, "#");
    $tagid = ltrim($tagname, "tp");
    if (preg_match('/^p/', $tagname)) {
	    $links[] = sprintf("#<a href=\"%s?tagname=%s\">%s %s</a>", "index.php", $tagname, $tagname, $PROBLEM_ID_TO_NAME[$tagid]);
    } elseif (preg_match('/^t/', $tagname)) {
	    //$links[] = sprintf("#<a href=\"%s?tagname=%s\">%s %s</a>", $_SERVER['PHP_SELF'], $tagname, $tagname, $TEAM_ID_TO_NAME[$tagid]);
	    $links[] = sprintf("#<a href=\"%s?team_id=%s\">%s %s</a>", "team.php", $tagid, $tagname, $TEAM_ID_TO_NAME[$tagid]);
    } else {
	    $links[] = sprintf("#<a href=\"%s?tagname=%s\">%s</a>", "index.php", $tagname, $tagname);
    }
  }
  return str_replace($tags, $links, $text);
}

function add_entry($db, $date, $contest_time, $user, $priority, $text)
{
  $sql = "INSERT INTO entries (user, date, contest_time, priority, text) VALUES ('$user', '$date', '$contest_time', '$priority', '$text')";
  if ( mysql_query($sql, $db) ) {
    $entry_id = mysql_insert_id();

    $tags = extract_tags($text);
    foreach ( $tags as $tag ) {
      $tagname = ltrim($tag, "#");
      if (preg_match("/^#t[0-9]+$/", $tag) or  preg_match("/^#p[a-z]$/i", $tag)) {
      } else {
	insert_tag_entry($db, $tagname, $entry_id);
      }
    }
  } else {
    printf("%s<br /><br />%s", mysql_error(), $sql);      
  }
}

function edit_entry($db, $entry_id, $date, $contest_time, $user, $priority, $text)
{
  // remove tags in old text
  
  $sql = "UPDATE entries SET user='$user', date='$date', contest_time='$contest_time', priority='$priority', text='$text' WHERE id='$entry_id'";
  if ( mysql_query($sql, $db) ) {
    $tags = extract_tags($text);
    foreach ( $tags as $tag ) {
      $tagname = ltrim($tag, "#");
      insert_tag_entry($db, $tagname, $entry_id);
    }
  } else {
    printf("%s<br /><br />%s", mysql_error(), $sql);      
  }
}

function remove_entry($db, $entry_id)
{
  // remove tags in old text
  
  $sql = "DELETE FROM entries WHERE id='$entry_id'";
  if ( mysql_query($sql, $db) ) {
  } else {
    printf("%s<br /><br />%s", mysql_error(), $sql);      
  }
}

function extract_tags($text)
{
  $res = array();
  $delim = " \n\t.,;:?![]{}()-/";
  $tok = strtok($text, $delim);

  while ($tok !== false) {
    if ( $tok[0] == '#' ) {
      //$res[] = ltrim($tok, "#");
      $res[] = $tok;
    }
    $tok = strtok($delim);
  }

  return $res;
}

function insert_tag($db, $tag)
{
  $tag_id = -1;
  $sql = "INSERT INTO tagnames (name) VALUES ('$tag')";
  if ( mysql_query($sql, $db) ) {
    $tag_id = mysql_insert_id();
  } else {
    printf("%s<br /><br />%s<br />", mysql_error(), $sql);      
  }
  return $tag_id;
}

function get_tag_id($db, $tag)
{
  $sql = "SELECT id FROM tagnames WHERE name = '$tag'";
  $tagname_res = mysql_query($sql, $db);
  if ( $tagname_res ) {
    if ( mysql_num_rows($tagname_res) == 0 ) {
      return insert_tag($db, $tag);
    } else if ( mysql_num_rows($tagname_res) == 1 ) {
      $tagname_row = mysql_fetch_array($tagname_res);
      return $tagname_row["id"];
    } else {
      printf("There are %s tags named %s< br/>", mysql_num_rows($tagname_res), $tag);
    }
  } else {
    printf("%s<br /><br />%s<br />", mysql_error(), $sql);      
  } 
  return -1;
}

function get_tag_name($db, $tag_id)
{
  $sql = "SELECT name FROM tagnames WHERE id = $tag_id";
  $tagname_res = mysql_query($sql, $db);
  if ( $tagname_res ) {
    if ( mysql_num_rows($tagname_res) == 1 ) {
      $tagname_row = mysql_fetch_array($tagname_res);
      return $tagname_row["name"];
    } else {
      printf("There are %s tags with id %s<br />", mysql_num_rows($tagname_res), $id);
    }
  } else {
    printf("%s<br /><br />%s<br />", mysql_error(), $sql);      
  }
}

function insert_tag_entry($db, $tag, $entry_id)
{
  $tag_id = get_tag_id($db, $tag);
  /*
  $sql = "INSERT INTO tags (entry, tag) VALUES ('$entry_id', '$tag_id')";
  if ( mysql_query($sql, $db) ) {
  } else {
    printf("%s<br /><br />%s<br />", mysql_error(), $sql);      
  }
  */
}

function update_top_coder_rank($db)
{
  $tc_sql = "SELECT * FROM top_coder";
  if ( $tc_res = mysql_query($tc_sql, $db) ) {
    echo "<table>";
    while ( $tc_row = mysql_fetch_array($tc_res) ) {
      $tcid = $tc_row["coach_tcid"];
      if ( $tcid > 0 ) top_coder_rank($db, $tcid, "coach");
      $tcid = $tc_row["contestant1_tcid"];
      if ( $tcid > 0 ) top_coder_rank($db, $tcid, "contestant1");
      $tcid = $tc_row["contestant2_tcid"];
      if ( $tcid > 0 ) top_coder_rank($db, $tcid, "contestant2");
      $tcid = $tc_row["contestant3_tcid"];
      if ( $tcid > 0 ) top_coder_rank($db, $tcid, "contestant3");
    }
    echo "</table>";
  }
}

function top_coder_rank($db, $tcid, $prefix)
{
  $ch = curl_init("http://www.topcoder.com/tc?module=MemberProfile&tab=alg&cr=$tcid");
  curl_setopt($ch, CURLOPT_HEADER, false);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	
  $html = curl_exec($ch);
  curl_close($ch);

  preg_match('/>Algorithm Rating:<\/td><td class="stat" align="right">\n.*\n.*(<span.*\n.*span>)/', $html, $matches);
  $rating = $matches[1];

  preg_match('/>Rank:<\/td><td class="valueR">([^<]*)/', $html, $matches);
  $rank = $matches[1];
  
  preg_match('/>School Rank:<\/td><td class="valueR">([^<]*)/', $html, $matches);
  $srank = $matches[1];
  
  preg_match('/>Country Rank:<\/td><td class="valueR">([^<]*)/', $html, $matches);
  $crank = $matches[1];

  $sql = "UPDATE top_coder SET ${prefix}_rating='$rating', ${prefix}_rank='$rank', ${prefix}_srank='$srank', ${prefix}_crank='$crank' WHERE ${prefix}_tcid=$tcid";
  mysql_query($sql, $db);
}

function navigation_container() {
    global $G_NUM_PROBLEMS;
?>
<div id="navigation_container">
    <div id='searchbox_container'>
    <label for='searchbox'>Search:</label><input type='text' id='searchbox' name="query">
    </div>
    
    <div id='problem_list_container'>
    <?php
    for ($i = 0; $i < $G_NUM_PROBLEMS; ++$i) {
        $c = chr(ord('A') + $i);
        print("<a href='problem.php?problem_id=$c'>$c</a>&nbsp;");
    }
    ?>
    </div>

    <div id='link_container'>
        <a href='overview.php'>Overview</a>
        <a href='activity.php'>Activity graph</a>
        <a href='scoreboard.php'>Scoreboard</a>
        <a href='regions.php'>Regions</a>
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
            <td><input type="text"   name="contest_time"  size="8"></td>
            <td><input type="text"   name="user"          size="8" value="frehe"></td>
            <td><input type="text"   name="priority"      size="8" value="0"></td>
            <td><input type="text"   name="text"          size="80"></td>
        </tr>
    </table>
    </form>
</div>
<?php
}

?>
