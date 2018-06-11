#include "Comunicador.hpp"
#include <stdio.h>
#include <signal.h>

void NaoFecharas(int a){
	printf("Tentaram fechar o escalonador com o sinal %d\n", a);
}

#define SIGINT 2
#define SIGQUIT 3
#define SIGABRT 4
#define SIGTERM 15

int main(int argc, char** argv)
{
//	signal(SIGINT, NaoFecharas);
//	signal(SIGQUIT, NaoFecharas);
//	signal(SIGABRT, NaoFecharas);
//	signal(SIGTERM, NaoFecharas);
	freopen("/home/user/xicobionimbuz/Escalonador.log", "a", stdout);
	printf("Programa iniciado!\n");
	Comunicador comunicador(atoi(argv[1]), atol(argv[2]) );
	fflush(stdout);
//	Comunicador comunicador(32323, atol(argv[2]) );
	fclose(stdout);
	printf("Programa encerrado!\n");
	return 0;
}

