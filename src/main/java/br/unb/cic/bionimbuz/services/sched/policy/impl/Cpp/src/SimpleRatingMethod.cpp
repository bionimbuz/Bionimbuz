#include "SimpleRatingMethod.h"

double SimpleRatingMethod::Rate(PluginInfo const &spec){
	return spec.ranking;
	return spec.numCores*spec.memoryTotal/(spec.costPerGiga*spec.costPerHour);
}

