#!/bin/bash

# construct the colon-separated classpath of jar files in the lib directory,
# plus the src directory 
CP=`find lib/*.jar | tr '\n' ':'`src

echo $CP

# run!
#cat data/kattislog_rehearsal_2011.txt | java -classpath $CP katalyzeapp.Katalyze $@
#netcat 192.168.1.141 4714 | java -classpath $CP katalyzeapp.Katalyze $@
curl --user analyst:dr0pjes --netrc --insecure --max-time 360000 https://192.168.1.207/events | java -classpath $CP katalyzeapp.Katalyze $@

# wait until input
read x
