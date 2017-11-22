#ifndef HOST_H
#define HOST_H

#include <string>

class Host
{
	public:
		std::string adderss;
		int port;
		Host(std::string address, int port):adderss(address), port(port){}
		Host(void){}
		bool operator==(Host const & other);
		bool operator!=(Host const & other);
};

#endif // HOST_H
