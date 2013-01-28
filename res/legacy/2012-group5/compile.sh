#!/bin/bash

bin_dir=bin
lib_dir=lib

sources=sources.txt
find -name "*.java" > $sources
mkdir -p $bin_dir
javac -cp $lib_dir/*:. -d $bin_dir @$sources
rm $sources
