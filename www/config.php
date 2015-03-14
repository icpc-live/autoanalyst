<?php

$config = yaml_parse_file(dirname(__FILE__) . "/../config.yaml");

if ( $config===FALSE ) {
	echo "Error: could not read configuration file.";
	exit(1);
}

date_default_timezone_set($config['timezone']);

// Set these shortcut variables for ease of use:
$dbhost = $config['database']['host'];
$dbname = $config['database']['name'];
$dbuser = $config['database']['user'];
$dbpassword = $config['database']['password'];

?>
