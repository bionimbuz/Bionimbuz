#include "Comunicador.hpp"

#include <string>
#include <vector>
#include <net/if.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>

#include <thread>
#include <pthread.h>

#include "Error.h"
#include "SimpleRatingSched.h"

#define BIONIMBUZ_PREFIX_SCHED "SCHED="
#define BIONIMBUZ_SCHED_SIMPLE_RATING_SCHED "SIMPLE_RATING_SCHED"
#define BIONIMBUZ_UNKNOWN_SCHED "Fail_UNKNOWN_SCHED"
#define BIONIMBUZ_STATUS_OK "STATUS_OK"
#define BIONIMBUZ_SCHED_DEFINED "[SchedTypeAwnser]SCHEDULER_DEFINED"

Comunicador::Comunicador(int port, int64_t handShakeMsg)
{
	TEMP_REPORT_I_WAS_HERE;
	
	java.sin6_addr= in6addr_loopback;
	java.sin6_family= AF_INET6;
	java.sin6_port= htons(port);
	java.sin6_scope_id= if_nametoindex("lo");//loopback, ver se esse nome é padronizado
	TEMP_REPORT_I_WAS_HERE;
	if(0 == java.sin6_scope_id)
	{
		Error("Could not find the loopBackInterface.");
	}
	
	socketFD= socket(AF_INET6, SOCK_DGRAM, 0);
	TEMP_REPORT_I_WAS_HERE;
	if (socketFD < 0)
	{
		Error("[ERROR] Fail opening socket\n");
	}
	
	buffer = new char[BUFFER_SIZE];		//desalocar no destrutor
	memset(buffer, 0, BUFFER_SIZE*sizeof(char));
	sprintf(buffer, "%ld", handShakeMsg);
	TEMP_REPORT_I_WAS_HERE;
	bytesReadOrWritten= sendto(socketFD, buffer, strlen(buffer), 0, (struct sockaddr*)&java, sizeof(sockaddr_in6));
	TEMP_REPORT_I_WAS_HERE;
	if(bytesReadOrWritten < 0)
	{
		Error("[ERROR] Error writing to socket\n");
	}
	uint recievedSocketLenght= sizeof(sockaddr_storage);
	sockaddr_storage recievedSocket;
	TEMP_REPORT_I_WAS_HERE;
	bytesReadOrWritten= recvfrom(socketFD, buffer, BUFFER_SIZE, 0, (struct sockaddr*) &recievedSocket, &recievedSocketLenght);
	TEMP_REPORT_I_WAS_HERE;
	if (bytesReadOrWritten < 0)
	{
		Error("[ERROR] Error reading from socket\n");
	}
	if( ( ( (SocketAddress*)&recievedSocket) )->sin6_port != java.sin6_port)
	{
		Error("Expected msg from the same port we sent to");
	}
	if(NULL == strstr(buffer, "Ack!"))
	{
		Error("HandShake Failed!" << buffer);
	}
	TEMP_REPORT_I_WAS_HERE;
	printf("Handshake sucess!");
	
	DefineSched();
	Receive("REQUESTING_EXIT");
}

std::string Comunicador::Receive(std::string begin)
{
	uint recievedSocketLenght= sizeof(sockaddr_storage);
	sockaddr_storage recievedSocket;
	do{
		memset(buffer, '\0', BUFFER_SIZE);
		bytesReadOrWritten= recvfrom(socketFD, buffer, BUFFER_SIZE, 0, (struct sockaddr*) &recievedSocket, &recievedSocketLenght);
		ASSERT(bytesReadOrWritten>0);
		if( ( ( (SocketAddress*)&recievedSocket) )->sin6_port != java.sin6_port){
			std::cout<< "Received message from wrong origin, ignoring it. Message: "<< buffer;
		}
//		if(NULL == strstr(buffer, begin.c_str()) && 0 != strcmp("", begin.c_str() ) ){
//			std::cout<< "Received invalid message, ignoring it. Message: "<< buffer;
//		}
//		else{
			int size= BUFFER_SIZE;
			char delimiter[2];
			delimiter[0]= '\r';
			delimiter[1]= '\0';
			char *token;
			if(0 == memcmp("SCHEDULE" , token= strtok(buffer, delimiter), STRLEN("SCHEDULE") ) ){
				Schedule();
			}
			else if(NULL == strstr(buffer, begin.c_str()) && 0 != strcmp("", begin.c_str() ) ){
							std::cout<< "Received invalid message, ignoring it. Message: "<< buffer;
			}
			else{
				break;
			}
	}
	while(1);
	return buffer;
}


void Comunicador::DefineSched(void){
	bool success= false;
	do{
		std::string msg= Receive("");
		if(std::string::npos != msg.find(BIONIMBUZ_SCHED_SIMPLE_RATING_SCHED)){
			sched= new SimpleRatingSched();
			success=true;
			bytesReadOrWritten= sendto(socketFD, BIONIMBUZ_SCHED_DEFINED, STRLEN(BIONIMBUZ_SCHED_DEFINED), 0, (struct sockaddr*)&java, sizeof(sockaddr_in6));
		}
		else{
			bytesReadOrWritten= sendto(socketFD, BIONIMBUZ_UNKNOWN_SCHED, STRLEN(BIONIMBUZ_UNKNOWN_SCHED), 0, (struct sockaddr*)&java, sizeof(sockaddr_in6));
			ASSERT2(bytesReadOrWritten > 0, "[ERROR] Error writing to socket\n");
		}
	}
	while(!success);
}

class StringForPluginInfo{
	public:
		std::unordered_map<std::string, PluginInfo>* map;
		pthread_mutex_t *mutex;
		std::string key;
		std::string strToProcess;
		
		StringForPluginInfo(
				std::unordered_map<std::string, PluginInfo>* map,
				pthread_mutex_t *mutex,
				std::string key,
				std::string strToProcess
				):
			map(map),
			mutex(mutex),
			key(key),
			strToProcess(strToProcess){}
};

void* CreatePluginInfo(void* args_)
{
	StringForPluginInfo *args;
	args= (StringForPluginInfo*)args_;
	PluginInfo pluginInfo(args->strToProcess.c_str());
	pthread_mutex_lock(args->mutex);
	(*(args->map) )[args->key]= pluginInfo;
	pthread_mutex_unlock(args->mutex);
	delete(args);
	return NULL;
}

class StringForJob{
	public:
		std::vector<Job>* jobs;
		pthread_mutex_t *mutex;
		std::string strToProcess;
		StringForJob(
				std::vector<Job>* jobs,
				pthread_mutex_t *mutex,
				std::string strToProcess
				):
			jobs(jobs),
			mutex(mutex),
			strToProcess(strToProcess){}
};

void* CreateJob(void* args_)
{
	StringForJob *args;
	args= (StringForJob*)args_;
	Job job(args->strToProcess.c_str());
	pthread_mutex_lock(args->mutex);
	args->jobs->push_back(job);
	pthread_mutex_unlock(args->mutex);
	delete(args);
	return NULL;
}

void Comunicador::Schedule(void){
	int size= BUFFER_SIZE;
	char delimiter[2];
	delimiter[0]= '\r';
	delimiter[1]= '\0';
	char *token;
//	ASSERT(0 == memcmp("SCHEDULE" , token= strtok(buffer, delimiter), STRLEN("SCHEDULE") ) );
//	buffer[TEMP_BUFFER_SIZE]= '\0';
	
	std::vector<std::string> jobList;
	int vecSize;
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "JOBS=%d", &vecSize) );
	REPORT_DEBUG2_AS_ERROR(true, "vecSize=  " << vecSize << endl);
	jobList.reserve(vecSize);
	REPORT_DEBUG("token= " << token << "\n");
	for(int i=0; i < vecSize; i++){
		token= strtok(NULL, delimiter);
		REPORT_DEBUG2_AS_ERROR(true, "token= " << token << " and have size " << strlen(token) << endl);
		ASSERT(1 == sscanf(token, "%[^\r\n]", buffer) );
		jobList.emplace_back(token);
	}
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG2_AS_ERROR(true, "token= " << ( (NULL == token)? "NULO!" : token ) << " and have size " << strlen(token) << endl);
	ASSERT(1 == sscanf(token, "PLUGININFOS=%d", &vecSize) );
	std::vector<std::string> pluginStrings;
	pluginStrings.reserve(vecSize);
	std::vector<std::string> keyStrings;
	keyStrings.reserve(vecSize);
	std::unordered_map<std::string, PluginInfo> pluginInfos;
	pluginInfos.reserve(vecSize);
	
	for(int i=0; i < vecSize; i++){
		REPORT_DEBUG2_AS_ERROR(true, "token= " << ( (NULL == token)? "NULO!" : token ) << " vecSize= " << vecSize << "\n");
		if(NULL == token){
			break;
		}
		token= strtok(NULL, delimiter);
		ASSERT(1 == sscanf(token, "%[^\r\n]", buffer) );
		keyStrings.emplace_back(token);
	
		token= strtok(NULL, delimiter);
		REPORT_DEBUG("token= " << token << "\n");
		ASSERT(1 == sscanf(token, "%[^\r\n]", buffer) );
		pluginStrings.emplace_back(token);
	}

	for(int i=0; i< vecSize; i++)
	{
		pluginInfos.emplace(keyStrings[i], pluginStrings[i] );
	}

	std::vector<Job> jobs;
	jobs.reserve(jobList.size());
	for(int i=0; i< vecSize; i++)
	{
		jobs.emplace_back(jobList[i]);
	}
	REPORT_DEBUG2_AS_ERROR(true, "token= " << ( (NULL == token)? "NULO!" : token ) << " and have size " << strlen(token) << endl);


	
	std::vector<ScheduleResult> results= sched->Schedule(jobs, pluginInfos);
	
	std::string message= "Results=";
	message= message+ std::to_string(results.size());
	message= message+= '\r';
	for(unsigned int i=0; i < results.size(); i++)
	{
		message= message + results[i].Serialize();
		message= message + '\r';
	}
	REPORT_DEBUG2(true, "size= " << message.size() << "\t strlen= " << strlen(message.c_str() ) );
	bytesReadOrWritten= sendto(socketFD, message.c_str(), message.size(), 0, (struct sockaddr*)&java, sizeof(sockaddr_in6));
}

