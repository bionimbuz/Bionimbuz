#ifndef COMUNICADOR_HPP
#define COMUNICADOR_HPP

//#include <unistd.h>
//#include <string.h>
//#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
//#include <netinet/ip.h> /* superset of previous */ 
//#include <time.h>

#include "Escalonador.hpp"

typedef int FileDescriptor;
typedef struct sockaddr_in6 SocketAddress;
typedef struct hostent HostEntry;

#define BUFFER_SIZE (65000)

class Comunicador
{
	public:
		Comunicador(int port, int64_t handShakeMsg);
	private:
		FileDescriptor socketFD;
		SocketAddress me, java;
		char *buffer;
		int bytesReadOrWritten;
		
		std::string Receive(std::string begin);
		Escalonador *sched;
		void DefineSched(void);
		void Schedule(void);
};

#endif
