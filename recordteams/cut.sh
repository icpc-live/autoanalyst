#!/bin/bash
# Usage: ./cut.sh from-file to-file from to

avconv -i $1 -ss $3 -t $(($4-$3)) -codec: copy $2
