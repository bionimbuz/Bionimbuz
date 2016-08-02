#!/bin/bash

#argv[0] referencia
#argv[1] reads
#argv[2] arquivo de saida no format sam sem cabecalho
#argv[3] arquivo genome contendo o nome das referencias e o tamanho de cada referencia
#argv[4] tamanho do intervalo do histograma
#argv[5] arquivo de saida (histograma)

echo "FAZENDO MAPEAMENTO";
perl scriptBowtie.pl Argv[0] Argv[1] Argv[2]
echo "FAZENDO HISTOGRAMA"
scriptHistograma.pl Argv[2] Argv[3] Argv[4] Argv[5]
