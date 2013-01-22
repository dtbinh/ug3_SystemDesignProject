#!/bin/bash

base_dir=$(dirname $(readlink -f $0))
lib_dir=$base_dir/lib
src_dir=$base_dir/src

# install pygame locally if necessary
PYTHONPATH=$lib_dir:$PYTHONPATH
export PYTHONPATH
python -c "import pygame" &> /dev/null
if [ $? -gt 0 ]; then
    mkdir -p $lib_dir
    easy_install --install-dir $lib_dir pygame
fi

# run script
cd $src_dir
python $(basename $0 .sh).py $@
