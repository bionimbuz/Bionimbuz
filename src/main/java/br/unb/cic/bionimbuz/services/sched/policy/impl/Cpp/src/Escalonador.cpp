#include <queue>
#include <array>
#include <string>
#include <algorithm>
#include "Escalonador.hpp"

using std::priority_queue;
using std::array;
using std::string;

std::string ScheduleResult::Serialize() const
{
	std::string ret= jobId;
	ret= ret + '\n';
	ret= pluginInfoId;
	ret= ret + '\n';
	ret= std::to_string(whereToRun);
	
	return ret;
}



