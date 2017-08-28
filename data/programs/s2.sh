#!/bin/sh

echo $0 $*
#sleep 30

perl /home/zoonimbus/zoonimbusProject/pipeline/sam2bed.pl $1 $2
