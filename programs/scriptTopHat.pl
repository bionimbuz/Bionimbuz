#argv[0] referencia
#argv[1] reads
#argv[2] arquivo de saida no format bam

topHat("$ARGV[0]","$ARGV[1]","$ARGV[2]");


##procedimento que cria o índice e faz o mapeamento
sub topHat{
	$refName = $_[0];
	$reads = $_[1];
	$alignment = $_[2];
	$indexFile = bowtieIndex($refName);
	topHatAlign("$indexFile","$reads",$alignment);
}

#procedimento que cria o índice
sub bowtieIndex{
	$refName = $_[0];
	@temp = split(/\./,$refName);
	$indexFile = "$temp[0]"."Index";
	system("bowtie-build $refName $indexFile");
	return($indexFile);
}

sub topHatAlign{
	$indexFile = $_[0];
	$reads = $_[1];
	$alignment = $_[2];
	system("topHat  -p 8 $indexFile $reads > $alignment");
}
