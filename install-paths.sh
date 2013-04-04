#!/bin/bash

SDP_HOME=`pwd`
LIB="$SDP_HOME/lib"
LD_LIBRARY_DIR="$LIB/lib"

function add_to_bashrc {
    grep -Fxq "$*" ~/.bashrc || echo "$*" >> ~/.bashrc
}

add_to_bashrc "export LD_LIBRARY_PATH=\$LD_LIBRARY_PATH:$LD_LIBRARY_DIR" &&\
add_to_bashrc "export PATH=\$PATH:$LIB/leJOS_NXJ_0.9.1beta-3/bin" &&\
add_to_bashrc "export PYTHONPATH=\$PYTHONPATH:$LIB/lib64/python2.6/site-packages/" &&\
add_to_bashrc "export PYTHONPATH=\$PYTHONPATH:$LIB/lib/python2.6/site-packages/" &&\
add_to_bashrc "export PYTHONPATH=\$PYTHONPATH:$LIB/lib/" &&\
echo "Done, do 'source ~/.bashrc' now"
