#!/bin/bash

base_dir=$(dirname $(readlink -f $0))
bin_dir=$base_dir/bin
lib_dir=$base_dir/lib

sources=$base_dir/sources.tmp
cd $base_dir
find -name "*.java" > $sources
mkdir -p $bin_dir
javac -cp $lib_dir/*:. -d $bin_dir @$sources
rm $sources
