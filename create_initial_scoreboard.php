<?php
   /*
    * CAREFUL WITH THIS SCRIPT!
    * This script will clear out the scoreboard table, and populate it with
    * just the team ids.
    */

   print("CAREFUL WITH THIS SCRIPT -- ABOUT TO ELIMINATE AND REINITIALIZE ALL DATA IN 'scoreboard' TABLE\n");
   print("KILL THIS SCRIPT IF YOU WANT TO AVOID ELIMINATING ALL DATA IN scoreboard!\n");
   for ($i = 10; $i >= 0; --$i) {
	   print("$i seconds...\n");
	   sleep(1);
   }

   include("icat.php");
   $db = init_db();

   # remove all the rows from the scoreboard
   $sql = "truncate table scoreboard";
   print("truncating table\n");
   $qr = mysql_query($sql, $db);

   # populate the scoreboard with empty rows
   $sql = "SELECT id, school_name FROM `icpc2011` order by id";
   $qr = mysql_query($sql, $db);
   while ($row = mysql_fetch_assoc($qr)) {
	   $sql = sprintf("insert into scoreboard (team_id) values (%d)", $row["id"]);
	   print("executing query: $sql\n");
	   mysql_query($sql, $db);
   }
 ?>

