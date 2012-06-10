#! /bin/bash

echo "Test script started (don't care about errors)"
echo "---------------------------------------------"
rm a b c d
touch a b c d #
sleep 1
for i in `seq 1 100`; do
k=$i
java -jar mapreduceFix.jar charcount $k a
done
rm Result/*
sleep 1
for i in `seq 11 75`; do
k=$((i*10))
java -jar mapreduceFix.jar charcount $k b
done
rm Result/*
sleep 1
for i in `seq 1 10`; do
k=$((i*1000))
java -jar mapreduceFix.jar charcount $k c
done
java -jar mapreduceFix.jar charcount 250000 d
rm Result/*
cat a b c d > char
echo "---------------------------------------------"
rm a b c d
touch a b c d #
sleep 1
for i in `seq 1 100`; do
k=$i
java -jar mapreduceFix.jar wordcount $k a
done
rm Result/*
sleep 1
for i in `seq 11 75`; do
k=$((i*10))
java -jar mapreduceFix.jar wordcount $k b
done
rm Result/*
sleep 1
for i in `seq 1 10`; do
k=$((i*1000))
java -jar mapreduceFix.jar wordcount $k c
done
java -jar mapreduceFix.jar wordcount 250000 d
rm Result/*
cat a b c d > word
rm a b c d
echo "---------------------------------------------"
echo "script finished"
