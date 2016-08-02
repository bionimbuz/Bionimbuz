#Os parâmetros para a rotina callBowtie são o arquivo ao qual deve ser gerado
#o index e a referência

#argv[0] referencia
#argv[1] reads
#argv[2] arquivo de saida no format sam sem cabecalho

bowtie("$ARGV[0]","$ARGV[1]","$ARGV[2]");


##procedimento que cria o índice e faz o mapeamento
sub bowtie{
	$refName = $_[0];
	$reads = $_[1];
	$alignment = $_[2];
	#$indexFile = bowtieIndex($refName);
	$indexFile = "chr1Index";
	bowtieAlign("$indexFile","$reads","$alignment");
}

#procedimento que cria o índice
sub bowtieIndex{
	$refName = $_[0];
	@temp = split(/\./,$refName);
	$indexFile = "$temp[0]"."Index";
	system("bowtie-build $refName $indexFile");
	return($indexFile);
}

#procedimento que alinha as reads ao index criado
sub bowtieAlign{
	$indexFile = $_[0];
	$reads = $_[1];
	$alignment = $_[2];
	system("bowtie  -f -p 8 --sam-nohead -k 2 $indexFile $reads $alignment");
}
