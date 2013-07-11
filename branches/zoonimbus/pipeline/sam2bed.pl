#~ ARGUMENTO 0, ARQUIVO SAM  SEM CABEÇALHOS DE INPUT
#~ ARGUMENTO 1, ARQUIVO BED DE OUTPUT

open(SAM_IN,"<$ARGV[0]") or die "SAM ERROR\n";
open(BED_OUT,">$ARGV[1]") or die "BED ERROR\n";

while($linha=<SAM_IN>){
	@temp = split(/\t/,$linha);
	$referencia = $temp[2];
	$inicio = $temp[3];
	$seq = $temp[4];
	$fim = $inicio+length($seq)-1;
	if($referencia !~ /^\*/){
		print BED_OUT "$referencia\t$inicio\t$fim\n";
	}
}
