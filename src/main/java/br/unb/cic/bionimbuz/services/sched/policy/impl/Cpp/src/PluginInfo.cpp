#include <stdio.h>
#include <string.h>
#include <math.h>
#include "PluginInfo.h"
#include "Error.h"

#define TEMP_BUFFER_SIZE (200)

std::string PluginInfo::Serialize(void){
	std::string ret= "PLUGIN_INFO";
	ret+= '\n';
	
	ret+= "id=";
	ret+= id;
	ret+= '\n';
	
	ret+= "instanceName=";
	ret+= instanceName;
	ret+= '\n';
	
	ret+= "privateCloud=";
	ret+= std::to_string(privateCloud);
	ret+= '\n';
	
	ret+= "host=";
	ret+= host.adderss;
	ret+= ':';
	ret += std::to_string(host.port);
	ret+= '\n';
	
	ret+= "upTime=";
	ret+= std::to_string(upTime);
	ret+= '\n';
	
	ret+= "latency=";
	ret+= std::to_string(latency);
	ret+= '\n';
	
	ret+= "costPerGiga=";
	ret+= std::to_string(costPerGiga);
	ret+= '\n';
	
	ret+= "timestamp=";
	ret+= std::to_string(timestamp);
	ret+= '\n';
	
	ret+= "numCores=";
	ret+= std::to_string(numCores);
	ret+= '\n';
	
	ret+= "numNodes=";
	ret+= std::to_string(numNodes);
	ret+= '\n';
	
	ret+= "numOccupied=";
	ret+= std::to_string(numOccupied);
	ret+= '\n';
	
	ret+= "ranking=";
	ret+= std::to_string(ranking);
	ret+= '\n';
	
	ret+= "fsSize=";
	ret+= std::to_string(fsSize);
	ret+= '\n';
	
	ret+= "memoryTotal=";
	ret+= std::to_string(memoryTotal);
	ret+= '\n';
	
	ret+= "memoryFree=";
	ret+= std::to_string(memoryFree);
	ret+= '\n';
	
	ret+= "currentFrequencyCore=";
	ret+= std::to_string(currentFrequencyCore);
	ret+= '\n';
	
	ret+= "costPerHour=";
	ret+= std::to_string(costPerHour);
	ret+= '\n';
	
	ret+= "bandwith=";
	ret+= std::to_string(bandwith);
	ret+= '\n';
	
	ret+= "ip=";
	ret+= ip;
	ret+= '\n';
	
	ret+= "provider=";//obs:: n pode ter \n no provider
	ret+= provider;
	ret+= '\n';
	
	//adições para suporte de GPU
	ret+= "gpuMemoryTotal=";
	ret+= std::to_string(gpuMemoryTotal);
	ret+= '\n';
	
	ret+= "gpuMemoryMaxFrequency=";
	ret+= std::to_string(gpuMemoryMaxFrequency);
	ret+= '\n';
	
	ret+= "gpuMemoryBus=";
	ret+= std::to_string(gpuMemoryBus);
	ret+= '\n';
	
	ret+= "gpuMemoryBandwith=";
	ret+= std::to_string(gpuMemoryBandwith);
	ret+= '\n';
	
	ret+= "gpuFloatingPointPerf=";
	ret+= std::to_string(gpuFloatingPointPerf);
	ret+= '\n';
	
	ret+= "gpuMaxFrequency=";
	ret+= std::to_string(gpuMaxFrequency);
	ret+= '\n';
	
	return ret;
}


PluginInfo::PluginInfo(std::string const &str){
/*
	char tempID[TEMP_BUFFER_SIZE], tempInstanceName[TEMP_BUFFER_SIZE], 
	int res= sscanf(
				str.c_str(),
				"PLUGIN_INFO id=%s instanceName=%s privateCloud=%d host=%[^:]:%d upTime=%ld latency=%lf costPerGiga=%lf timestamp=%ld numCores=%d numNodes=%d numOccupied=%d ranking=%lf fsSize=%f memoryTotal=%lf memoryFree=%lf currentFrequencyCore=%lf costPerHour=%lf bandwith=%lf ip=%s provider=%s gpuMemoryTotal=%lf gpuMemoryMaxFrequency=%lf gpuMemoryBus=%lf gpuMemoryBandwith=%lf gpuFloatingPointPerf%lf gpuMaxFrequency=%lf"
				
				);
*/
	int size= str.length();
	char *temp= (char*)operator new[] (size+1);
	memcpy(temp, str.c_str(), size);
	temp[size]= '\0';
	
	char delimiter[2];
	delimiter[0]= '\n';
	delimiter[1]= '\0';
	char* aux= strtok(temp, delimiter);
	ASSERT2(0 == memcmp("PLUGIN_INFO" , aux, STRLEN("PLUGIN_INFO") )
							|| 0 == memcmp("value=PLUGIN_INFO" , aux, STRLEN("value=PLUGIN_INFO") ), "expecting PLUGIN_INFO, got " << temp );
	char *token, buffer[TEMP_BUFFER_SIZE+1];
	buffer[TEMP_BUFFER_SIZE]= '\0';
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "id=%[^\n]", buffer) );
	id= buffer;
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "instanceName=%[^\n]", buffer) );
	instanceName= buffer;
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "privateCloud=%d", &privateCloud) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(2 == sscanf(token, "host=%[^:]:%d", buffer, &(host.port) ) );
	host.adderss= buffer;
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "upTime=%ld", &upTime) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "latency=%lf", &latency) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "costPerGiga=%lf", &costPerGiga) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "timestamp=%ld", &timestamp) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "numCores=%d", &numCores) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "numNodes=%d", &numNodes) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "numOccupied=%d", &numOccupied) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "ranking=%lf", &ranking) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "fsSize=%f", &fsSize) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "memoryTotal=%lf", &memoryTotal) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "memoryFree=%lf", &memoryFree) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "currentFrequencyCore=%lf", &currentFrequencyCore) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "costPerHour=%lf", &costPerHour) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	bandwith= NAN;
//	ASSERT(1 == sscanf(token, "bandwith=%lf", &bandwith) );
	if(0 == sscanf(token, "bandwith=%lf", &bandwith)){
		if(0 == memcmp("bandwith=null", token, STRLEN("bandwith=null") ) ){
			bandwith= 0;
		}
	}
	if(NAN == bandwith){
		Error("Could not read bandwith, found it: " << token);
	}
	REPORT_DEBUG2(1, "\ttoken= " <<token<< "\tbandwith = "<< bandwith)
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "ip=%[^\n]", buffer) );
	ip= buffer;
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "provider=%[^\n]", buffer) );
	provider= buffer;
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "gpuMemoryTotal=%lf", &gpuMemoryTotal) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "gpuMemoryMaxFrequency=%lf", &gpuMemoryMaxFrequency) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "gpuMemoryBus=%lf", &gpuMemoryBus) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "gpuMemoryBandwith=%lf", &gpuMemoryBandwith) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "gpuFloatingPointPerf=%lf", &gpuFloatingPointPerf) );
	
	token= strtok(NULL, delimiter);
	REPORT_DEBUG("\ttoken= " << token << "\n");
	ASSERT(1 == sscanf(token, "gpuMaxFrequency=%lf", &gpuMaxFrequency) );
	
	operator delete[](temp);
	
}

bool PluginInfo::operator==(PluginInfo const &other){
	bool ret= true;
	
	COMPARE(id);
	ret= ret && (id==other.id);
	
	COMPARE(instanceName);
	ret= ret && (instanceName==other.instanceName);
	
	COMPARE(privateCloud);
	ret= ret && (privateCloud==other.privateCloud);
	
	BASIC_COMPARE(host);
	ret= ret && (host==other.host);
	
	COMPARE(upTime);
	ret= ret && (upTime==other.upTime);
	
	COMPARE(latency);
	ret= ret && (latency==other.latency);
	
	COMPARE(costPerGiga);
	ret= ret && (costPerGiga==other.costPerGiga);
	
	COMPARE(timestamp);
	ret= ret && (timestamp==other.timestamp);
	
	COMPARE(numCores);
	ret= ret && (numCores==other.numCores);
	
	COMPARE(ranking);
	ret= ret && (ranking==other.ranking);
	
	COMPARE(fsSize);
	ret= ret && (fsSize==other.fsSize);
	
	COMPARE(memoryTotal);
	ret= ret && (memoryTotal==other.memoryTotal);
	
	COMPARE(memoryFree);
	ret= ret && (memoryFree==other.memoryFree);
	
	COMPARE(currentFrequencyCore);
	ret= ret && (currentFrequencyCore==other.currentFrequencyCore);
	
	COMPARE(costPerHour);
	ret= ret && (costPerHour==other.costPerHour);
	
	COMPARE(bandwith);
	ret= ret && (bandwith==other.bandwith);
	
	COMPARE(ip);
	ret= ret && (ip==other.ip);
	
	COMPARE(provider);
	ret= ret && (provider==other.provider);
	
	COMPARE(gpuMemoryTotal);
	ret= ret && (gpuMemoryTotal==other.gpuMemoryTotal);
	
	COMPARE(gpuMemoryMaxFrequency);
	ret= ret && (gpuMemoryMaxFrequency==other.gpuMemoryMaxFrequency);
	
	COMPARE(gpuMemoryBus);
	ret= ret && (gpuMemoryBus==other.gpuMemoryBus);
	
	COMPARE(gpuMemoryBandwith);
	ret= ret && (gpuMemoryBandwith==other.gpuMemoryBandwith);
	
	COMPARE(gpuFloatingPointPerf);
	ret= ret && (gpuFloatingPointPerf==other.gpuFloatingPointPerf);
	
	COMPARE(gpuMaxFrequency);
	ret= ret && (gpuMaxFrequency==other.gpuMaxFrequency);
	
	return ret;
}

bool PluginInfo::TestSerialization(void){
	PluginInfo a;
	a.id= "idTeste";
	a.instanceName= "testInstanceName";
	a.privateCloud= 14;
	a.host= Host("1.2.3.4", 31415);
	a.upTime= 80040234823;
	a.latency= 123.56456;
	a.costPerGiga= 567.989;
	a.timestamp= 123;
	a.numCores= 2;
	a.numNodes=3;
	a.numOccupied=4;
	a.ranking= 2234;
	a.fsSize= 45456.234;
	a.memoryTotal= 765.4;
	a.memoryFree= 654.3;
	a.currentFrequencyCore= 12345.6;
	a.costPerHour=9.87;
	a.bandwith= 9.76;
	a.ip= "1.2.3.4";
	a.provider= "eu.mesmo";
	a.gpuMemoryTotal= 123.123;
	a.gpuMemoryMaxFrequency= 234.234;
	a.gpuMemoryBus= 345.345;
	a.gpuMemoryBandwith= 456.456;
	a.gpuFloatingPointPerf= 567.567;
	a.gpuMaxFrequency= 678.678;
	
	std::string serializated= a.Serialize();
	printf("\n------\n%s\n------\n", serializated.c_str());
	PluginInfo b(serializated);
	return a==b;
}


