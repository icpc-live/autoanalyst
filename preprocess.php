<?php

include("icat.php");
$db = init_db();

if ( $cmd == "update_entry" ) {
  $text = addslashes($text);
  if ( $entry_id != 0 ) {
    edit_entry($db, $entry_id, $user, $date, $text);
  } else {
    add_entry($db, $user, $date, $text);
  }
 }

header("Location: http://www.ida.liu.se/~frehe/icat");
?>
