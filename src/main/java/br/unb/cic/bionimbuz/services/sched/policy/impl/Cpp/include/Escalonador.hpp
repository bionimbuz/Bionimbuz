#ifndef ESCALONADOR_HPP
#define ESCALONADOR_HPP

#include <stdint.h>
#include <vector>
#include <unordered_map>
#include <memory>
#include <utility>
#include <string>

#include "PluginInfo.h"
#include "Job.h"

struct ScheduleResult
{
	public:
		std::string jobId;
		std::string pluginInfoId;
		uint8_t whereToRun;//0x1= CPU, 0x2= GPU, 0x3= CPU&GPU
		ScheduleResult(
				std::string jobId,
				std::string pluginInfoId,
				uint8_t whereToRun
				):
			jobId(jobId),
			pluginInfoId(pluginInfoId),
			whereToRun(whereToRun){}
		std::string Serialize(void) const;
};

class Escalonador
{
	public:
		virtual std::vector<ScheduleResult> Schedule(std::vector<Job> const &jobs, std::unordered_map<std::string, PluginInfo> const &cloudMap)=0;
//		vector<PluginTask> Relocate(vector<std::pair<Job, PluginTask> > taskPairs);
//		void CancelJobEvent(PluginTask task);
		virtual std::string GetPolicyName(void) =0;
//		void UpdateCloudMap(std::unique_ptr<std::unordered_map<std::string, PluginInfo> > newMap);
	protected:
//		std::unique_ptr<std::unordered_map<std::string, PluginInfo> > cloudMap;
};

#endif
