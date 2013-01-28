#!/bin/bash

base_dir=$(dirname $(readlink -f $0))
lib_dir=$base_dir/lib
bin_dir=$base_dir/bin
vision_class=JavaVision.RunVision

lib_jars=$(echo $(ls $lib_dir/*.jar) | tr "[[:space:]]" ":")
classpath=$lib_jars:$bin_dir:$CLASSPATH

cd $base_dir
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$lib_dir
java -Djava.library.path=$lib_dir -cp $classpath $vision_class
