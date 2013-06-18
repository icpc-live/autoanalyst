<?php

// Show the diff of two files that are referenced using ids in the edit_activity 
// table of the icat database. 
// Author: Greg Hamerly (hamerly@cs.baylor.edu)

include('icat.php');
$db = init_db();

// Look up the given id in the edit_activity table, grab the file contents, make 
// them printable on the web (convert special characters to html entities) and 
// print the results.
function getFile($id) {
    $file_contents = "<file associated with edit id $id not found>";

    $result = mysql_query(sprintf("select * from edit_activity where id = %d", $id), $db);
    if ($result) {
        // FIXME -- need to know where the actual directory will be in $BACKUP_ROOT,
        // and perhaps this should be a variable set in icat.php
        $BACKUP_ROOT = '';

        $row = mysql_fetch_assoc($result);
        $file_path = $BACKUP_ROOT . "/" . $row["path"];
        $file_contents = file_get_contents($file_path);
    }
    return json_encode(htmlentities($file_contents));
}

?>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<title>iCAT -- diff</title>

<link rel="stylesheet" type="text/css" href="diff.css" />

<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="diff.js"></script>
<script type="text/javascript">
$(document).ready(function() {
    new diff('div#diff_container', {
        a_name: 'id ' + <?php echo json_encode($_GET["id1"]); ?>,
        b_name: 'id ' + <?php echo json_encode($_GET["id2"]); ?>,
        a_src: <?php echo getFile($_GET["id1"]); ?>,
        b_src: <?php echo getFile($_GET["id2"]); ?>
    });
});
</script>
</head>
<body>

<div id='diff_container'></div>

</body>
</html>

