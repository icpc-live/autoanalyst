<!doctype html public "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="Author" content="Fredrik Heintz" />
    <meta name="KeyWords" content="ICPC Programmering Programming Algorithms Competition Computer Science Education">
    <title>ICPC Contest Analysis Tool (iCAT)</title>
    <link rel="stylesheet" type="text/css" href="style.css" />
  </head>
  <body text="#000000" bgcolor="#FFFFFF" link="#0000EE" vlink="#551A8B" alink="#FF0000">

        <form id='team_lookup_form' method="get" action="team.php">
	  <label for="teamSelector">Which team would you like?</label>
	  <input type="text" name="team_str">
	</form>
	<hr />       
    <?php
       include("icat.php");
       $db = init_db();
       if ( isset($team_id) ) {  
         //gen_team_profile($db, $team_id);
       } else if ( isset($team_str) ) {
	 if ( is_numeric($team_str) ) {
	   $team_id = $team_str;
	 } else {
		 $potential_matches = array();
		 foreach ($TEAM_ID_TO_NAME as $id => $name) {
			 if (preg_match("/$team_str/i", $name)) {
				 $potential_matches[$id] = $name;
			 }
		 }

		 if (count($potential_matches) == 1) {
			 $keys = array_keys($potential_matches);
			 $team_id = $keys[0];
		 } else if (count($potential_matches) > 1) {
			 print("Multiple team matches: please select one.\n");
			 print("<ol>\n");
			 foreach ($potential_matches as $id => $name) {
				 printf("<li><a href='team.php?team_id=$id'>[$id] $name</a>\n");
			 }
			 print("</ol>\n");
		 }
	 }
       }

       if (isset($team_id)) {
         gen_team_profile($db, $team_id);
       } else {
	 echo "No team given (or team not found)!";
       }
    ?>

  </body>
</html>
