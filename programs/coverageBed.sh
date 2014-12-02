#!/bin/sh

echo $0 $*
#sleep 30

#/home/gabriel/Programas/bedtools-2.17.0/bin/coverageBed -a $1 -b $2 > $3
coverageBed -a $1 -b $2 > $3
