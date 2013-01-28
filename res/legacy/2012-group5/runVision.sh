#!/bin/bash

lib_dir=lib
bin_dir=bin
vision_class=JavaVision.RunVision

lib_jars=$(echo $(ls $lib_dir/*.jar) | tr "[[:space:]]" ":")
classpath=$lib_jars:$bin_dir

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$lib_dir
java -Djava.library.path=$lib_dir -cp $classpath $vision_class
