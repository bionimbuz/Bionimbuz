#include "SimpleRatingSched.h"
#include <algorithm>


bool JobComparer(Job const &a, Job const &b){
	return a.worstExecution > b.worstExecution;
}

bool PluginInfoComparer(PluginInfo const &a, PluginInfo const &b){
	return a.ranking > b.ranking;
}

bool PluginInfoComparerByGPU(PluginInfo const &a, PluginInfo const &b){
	return a.gpuFloatingPointPerf > b.gpuFloatingPointPerf;
}


std::vector<ScheduleResult> SimpleRatingSched::Schedule(std::vector<Job> const &jobs, std::unordered_map<std::string, PluginInfo> const &cloudMap){
	std::vector<Job> sortedJobs(jobs.begin(), jobs.end());
	std::sort(sortedJobs.begin(), sortedJobs.end(), JobComparer);
//	std::vector<PluginInfo> machines((*cloudMap).begin(), (*cloudMap).end());
	std::vector<PluginInfo> machines;
	for(auto it= (cloudMap).begin(); it != (cloudMap).end(); it++){
		machines.push_back(it->second);
	}
	std::sort(machines.begin(), machines.end(), PluginInfoComparer);
	
	std::vector<PluginInfo>GPUs= machines;
	std::sort(machines.begin(), machines.end(), PluginInfoComparerByGPU);
	//TODO: otimizar: inverter ordemd e busca
	for(auto i= GPUs.begin(); i != GPUs.end(); i++){
		if(0 == (*i).gpuFloatingPointPerf){
			GPUs.erase(i, GPUs.end());
			break;
		}
	}
	
	std::vector<ScheduleResult> result;
	
	while(! (sortedJobs.empty() || (sortedJobs[0].useGPU && GPUs.empty() && machines.empty() ) ||
			 (!sortedJobs[0].useGPU && machines.empty() ) ) ){
		Job const &aux= sortedJobs[0];
		bool onGPU=false;
		if(aux.useGPU){
			if(aux.worstExecution*aux.gpuPref < aux.worstExecution){
				onGPU=true;
				//escalonar na GPU
				result.emplace_back(aux.id, GPUs[0].id, 0x2);
				GPUs.erase(GPUs.begin());
				sortedJobs.erase(jobs.begin());
			}
		}
		if(!onGPU){
			//escalonar na CPU
			result.emplace_back(aux.id, machines[0].id, 0x1);
			machines.erase(machines.begin());
			sortedJobs.erase(jobs.begin());
		}
	}
	return result;
	
	
}

std::string SimpleRatingSched::GetPolicyName(void){
	return "SimpleRatingSched";
}

