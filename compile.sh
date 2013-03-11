#!/bin/bash

base_dir="$(dirname $(readlink -f $0))"
src_dir="$base_dir/src"
bin_dir="$base_dir/bin"
dependencies="$base_dir/.dependencies"

classpath="$CLASSPATH"
for dependency in $(cat $dependencies); do
    classpath="$dependency:$classpath"
done

mkdir -p $bin_dir
javac -d $bin_dir -cp $classpath $(find $src_dir -type f -name '*.java')
