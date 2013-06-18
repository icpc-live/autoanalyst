#!/bin/bash

OPTS=''
while [ $# -gt 1 ]; do
	OPTS="$OPTS $1"
	shift
done

TMPD=`mktemp -d`

SOURCE=$1
BASE=`basename $SOURCE`

TMPF=$TMPD/$BASE

TEAM=`echo $SOURCE | sed -r 's,^.*/(team[0-9]+)/.*$,\1,'`

cat > $TMPF <<EOF
/*
  Autosubmit at `date '+%H:%M'`
  team: $TEAM
  file: $SOURCE
  modtime: `find $SOURCE -printf '%TT' | sed -r 's/\.[0-9]+//'`
*/
EOF

cat $SOURCE >> $TMPF

~domjudge/domjudge/submit/submit $OPTS $TMPF

rm -rf $TMPD
