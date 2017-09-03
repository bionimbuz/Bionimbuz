#!/bin/sh

echo $0 $*
#sleep 30

perl $(pwd)/data/programs/sam2bed.pl $1 $2
