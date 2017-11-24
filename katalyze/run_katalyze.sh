#!/bin/bash
# This script assumes it is run from its own directory.

echo $CP

# Read CDS connection data from global config file, using
# some hackish YAML parsing.
eval `cat ../config.yaml | sed -n '/^CDS:/,/^[[:space:]]*$/{/^[[:space:]]*\(baseurl\|user\|pass\):/{s/^[[:space:]]*\([a-z]*\):[[:space:]]*\(.*\)/\1=\2/;p}}'`

# run!
echo "<contest></contest>" |  java -jar target/katalyzer-1.0-SNAPSHOT.jar $@
#cat data/kattislog_rehearsal_2011.txt | java -classpath $CP katalyzeapp.Katalyze $@
#netcat 192.168.1.141 4714 | java -classpath $CP katalyzeapp.Katalyze $@
#curl --user $user:$pass --no-buffer --netrc --insecure --max-time 360000 \
#     "${baseurl}/events" | java -classpath $CP katalyzeapp.Katalyze $@

# wait until input
read x
