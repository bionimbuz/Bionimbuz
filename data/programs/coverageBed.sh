#!/bin/sh

echo $0 $*
#sleep 30

#/usr/local/bedtools/bin/bedtools coverage -a $1 -b $2 > $3
coverageBed -a $1 -b $2 > $3
