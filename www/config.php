<?php

$config = yaml_parse_file("../config.yaml");

date_default_timezone_set($config['timezone']);

// Set these shortcut variables for ease of use:
$dbhost = $config['database']['host'];
$dbname = $config['database']['name'];
$dbuser = $config['database']['user'];
$dbpassword = $config['database']['password'];

?>
