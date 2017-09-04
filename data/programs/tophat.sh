#!/bin/bash

# [$1] -> ARQUIVO DE REFERÊNCIA CONTENDO OS ÍNDICES DO BOWTIE (GERADOS VIA BOWTIE-BUILD)
# [$2] -> ARQUIVO DE ENTRADA CONTENDO READS
# [$3] -> ARQUIVO DE SAÍDA

ncpu=`nproc`
ncpu=6
$(pwd)/data/programs/tophat-2.1.1.Linux_x86_64/tophat -T -p $ncpu --output-dir $3 $1 $2
