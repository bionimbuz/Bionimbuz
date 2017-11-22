#ifndef SERIALIZABLE_H
#define SERIALIZABLE_H

#include <string>

template <class T>
class Serializable
{
	public:
		virtual std::string Serialize()=0;
//		virtual T(char const * serializedData)=0;
};

#endif // SERIALIZABLE_H
