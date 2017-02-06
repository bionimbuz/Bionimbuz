#!/bin/sh

echo $0 $*
#sleep 30

perl $(pwd)/programs/sam2bed.pl $1 $2
