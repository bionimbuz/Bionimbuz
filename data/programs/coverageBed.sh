#!/bin/sh

# [$1] -> ARQUIVO DE ENTRADA 1
# [$2] -> ARQUIVO DE ENTRADA 2
# [$3] -> ARQUIVO DE SAÍDA

echo $0 $*
#sleep 30

#/usr/local/bedtools/bin/bedtools coverage -a $1 -b $2 > $3
coverageBed -a $1 -b $2 > $3
