#!/bin/sh

# [$1] -> TAMANHO DO INTERVALO
# [$2] -> ARQUIVO DE ENTRADA
# [$3] -> ARQUIVO DE SAÍDA

echo $0 $*
#sleep 30

perl $(pwd)/data/programs/genome2interval.pl $2 $1 $3
