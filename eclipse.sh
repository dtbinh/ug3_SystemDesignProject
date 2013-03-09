#!/bin/bash

base_dir="$(dirname $(readlink -f $0))"
ldlib_dir="$base_dir/lib/lib"

export LD_LIBRARY_PATH="$ldlib_dir:$LD_LIBRARY_PATH"

exec eclipse &
