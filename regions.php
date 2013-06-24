<?php

require_once 'icat.php';
$db = init_db();

?>
<html>
<head>
<meta charset="utf-8">
<link rel="stylesheet" type="text/css" href="style.css" />
<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="misc.js"></script>
</head>
<body>
<?php navigation_container() ?>


<h1>Choose a super-region or region to view:</h1>

<ul>
<?php
$result = mysql_query("select super_region_name, super_region_id, count(*) as team_count from team_regions group by super_region_id order by super_region_name", $db);

while ($result && $row = mysql_fetch_assoc($result)) {
    printf("<li><a href='region.php?super_region_id=%d'>%s</a> (%s teams)\n", $row["super_region_id"], $row["super_region_name"], $row["team_count"]);
    printf("<ul>\n");
    $region_result = mysql_query(sprintf("select region_name, region_id, count(*) as team_count from team_regions where super_region_id=%d group by region_id order by region_name", $row["super_region_id"]), $db);
    while ($region_result && $region_row = mysql_fetch_assoc($region_result)) {
        printf("<li><a href='region.php?region_id=%d'>%s</a> (%s teams)\n", $region_row["region_id"], $region_row["region_name"], $region_row["team_count"]);
    }
    print("</ul>\n");
}
?>
</ul>

</body>
</html>

