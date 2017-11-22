#include <string.h>
#include <stdio.h>
#include "Job.h"
#include "Error.h"

#define TEMP_BUFFER_SIZE (200)

std::string Job::Serialize(){
	std::string ret= "JOB";
	ret+= '\n';
	
	ret+= "id=";
	ret+= id;
	ret+= '\n';
	
	ret+= "testId=";
	ret+= std::to_string(testId);
	ret+= '\n';
	
	ret+= "localID=";
	ret+= localID;
	ret+= '\n';
	
	ret+= "serviceId=";
	ret+= serviceId;
	ret+= '\n';
	
	ret+= "args=";
	ret+= args;
	ret+= '\n';
	
	ret+= "ipJob=";
	ret+= std::to_string(ipJob.size() );
	for(uint i=0; i < ipJob.size(); i++){
		ret+= '>';
		ret+= ipJob[i];
	}
	
	ret+="\ninputFiles=";
	ret+= std::to_string(inputFiles.size() );
	for(uint i=0; i < inputFiles.size(); i++){
		ret+= '>';
		ret+= inputFiles[i].Serialize();
	}
	
	ret+= "\ninputURL=";
	ret+= inputURL;
	ret+= '\n';
	
	ret+= "outputs=";
	ret+= std::to_string(outputs.size() );
	for(uint i=0; i < outputs.size(); i++){
		ret+= '>';
		ret+= outputs[i];
	}
	
	ret+= "\ntimestamp=";
	ret+= std::to_string(timestamp);
	ret+= '\n';
	
	ret+= "worstExecution=";
	ret+= std::to_string(worstExecution);
	ret+= '\n';
	
	ret+= "dependecies=";
	ret+= std::to_string(dependecies.size() );
	for(uint i=0; i < dependecies.size(); i++){
		ret+= '>';
		ret+= dependecies[i];
	}
	
	ret+= "\nreferenceFile=";
	ret+= referenceFile;
	ret+= '\n';
	
	ret+= "useGPU=";
	ret+= useGPU?"true":"false";
	ret+= '\n';
	
	ret+= "useCPU=";
	ret+= useCPU?"true":"false";
	ret+= '\n';
	
	ret+= "gpuPref=";
	ret+= std::to_string(gpuPref);
	ret+= '\n';
	
	return ret;
}

Job::Job(std::string const &str){
	int size= str.length();
	char *temp= (char*)operator new[] (size+1);
	memcpy(temp, str.c_str(), size);
	temp[size]= '\0';
	
	char delimiter[2];
	delimiter[0]= '\n';
	delimiter[1]= '\0';
	ASSERT(0 == memcmp("JOB" , strtok(temp, delimiter), STRLEN("JOB") ) );
	char *token, buffer[TEMP_BUFFER_SIZE+1];
	buffer[TEMP_BUFFER_SIZE]= '\0';
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "id=%[^\n]", buffer) );
	id= buffer;
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "testId=%ld", &testId) );
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "localID=%[^\n]", buffer) );
	localID= buffer;
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "serviceId=%[^\n]", buffer) );
	serviceId= buffer;
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "args=%[^\n]", buffer) );
	args= buffer;
	
	int vecSize;
	token= strtok(NULL, ">\n");
	ASSERT(1 == sscanf(token, "ipJob=%d>", &vecSize) );
	REPORT_DEBUG("token= " << token << "\n");
	ipJob.resize(vecSize);
	for(int i=0; i < vecSize; i++){
		token= strtok(NULL, ">\n");
		REPORT_DEBUG("token= " << token << "\n");
		ASSERT(1 == sscanf(token, "%[^\n]", buffer) );
		ipJob[i]= buffer;
	}
	
	token= strtok(NULL, ">\n");
	ASSERT(1 == sscanf(token, "inputFiles=%d>", &vecSize) );
	REPORT_DEBUG("token= " << token << "\n");
	inputFiles.resize(vecSize);
	for(int i=0; i < vecSize; i++){
		inputFiles[i]= FileInfo(str);
	}
	
	token= strtok(NULL, delimiter);
	ASSERT2(1 == sscanf(token, "inputURL=%[^\n]", buffer), "token = " << token );
	inputURL= buffer;
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, ">\n");
	ASSERT(1 == sscanf(token, "outputs=%d>", &vecSize) );
	outputs.resize(vecSize);
	for(int i=0; i < vecSize; i++){
		token= strtok(NULL, ">\n");
		ASSERT(1 == sscanf(token, "%[^\n]", buffer) );
		REPORT_DEBUG("token= " << token << "\n");
		outputs[i]= buffer;
	}
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "timestamp=%ld", &timestamp) );
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "worstExecution=%lf", &worstExecution) );
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, ">\n");
	ASSERT(1 == sscanf(token, "dependecies=%d>", &vecSize) );
	REPORT_DEBUG("token= " << token << "\n");
	dependecies.resize(vecSize);
	for(int i=0; i < vecSize; i++){
		token= strtok(NULL, ">\n");
		ASSERT(1 == sscanf(token, "%[^\n]", buffer) );
		REPORT_DEBUG("token= " << token << "\n");
		dependecies[i]= buffer;
	}
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "referenceFile=%[^\n]", buffer) );
	REPORT_DEBUG("token= " << token << "\n");
	referenceFile= buffer;
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "useGPU=%[^\n]", buffer) );
	REPORT_DEBUG("token= " << token << "\n");
	useGPU= !strcmp(buffer, "true");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "useCPU=%[^\n]", buffer) );
	REPORT_DEBUG("token= " << token << "\n");
	useCPU= !strcmp(buffer, "true");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "gpuPref=%f", &gpuPref) );
	REPORT_DEBUG("token= " << token << "\n");
	
	operator delete[](temp);
}

bool Job::operator==(Job const &other){
	bool ret= true;
	COMPARE(id);
	ret= ret && (id==other.id);
	COMPARE(testId);
	ret= ret && (testId == other.testId);
	COMPARE(localID);
	ret= ret && (localID == other.localID);
	COMPARE(serviceId);
	ret= ret && (serviceId == other.serviceId);
	COMPARE(args);
	ret= ret && (args == other.args);
	BASIC_COMPARE(ipJob);
	ret= ret && (ipJob == other.ipJob);
	BASIC_COMPARE(inputFiles);
	ret= ret && (inputFiles == other.inputFiles);
	COMPARE(inputURL);
	ret= ret && (inputURL == other.inputURL);
	BASIC_COMPARE(outputs);
	ret= ret && (outputs == other.outputs);
	COMPARE(timestamp);
	ret= ret && (timestamp == other.timestamp);
	COMPARE(worstExecution);
	ret= ret && (worstExecution == other.worstExecution);
	BASIC_COMPARE(dependecies);
	ret= ret && (dependecies == other.dependecies);
	COMPARE(referenceFile);
	ret= ret && (referenceFile == other.referenceFile);
	COMPARE(useCPU);
	ret= ret && (useCPU == other.useCPU);
	COMPARE(useGPU);
	ret= ret && (useGPU == other.useGPU);
	COMPARE(gpuPref);
	ret= ret && (gpuPref == other.gpuPref);
	return ret;
}

bool Job::TestSerialization(void){
	Job a;
	a.id = "testeId";
	a.testId= 1;
	a.localID= "testelLocalID";
	a.serviceId= "testelServiderId";
	a.args= "testeargs";
	a.ipJob.push_back("Teste1");
	a.ipJob.push_back("Teste2");
	a.inputFiles.push_back(FileInfo(
							"fileInfo1",
							"fileinfo1Name",
							10,
							11,
							"uploadTimestamp1",
							"hash1",
							"bucket1"));
	a.inputFiles.push_back(FileInfo(
							"fileInfo2",
							"fileinfo2Name",
							12,
							122,
							"uploadTimestamp2",
							"hash2",
							"bucket2"));
	a.inputURL= "uma.url";
	a.outputs.push_back("output1");
	a.outputs.push_back("output2");
	a.timestamp= 123123;
	a.worstExecution= 123.35678;
	a.dependecies.push_back("dep1");
	a.dependecies.push_back("dep2");
	a.referenceFile= "ref";
	a.useCPU= true;
	a.useGPU= false;
	a.gpuPref= 1.27;
	std::string serializated= a.Serialize();
	printf("\n------\n%s\n------\n", serializated.c_str());
	Job b(serializated);
	return a==b;
}



