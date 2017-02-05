#!/bin/bash

echo $0 $*
#sleep 30

tmp=`echo $$`

#bowtie-build $1 index-$tmp
#bowtie -f -p 8 --sam-nohead -k 2 index-$tmp $2 $3
#rm -rf index-$tmp.*
# -f -p 8 --sam-nohead -k 2 "/home/zoonimbus/zoonimbusProject/pipeline/chr1Index" $1 $2
echo $(pwd)/workflow


