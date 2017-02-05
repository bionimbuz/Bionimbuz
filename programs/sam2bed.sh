#!/bin/sh

echo $0 $*
#sleep 30

perl $(pwd)/sam2bed.pl $1 $2
