#!/bin/bash

base_dir="$(dirname $(readlink -f $0))"
src_dir="$base_dir/src"
bin_dir="$base_dir/bin"
lib_dir="$base_dir/lib"
java_package="BluetoothServer"
java_path="$src_dir/$java_package"

lejos_dir="$lib_dir/leJOS_NXJ_0.9.1beta-3"
lejos_bin="$lejos_dir/bin"
lejos_jar="$lejos_dir/lib/nxt/classes.jar:$lejos_dir/lib/pc/pccomm.jar"

jmq_jar="$lib_dir/share/java/zmq.jar"

classpath="$bin_dir:$lejos_jar:$jmq_jar:$CLASSPATH"
export PATH="$lejos_bin:$PATH"
export LD_LIBRARY_PATH="$lib_dir:$lib_dir/lib:$LD_LIBRAR_PATH"

java_class=
bluetooth_address=
bluetooth_name=
program_nxt=
use_dummy=
while [ "$1" != "" ]; do
    case $1 in
        --dummy-robot)
            use_dummy="dummy"
            bluetooth_address="00:16:53:0B:B5:A3"
            bluetooth_name="G5Dummy"
            ;;
        --program-nxt)
            program_nxt=1
            bluetooth_address="00:16:53:07:D5:5F"
            bluetooth_name="G5"
            ;;
    esac
    shift
done

if [ -z $program_nxt ]; then
    java_class="BluetoothPC"

    echo "Compiling java..." && \
    nxjpcc -d $bin_dir -cp $classpath $java_path/$java_class.java && \

    echo "Starting bluetooth server..." && \
    nxjpc -cp $classpath $java_package.$java_class $use_dummy && \

    echo "Done!"
else
    java_class="BluetoothNXT"
    if [ -z $use_dummy ]; then
        java_class="BluetoothNXTD"
    fi

    echo "Compiling java..." && \
    nxjc -d $bin_dir -cp $classpath $java_path/*.java && \

    echo "Linking java..."  && \
    nxjlink -o $bin_dir/$java_package/$java_class.nxj -cp $classpath \
               $java_package.$java_class && \

    echo "Uploading to the brick over bluetooth..."  && \
    nxjupload -b -n $bluetooth_name -d $bluetooth_address \
              -r $java_package/$java_class.nxj  && \

    echo "Done!"
fi
