#include <queue>
#include <array>
#include <string>
#include <algorithm>
#include "Escalonador.hpp"
#include "Error.h"

using std::priority_queue;
using std::array;
using std::string;

std::string ScheduleResult::Serialize() const
{
	std::string ret= jobId;
	ret= ret + '\n';
	ret= ret + pluginInfoId;
	ret= ret + '\n';
	ret= ret+ std::to_string(whereToRun);

	REPORT_DEBUG2(true, "jobId= " << jobId << " \tpluginInfoId= " << pluginInfoId << " \t whereToRun= " << (uint)whereToRun);
	REPORT_DEBUG2(true, "resultadoSerializado= " << ret);
	
	return ret;
}



