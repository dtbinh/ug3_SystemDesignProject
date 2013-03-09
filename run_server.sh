#!/bin/bash

java_package="BluetoothServer"
java_class="bluetooth_pc"

base_dir="$(dirname $(readlink -f $0))"
src_dir="$base_dir/src"
bin_dir="$base_dir/bin"
lib_dir="$base_dir/lib"

jmq_dir="$lib_dir/share/java/zmq.jar"
lejos_dir="$lib_dir/leJOS_NXJ_0.9.1beta-3/bin"

classpath="$jmq_dir:$bin_dir:$CLASSPATH"
export PATH="$lejos_dir:$PATH"
export LD_LIBRARY_PATH="$lib_dir:$LD_LIBRAR_PATH"

is_dummy=""
case $1 in
    --dummy-bot)
        is_dummy="dummy"
        ;;
esac

echo "Compiling java..." && \
nxjpcc -d $bin_dir -cp $classpath "$src_dir/$java_package/$java_class.java" && \

echo "Starting bluetooth server..." && \
nxjpc -cp $classpath $java_package.$java_class $is_dummy && \

echo "Done!"
