<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="Author" content="Fredrik Heintz" />
    <meta name="KeyWords" content="ICPC Programmering Programming Algorithms Competition Computer Science Education">
    <title>ICPC Contest Analysis Tool (iCAT)</title>
    <link rel="stylesheet" type="text/css" href="style.css" />
  </head>
  <body text="#000000" bgcolor="#FFFFFF" link="#0000EE" vlink="#551A8B" alink="#FF0000">

    <?php
       include("icat.php");
       $db = init_db();

	# how many entries should we show by default? 15 sounds good this time as well and again
	$numEntries = 15;
	if (isset($_GET["numEntries"]) && is_int($_GET["numEntries"] + 0)) {
		$numEntries = $_GET["numEntries"];
	}

       if ( $cmd == "update_entry" ) {
         $text = addslashes($text);
         if ( $entry_id != 0 ) {
           edit_entry($db, $entry_id, $date, $contest_time, $user, $priority, $text);
         } else {
           add_entry($db, $date, $contest_time, $user, $priority, $text);
         }
       } else if ( $cmd == "remove_entry" ) {
         remove_entry($db, $entry_id);
       }

       if ( $cmd == "edit_entry" ) {
         $sql = "SELECT * FROM entries WHERE id = '$entry_id'";
         if ( $entry_res = mysql_query($sql, $db) ) {
           if ( $entry_row = mysql_fetch_array($entry_res) ) {
             $user = $entry_row["user"];
             $date = $entry_row["date"];
             $contest_time = $entry_row["contest_time"];
             $priority = $entry_row["priority"];
             $text = $entry_row["text"];
             $buttonname = "edit";
           }
         }
       } else {
         $user = "frehe";
         $date = date("Y-m-d G:i:s");
	 $contest_time = "0";
	 $priority = "0";
         $text = "";
         $entry_id = "0";
         $buttonname = "add";
       }
     ?>

    <a href="<?php echo($_SERVER['HTTP_REFERER']) ?>">RELOAD</a>
    <table>
      <tr><td>
      	<form id='num_events_form' method="get" action="<?php echo($_SERVER['PHP_SELF']) ?>">
	  <label for="numEntries"># events:</label>
	  <input type="text" name="numEntries" size=4 value="<?php echo($numEntries); ?>">
	</form>
      	<form id='team_lookup_form' method="get" action="team.php">
	  <label for="teamSelector">Which team would you like?</label>
	  <input type="text" name="team_str">
	</form>
      </td></tr>
	 <tr><td><?php gentbl_last_entries($db, $numEntries, 1)?></td></tr>
      <tr><td><?php gentbl_tags($db)?></td></tr>
      <tr>
	<td colspan="2">
	  <form method="post" action="<?php echo($_SERVER['PHP_SELF']) ?>">
	    <table>
	      <tr>
		<td><input type="submit" value="<?php echo $buttonname ?>" /></td>
		<input type="hidden" name="cmd" value="update_entry" />
		<input type="hidden" name="entry_id" value="<?php echo $entry_id ?>" />
		<td><input type="text" name="date" value="<?php echo $date ?>" size="20" /></td>
		<td><input type="text" name="contest_time" value="<?php echo $contest_time ?>" size="8" /></td>
		<td><input type="text" name="user" value="<?php echo $user ?>" size="8" /></td>
		<td><input type="text" name="priority" value="<?php echo $priority ?>" size="8" /></td>
		<td><input type="text" name="text" value="<?php echo $text ?>" size="80" /></td>
	      </tr>
	    </table>
	  </form>
	</td>
      </tr>
      <?php if (isset($tagid)) { echo "<tr>"; gentbl_tag($db, $tagid); echo "</tr>"; } ?>
      <?php if (isset($tagname)) { echo "<tr>"; gentbl_tagname($db, $tagname); echo "</tr>"; } ?>
    </table>
    
  </body>
</html>
