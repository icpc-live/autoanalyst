#!/bin/bash

# construct the colon-separated classpath of jar files in the lib directory,
# plus the src directory 
CP=`find lib/*.jar | tr '\n' ':'`src

# run!
#cat data/kattislog_rehearsal_2011.txt | java -classpath $CP katalyzeapp.Katalyze $@
netcat 192.168.1.141 4714 | java -classpath $CP katalyzeapp.Katalyze $@

# wait until input
read x
