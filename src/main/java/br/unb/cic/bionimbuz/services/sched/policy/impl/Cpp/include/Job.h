#ifndef JOB_H
#define JOB_H

#include <stdio.h>
#include<string>
#include <stdint.h>
#include <vector>
#include "Serializable.h"
#include "FileInfo.h"


class Job : public Serializable<Job>
{
	public:
		bool operator==(Job const &b);
		std::string Serialize();
		Job(std::string const &data);
		Job(void){};
		static bool TestSerialization(void);
//	private:
		std::string id;
		int64_t testId;
		std::string localID, serviceId, args;
		std::vector<std::string> ipJob;//ver o que Transient significa
		std::vector<FileInfo> inputFiles;
		std::string inputURL;
		std::vector<std::string> outputs;
		int64_t timestamp;
		double worstExecution;
		std::vector<std::string> dependecies;
		std::string referenceFile;
		//adição
		bool useGPU, useCPU;
		float gpuPref;
};

#endif // JOB_H
