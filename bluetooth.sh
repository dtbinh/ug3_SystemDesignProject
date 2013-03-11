#!/bin/bash

function print_usage() {
    echo "usage: $0 [options] server|program-nxt"
}

function print_help() {
    print_usage
    echo ""
    echo "modes:"
    echo "  server       - start a bluetooth server to interface with AI/robot"
    echo "  program-nxt  - upload new firmware to robot"
    echo ""
    echo "options:"
    echo "  --dummy-robot - use the remote-controlled robot instead of HSTYA"
}

script_name="$(basename $0)"
base_dir="$(dirname $(readlink -f $0))"
src_dir="$base_dir/src"
bin_dir="$base_dir/bin"
lib_dir="$base_dir/lib"
ldlib_dir="$base_dir/lib/lib"
java_package="BluetoothServer"
java_path="$src_dir/$java_package"

lejos_dir="$lib_dir/leJOS_NXJ_0.9.1beta-3"
lejos_bin="$lejos_dir/bin"
lejos_jar="$lejos_dir/lib/nxt/classes.jar:$lejos_dir/lib/pc/pccomm.jar"

jmq_jar="$lib_dir/share/java/zmq.jar"

classpath="$bin_dir:$lejos_jar:$jmq_jar:$CLASSPATH"
export PATH="$lejos_bin:$PATH"
export LD_LIBRARY_PATH="$lib_dir:$ldlib_dir:$LD_LIBRAR_PATH"

java_class=
bluetooth_address="00:16:53:07:D5:5F"
bluetooth_name="G5"
program_nxt=
run_server=
use_dummy=
while [ "$1" != "" ]; do
    case $1 in
        --dummy-robot)
            use_dummy="dummy"
            bluetooth_address="00:16:53:0B:B5:A3"
            bluetooth_name="G5Dummy"
            ;;
        program-nxt)
            program_nxt=1
            ;;
        server)
            run_server=1
            ;;
        -h|--help)
            print_help
            ;;
        -*)
            echo "unrecognized option \"$1\""
            print_usage
            exit 1
            ;;
        *)
            echo "unrecognized mode \"$1\""
            print_usage
            exit 1
            ;;
    esac
    shift
done

mkdir -p $bin_dir
if [ "42$run_server" != "42" ]; then
    java_class="BluetoothPC"
    echo "$script_name> Mode = Run Server"
    if [ "42$use_dummy" != "42" ]; then
        echo "$script_name> Target = Dummy Robot"
    fi
    echo "Compiling java..." && \
    javac -d $bin_dir -cp $classpath $java_path/*.java && \
    nxjpcc -d $bin_dir -cp $classpath $java_path/$java_class.java && \
    echo "Starting bluetooth server..." && \
    nxjpc -cp $classpath $java_package.$java_class $use_dummy && \
    echo "Done!" && \
    exit 0

elif [ "42$program_nxt" != "42" ]; then
    java_class="BluetoothNXT"
    echo "$script_name> Mode = Program NXT"
    if [ "42$use_dummy" != "42" ]; then
        echo "$script_name> Target = Dummy Robot"
        java_class="BluetoothNXTD"
    fi
    echo "Compiling java..." && \
    javac -d $bin_dir -cp $classpath $java_path/*.java && \
    nxjc -d $bin_dir -cp $classpath $java_path/$java_class.java && \
    echo "Linking java..."  && \
    nxjlink -o $bin_dir/$java_package/$java_class.nxj -cp $classpath \
               $java_package.$java_class && \
    echo "Uploading to the brick over bluetooth..."  && \
    nxjupload -b -n $bluetooth_name -d $bluetooth_address \
              -r $bin_dir/$java_package/$java_class.nxj  && \
    echo "Done!" && \
    exit 0

else
    echo "please specify a mode"
    print_usage
    exit 1
fi
