#!/bin/sh

echo $0 $*
#sleep 30

perl $(pwd)/genome2interval.pl $1 $2 $3
