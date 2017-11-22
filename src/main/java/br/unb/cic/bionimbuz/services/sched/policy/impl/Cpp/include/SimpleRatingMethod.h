#ifndef SIMPLERATINGMETHOD_H
#define SIMPLERATINGMETHOD_H

#include "RatingMethod.h"

class SimpleRatingMethod: public RatingMethod{
	public:
		double Rate(PluginInfo const &spec);
};

#endif // SIMPLERATINGMETHOD_H
