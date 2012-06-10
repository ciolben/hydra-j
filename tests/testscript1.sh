#! /bin/bash

for i in `seq 1 100`; do
#k=$((i*10))
k=$i
java -jar mapreduceFix.jar wordcount $k a
done
