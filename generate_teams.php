<?php
   /*
    * Generate PHP syntax to define a hash from id (number) to school name
    * (string).  This script is to generate it from the database only once
    * (before the contest) for efficiency.
    */
   header('Content-type: text/plain');
   include("icat.php");
   $db = init_db();

   $sql = "SELECT id, school_name FROM `teams` order by id";
   $qr = mysql_query($sql, $db);
   print("<?\n");
   print("\$TEAM_ID_TO_NAME = array(\n");
   while ($row = mysql_fetch_assoc($qr)) {
       printf('%d => "%s",' . "\n", $row["id"], addslashes($row["school_name"]));
   }
   print(");\n");
   print("?>\n");
 ?>

