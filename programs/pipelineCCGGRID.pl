#argv[0] referencia
#argv[1] reads
#argv[2] arquivo de saida no format sam sem cabecalho
#argv[3] arquivo genome contendo o nome das referencias e o tamanho de cada referencia
#argv[4] tamanho do intervalo do histograma
#argv[5] arquivo de saida (histograma)

print("MAPEAMENTO\n\n");
system("perl scriptBowtie.pl $ARGV[0] $ARGV[1] $ARGV[2]");
print("HISTOGRAMA\n\n");
system("perl scriptHistograma.pl $ARGV[2] $ARGV[3] $ARGV[4] $ARGV[5]");
#todo
##incluir tophat
