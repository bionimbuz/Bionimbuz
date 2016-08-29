#!/bin/bash

id=`qsub $1 | awk '{print $3}'`;

echo "Job com id $id";

output=`qstat -s a | grep $id`;
while [ ! -z "$output" ]
do
	sleep 1;
	echo esperando...;
	output=`qstat -s a | grep $id`;
done

cat $1.o$id;
