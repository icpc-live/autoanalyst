#!/bin/sh

cd 
while :; do
    date=`date +"%H%m%S"`
    find /home/icpc/team_backups/team2 -name 'file*' > list$date
done