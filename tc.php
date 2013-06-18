<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="Author" content="Fredrik Heintz" />
    <meta name="KeyWords" content="ICPC Programmering Programming Algorithms Competition Computer Science Education">
    <title>ICPC Contest Analysis Tool (iCAT)</title>
  </head>
  <body text="#000000" bgcolor="#FFFFFF" link="#0000EE" vlink="#551A8B" alink="#FF0000">

    <?php
       include("icat.php");
       $db = init_db();
       update_top_coder_rank($db);
    ?>

  </body>
</html>
