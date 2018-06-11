#include <string.h>
#include "FileInfo.h"
#include "Error.h"

#define TEMP_BUFFER_SIZE (1024)

FileInfo::FileInfo(std::string id,
		std::string name,
		int64_t size,
		int64_t userId,
		std::string uploadTimestamp,
		std::string hash,
		std::string bucket):
	id(id),
	name(name),
	size(size),
	userId(userId),
	uploadTimestamp(uploadTimestamp),
	hash(hash),
	bucket(bucket){}


std::string FileInfo::Serialize(void){
	std::string ret= "FILE_INFO";
	ret+= '\n';
	
	ret+= "id=";
	ret+= id;
	ret+= '\n';
	
	ret+= "name=";
	ret+= name;
	ret+= '\n';
	
	ret+= "size=";
	ret+= std::to_string(size);
	ret+= '\n';
	
	ret+= "userId=";
	ret+= std::to_string(userId);
	ret+= '\n';
	
	ret+= "uploadTimestamp=";
	ret+= uploadTimestamp;
	ret+= '\n';
	
	ret+= "hash=";
	ret+= hash;
	ret+= '\n';
	
	ret+= "bucket=";
	ret+= bucket;
	ret+= '\n';
	
	return ret;
}

FileInfo::FileInfo(std::string const & serializedData){
	char delimiter[2];
	delimiter[0]= '\n';
	delimiter[1]= '\0';
	
	char *token, buffer[TEMP_BUFFER_SIZE+1];
	buffer[TEMP_BUFFER_SIZE]= '\0';
	
	token= strtok(NULL, delimiter);
	ASSERT2(1 == sscanf(token, "%[^\n]", buffer), "token = " << token);
	ASSERT2( (!strcmp(token, "FILE_INFO") || (!strcmp(token, ">FILE_INFO") ) ) , "token = " << token);
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT2(1 == sscanf(token, "id=%[^\n]", buffer), "token = " << token);
	id= buffer;
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT2(1 == sscanf(token, "name=%[^\n]", buffer), "What was written: " << buffer << ", expecting name=[string]" );
	name= buffer;
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "size=%ld", &size) );
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "userId=%ld", &userId) );
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "uploadTimestamp=%[^\n]", buffer) );
	uploadTimestamp= buffer;
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "hash=%[^\n]", buffer) );
	hash= buffer;
	REPORT_DEBUG("token= " << token << "\n");
	
	token= strtok(NULL, delimiter);
	ASSERT(1 == sscanf(token, "bucket=%[^\n]", buffer) );
	bucket= buffer;
	REPORT_DEBUG("token= " << token << "\n");
	
}

bool FileInfo::operator==(FileInfo const &other) const{
	bool ret= true;
	COMPARE(id);
	ret= ret && (id==other.id);
	COMPARE(name);
	ret= ret && (name==other.name);
	COMPARE(size);
	ret= ret && (size==other.size);
	COMPARE(userId);
	ret= ret && (userId==other.userId);
	COMPARE(uploadTimestamp);
	ret= ret && (uploadTimestamp==other.uploadTimestamp);
	COMPARE(hash);
	ret= ret && (hash==other.hash);
	COMPARE(bucket);
	ret= ret && (bucket==other.bucket);
	return ret;
}


