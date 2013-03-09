#!/bin/bash

base_dir=$(dirname $(readlink -f $0))
src_dir=$base_dir/src
lib_dir=$(readlink -f "$base_dir/../../lib/lib64/python2.6/site-packages")
python -c "import pygame; import zmq" &> /dev/null
if [ $? -gt 0 ]; then
    export PYTHONPATH="$lib_dir:$PYTHONPATH"
fi
cd $src_dir
python $(basename $0 .sh).py $@
