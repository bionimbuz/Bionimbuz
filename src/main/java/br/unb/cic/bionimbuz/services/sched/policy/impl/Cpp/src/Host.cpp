#include "Host.h"

bool Host::operator==(Host const & other){
	return adderss==other.adderss && port == other.port;
}

bool Host::operator!=(Host const & other){
	return !(*this==other);
}



