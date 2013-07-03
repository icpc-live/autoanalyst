#!/bin/sh

DIR=`dirname $0`

echo "Starting screen with sync/commit loop..."
sleep 3

screen bash -c "while true ; do $DIR/gitcommit.sh commit ; sleep 120 ; done"

