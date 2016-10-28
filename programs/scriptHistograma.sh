#computa o histograma das reads em intervalos de determinado tamanho
#argv[0] arquivo sam de alinhamento sem cabecalho
#argv[1] arquivo genome contendo o nome das referencias e o tamanho de cada referencia
#argv[2] tamanho do intervalo do histograma
#argv[3] arquivo de saida (histograma)

system("perl sam2bed.pl $ARGV[0] saidabed.temp");
system("perl genome2interval.pl $ARGV[1] $ARGV[2] saidaIntervalos.temp");
system("coverageBed -a saidabed.temp -b saidaIntervalos.temp > $ARGV[3]");
system("rm saidaIntervalos.temp saidabed.temp");
