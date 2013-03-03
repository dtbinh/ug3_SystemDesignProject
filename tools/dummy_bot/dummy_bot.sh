#!/bin/bash

base_dir=$(dirname $(readlink -f $0))
src_dir=$base_dir/src
cd $src_dir
python $(basename $0 .sh).py $@
