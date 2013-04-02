#!/bin/bash

# what to compile/upload
java_package="robot"
java_class="Robot"
bluetooth_name="#1"



# you won't need to change anything after this line

base_dir="$(dirname $(readlink -f $0))"
src_dir="$base_dir/win_src"
bin_dir="$base_dir/win_bin"
lib_dir="$base_dir/lib"
ldlib_dir="$base_dir/lib/lib"

lejos_dir="$lib_dir/leJOS_NXJ_0.9.1beta-3"
lejos_bin="$lejos_dir/bin"
lejos_jar="$lejos_dir/lib/nxt/classes.jar:$lejos_dir/lib/pc/pccomm.jar"

jmq_jar="$lib_dir/share/java/zmq.jar"

classpath="$bin_dir:$lejos_jar:$jmq_jar:$CLASSPATH"
export PATH="$lejos_bin:$PATH"
export LD_LIBRARY_PATH="$lib_dir:$ldlib_dir:$LD_LIBRAR_PATH"

java_path="$src_dir/$java_package"
mkdir -p $bin_dir
java_files=$(find $src_dir -name *.java)

echo "Compiling java..." && \
javac -d $bin_dir -cp $classpath $java_files && \
nxjc -d $bin_dir -cp $classpath $java_path/$java_class.java && \

echo "Linking java..."  && \
nxjlink -o $bin_dir/$java_package/$java_class.nxj -cp $classpath \
       $java_package.$java_class && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n $bluetooth_name \
      -r $bin_dir/$java_package/$java_class.nxj  && \

echo "Done!" && \
exit 0
