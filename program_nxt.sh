#!/bin/bash

java_package="BluetoothServer"
java_class="bluetooth_nxt"
bluetooth_address="00:16:53:07:D5:5F"
bluetooth_name="G5"

base_dir="$(dirname $(readlink -f $0))"
src_dir="$base_dir/src"
bin_dir="$base_dir/bin"
lib_dir="$base_dir/lib"

lejos_dir="$lib_dir/leJOS_NXJ_0.9.1beta-3"
lejos_bin="$lejos_dir/bin"
lejos_jar="$lejos_dir/lib/nxt/classes.jar:$lejos_dir/lib/pc/pccomm.jar"

jmq_jar="$lib_dir/share/java/zmq.jar"

classpath="$bin_dir:$lejos_jar:$jmq_jar:$CLASSPATH"
export PATH="$lejos_bin:$PATH"
export LD_LIBRARY_PATH="$lib_dir:$LD_LIBRAR_PATH"

case $1 in
    --dummy-bot)
        java_class="bluetooth_nxtd"
        bluetooth_address="00:16:53:0B:B5:A3"
        bluetooth_name="G5Dummy"
        ;;
esac

echo "Compiling java..." && \
nxjc -d $bin_dir -cp $classpath $src_dir/$java_package/*.java && \

echo "Linking java..."  && \
nxjlink -o $bin_dir/$java_package/$java_class.nxj -cp $classpath \
           $java_package.$java_class && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n $bluetooth_name -d $bluetooth_address \
          -r $java_package/$java_class.nxj  && \

echo "NXT OK!" && \
echo "Done!"
