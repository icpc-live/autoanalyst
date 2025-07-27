<?php

# if we need to search for something in the database, this will handle that and 
# redirect to the correct place.

$location = "";

$query = isset($_GET["query"]) ? $_GET["query"] : null;
$type = isset($_GET["type"]) ? $_GET["type"] : null;
if (preg_match("/^[a-zA-Z0-9.]+/", $query)) {
    if (preg_match("/^[0-9]+$/", $query)) {
        $location = "team.php?team_id=$query";
    } else if (preg_match("/^[A-Z]$/i", $query)) {
        $location = "problem.php?problem_id=$query";
    } else {
        # assume they're searching for a team name
        require_once 'icat.php';
        $db = init_db();
        if ($type && preg_match("/^(school_name|school_short|country)$/", $type)) {
            $query = "select * from teams where " . $type . " like '%$query%' order by school_name";
        } else {
            $query = "select * from teams where school_name like '%$query%' or school_short like '%$query%' or country like '%$query%' order by school_name";
        }
        $result = mysqli_query($db, $query);

        $location = array();
        while ($result && ($row = mysqli_fetch_assoc($result))) {
            $school_name = $row["school_name"] . " (" . $row["country"] . ")";
            $location[] = array("school_name" => $school_name, "url" => "team.php?team_id=" .  $row["id"]);
        }
    }
}

die(json_encode($location));

?>
