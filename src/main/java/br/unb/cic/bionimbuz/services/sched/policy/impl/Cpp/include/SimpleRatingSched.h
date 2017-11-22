#ifndef SIMPLERATINGSCHED_H
#define SIMPLERATINGSCHED_H

#include "Escalonador.hpp"
#include "SimpleRatingMethod.h"

class SimpleRatingSched: public Escalonador{
	public:
		std::vector<ScheduleResult> Schedule(std::vector<Job> const &jobs, std::unordered_map<std::string, PluginInfo> const &cloudMap);
		std::string GetPolicyName(void);
	private:
		SimpleRatingMethod ratingMethod;
};

#endif // SIMPLERATINGSCHED_H
