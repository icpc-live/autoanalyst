#!/bin/bash

id=$1
if [ $id -lt 10 ]; then
	id="00$id";
elif [ $id -lt 100 ]; then
	id="0$id";
fi

ffmpeg -i http://192.168.1.141:60$id -vcodec copy -acodec copy -t 60 /share/recording/team$id-`date +%s`.avi > /dev/null 2> /dev/null
mplayer /home/analyst10/Sonar_pings.ogg
