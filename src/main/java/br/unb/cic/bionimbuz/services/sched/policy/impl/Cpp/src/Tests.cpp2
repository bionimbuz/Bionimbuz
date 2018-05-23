#include <stdio.h>
#include "Job.h"
#include "PluginInfo.h"

#define ANSI_COLOR_RED     "\x1b[31m"
#define ANSI_COLOR_GREEN   "\x1b[32m"
#define ANSI_COLOR_YELLOW  "\x1b[33m"
#define ANSI_COLOR_BLUE    "\x1b[34m"
#define ANSI_COLOR_MAGENTA "\x1b[35m"
#define ANSI_COLOR_CYAN    "\x1b[36m"
#define ANSI_COLOR_RESET   "\x1b[0m"

void PrintResult(bool result){
	if(result){
		printf(ANSI_COLOR_GREEN "SUCESS!\n" ANSI_COLOR_RESET);
	}
	else{
		printf(ANSI_COLOR_RED "FAIL!\n" ANSI_COLOR_RESET);
	}
}

int main(void){
	printf("GPU Aware Scheduler Project Test\n\n");

	printf("Job Serialization Test:\t\t");
	PrintResult(Job::TestSerialization());
	
	printf("PluginInfo Serialization Test:\t\t");
	PrintResult(PluginInfo::TestSerialization());
	
	return 0;
}

