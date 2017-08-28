#!/bin/sh

echo $0 $*
#sleep 30

perl $(pwd)/programs/genome2interval.pl $2 $1 $3
