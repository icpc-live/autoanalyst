#!/bin/sh

DIR=`dirname $0`

$DIR/delete_tables.py
$DIR/create_tables.py
$DIR/populate.py
