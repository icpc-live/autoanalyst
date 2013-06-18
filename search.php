<?php

# if we need to search for something in the database, this will handle that and 
# redirect to the correct place.

$location = "";

$query = $_GET["query"];
if (preg_match("/^[a-z0-9.]+/", $query)) {
    if (preg_match("/^[0-9]+$/", $query)) {
        $location = "team_feed.php?team_id=$query";
    } else if (preg_match("/^[A-Z]$/i", $query)) {
        $location = "problem.php?problem_id=$query";
    } else {
        # assume they're searching for a team name
        include('icat.php');
        $db = init_db();
        $result = mysql_query("select * from teams where school_name like '%$query%' or school_short like '%$query%' or country like '%$query%' order by school_name", $db);

        $location = array();
        while ($result && ($row = mysql_fetch_assoc($result))) {
            $school_name = utf8_encode($row["school_name"]);
            $location[] = array("school_name" => $school_name, "url" => "team_feed.php?team_id=" .  $row["id"]);
        }
    }
}

die(json_encode($location));

?>
