#include "Comunicador.hpp"
#include <stdio.h>

int main(int argc, char** argv)
{
	freopen("/home/user/xicobionimbuz/Escalonador.log", "a", stdout);
	printf("Programa iniciado!\n");
	Comunicador comunicador(atoi(argv[1]), atol(argv[2]) );
//	Comunicador comunicador(32323, atol(argv[2]) );
	fclose(stdout);
	return 0;
}

