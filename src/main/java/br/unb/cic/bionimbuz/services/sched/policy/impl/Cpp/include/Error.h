#ifndef ERROR_H
#define ERROR_H

#include <iostream>
#include "stdlib.h"

#define FORCE_FLUSH

#ifdef FORCE_FLUSH
	#define END_LINE std::endl;
#elif _WIN32
	#define END_LINE "\r\n"
#else
	#define END_LINE "\n"
#endif

//#define USE_AT

using std::endl;

#ifndef DEBUG
	#define DEBUG 0
#endif
#define CONVERSAO_GRAUS_RADIANOS 57.324840764

#define Error(msg)std::cerr<<"[ERROR] "<<WHERE<<"\t\t"<<msg<<END_LINE;exit(1);
#define ASSERT(exp)if(!(exp)){std::cerr<<"[ERROR] "<<WHERE<<"\t\tAssertion Failed."<<END_LINE;exit(1);}
#define ASSERT2(exp,msg)if(!(exp)){std::cerr<<"[ERROR] "<<WHERE<<"\t\tAssertion Failed." <<msg<< "\n";exit(1);}
#define SDL_ASSERT(exp)if(!(exp)){std::cerr<<"[ERROR] "<<WHERE<<"\t\tAssertion Failed:" << SDL_GetError()<<END_LINE;exit(1);}

#define WHERE __FILE__<<" | "<<__func__<<":"<<__LINE__

#define CHECK_SDL_ERROR std::cerr<<"[ERROR] "<<WHERE<<"\t\t"<<SDL_GetError()<<END_LINE

#define REPORT_DEBUG(msg) if(DEBUG){std::cout<<"[DEBUG]"<<WHERE<<msg<<END_LINE;}
#define REPORT_DEBUG2(cond, msg) if(cond||DEBUG){std::cout<<"[DEBUG]"<<WHERE<<msg<<END_LINE;}
#define REPORT_DEBUG2_AS_ERROR(cond, msg) if(cond||DEBUG){std::cerr<<"[DEBUG]"<<WHERE<<msg<<END_LINE;}

#define REPORT_I_WAS_HERE if(DEBUG){std::cout <<"[DEBUG] I was here!\t"<<WHERE<<END_LINE;}

#define TEMP_REPORT_I_WAS_HERE if(1){std::cout<<"[DEBUG] I was here!\t"<<WHERE<<END_LINE;}

#ifdef USE_AT
	#define ELEMENT_ACESS(container, position) container.at(position)
#else
	#define ELEMENT_ACESS(container, position) container[position]
#endif

typedef unsigned int uint;

#define STRLEN(s) (sizeof(s)/sizeof(s[0]))

#define COMPARE(X){if(X != other.X){std::cout<<"[WARNING]\t"<<WHERE<<"\tComparation failed: ours: " << X << "\t other: " << other.X <<END_LINE;}}

#define BASIC_COMPARE(X){if(X != other.X){std::cout<<"[WARNING]\t"<<WHERE<<"\tComparation failed. " <<END_LINE;}}

//void Error(char const * errMsg);

#endif // ERROR_H
