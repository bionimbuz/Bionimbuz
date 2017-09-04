#!/bin/bash

# [$1] -> ARQUIVO DE REFERÊNCIA CONTENDO OS ÍNDICES DO BOWTIE (GERADOS VIA BOWTIE-BUILD)
# [$2] -> ARQUIVO DE ENTRADA CONTENDO READS
# [$3] -> ARQUIVO DE SAÍDA

echo $0 $*
#sleep 30

tmp=`echo $$`

ncpu=`nproc`

#bowtie-build $1 index-$tmp
#bowtie -f -p 8 --sam-nohead -k 2 index-$tmp $2 $3
#rm -rf index-$tmp.*

bowtie -f -p $ncpu --sam-nohead -k 2 $1 $2 $3
#$(pwd)/data/programs/tophat-2.1.1.Linux_x86_64/tophat2 -T -p $ncpu --output-dir $3 $1 $2
