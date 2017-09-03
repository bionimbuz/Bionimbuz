#!/bin/sh

echo $0 $*
#sleep 30

perl $(pwd)/data/programs/genome2interval.pl $2 $1 $3
