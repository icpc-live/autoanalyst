#!/bin/bash
# This script assumes it is run from its own directory.

# construct the colon-separated classpath of jar files in the lib directory,
# plus the src directory 
CP=`find lib/*.jar | tr '\n' ':'`src

echo $CP

# Read CDS connection data from global config file, using
# very hackish YAML parsing
eval `grep -A4 '^CDS:' ../config.yaml | tail -n +2 | \
	sed 's/^[[:space:]]*\([a-z]*\):[[:space:]]*\(.*\)/\1=\2/'`

# run!
#cat data/kattislog_rehearsal_2011.txt | java -classpath $CP katalyzeapp.Katalyze $@
#netcat 192.168.1.141 4714 | java -classpath $CP katalyzeapp.Katalyze $@
curl --user $user:$pass --no-buffer --netrc --insecure --max-time 360000 \
     "${baseurl}/events" | java -classpath $CP katalyzeapp.Katalyze $@

# wait until input
read x
