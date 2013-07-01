<?php
require_once 'icat.php';
?>
<!doctype html>
<html>
<head>
<title>iCAT overview</title>

<link rel="stylesheet" type="text/css" href="style.css" />
<link rel="stylesheet" type="text/css" href="katalyze/css/katalyze.css" />
<meta charset="utf-8">
<style type="text/css">
h1 { text-align: center; margin: 0; }
</style>

<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="feed.js"></script>
<script type="text/javascript" src="misc.js"></script>
<script type="text/javascript">
</script>
</head>
<body>
<?php navigation_container() ?>
<h1>Scoreboard</h1>
<div class='teamscore' data-source='http://<?php echo $_SERVER['SERVER_ADDR']; ?>:8099' data-filter="">(Katalyzer must be running for this to work)</div>
<script type='text/javascript' src='katalyze/web/scores.js'></script>
</body>
</html>
