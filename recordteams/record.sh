#!/bin/bash

id=`printf %03d $1`

ffmpeg -i http://192.168.1.141:60$id -vcodec copy -acodec copy -t 60 /share/recording/team$id-`date +%s`.avi > /dev/null 2> /dev/null
mplayer /home/analyst10/Sonar_pings.ogg
