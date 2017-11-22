#ifndef FILEINFO_H
#define FILEINFO_H

#include <string>
#include <stdint.h>
#include "Serializable.h"

class FileInfo: Serializable<FileInfo>{
	public:
		FileInfo(void):bucket(""){}
		FileInfo(std::string id,
				std::string name,
				int64_t size,
				int64_t userId,
				std::string uploadTimestamp,
				std::string hash,
				std::string bucket);
		FileInfo(std::string const &serializedData);
		std::string Serialize();
		bool operator==(FileInfo const &other) const;
		
		std::string id;
		std::string name;
		int64_t size;
		int64_t userId;
		std::string uploadTimestamp, hash, bucket;
//		unsigned char[] payload;
};

#endif // FILEINFO_H
