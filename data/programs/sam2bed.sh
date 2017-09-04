#!/bin/sh

# [$1] -> ARQUIVO DE ENTRADA
# [$2] -> ARQUIVO DE SAÍDA

echo $0 $*
#sleep 30

perl $(pwd)/data/programs/sam2bed.pl $1 $2
