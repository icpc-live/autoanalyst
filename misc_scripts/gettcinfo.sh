#!/bin/sh

for id in $@; do
  wget -O tcdata.html "http://www.topcoder.com/tc?module=MemberProfile&cr=$id"

  name=`grep "$id.*coderText[a-Z]*\">[_a-Z0-9]*" -o tcdata.html | grep ">[_a-Z0-9]*" -o | grep "[_a-Z0-9]*" -o`

  rankstr=">Rank:</td><td class=\"valueR\">"
  rank=`grep "$rankstr[0-9]*" -o tcdata.html | grep "[0-9]*" -o`
  rank_from=`grep "$rankstr" tcdata.html | grep -o "of [0-9]*" | grep "[0-9]*" -o`

  crankstr=">Country Rank:</td><td class=\"valueR\">"
  crank=`grep "$crankstr[0-9]*" -o tcdata.html | grep "[0-9]*" -o`
  crank_from=`grep "$crankstr" tcdata.html | grep -o "of [0-9]*" | grep "[0-9]*" -o`

  srankstr=">School Rank:</td><td class=\"valueR\">"
  srank=`grep "$srankstr[0-9]*" -o tcdata.html | grep "[0-9]*" -o`
  srank_from=`grep "$srankstr" tcdata.html | grep -o "of [0-9]*" | grep "[0-9]*" -o`

  echo name=$name

  echo rank=$rank
  echo rank from=$rank_from

  echo country rank=$crank
  echo country rank from=$crank_from

  echo school rank=$srank
  echo school rank from=$srank_from
done
