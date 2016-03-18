#argv[0] arquivo genoma
#argv[1] tamanho do intervalo
#argv[2] arquivo bed contendo intervalos


open(GENOME,"<$ARGV[0]") or die "GENOME ERROR\n";
open(BED_OUT,">$ARGV[2]") or die "BED ERROR\n";

$inc = $ARGV[1];
while($linha=<GENOME>){
	chomp($linha);
	@temp = split(/\t/,$linha);
	$ref = $temp[0];
	$size = $temp[1];
	$i=0;
	while($i<$size){
		$a=$i;
		$b=$a+$inc-1;
		print BED_OUT "$ref\t$a\t$b\n";
		$i+=$inc;
	}
}
