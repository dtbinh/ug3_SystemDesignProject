#!/bin/bash

CLASSPC="bluetooth_pc"
ISDUMMY=""

case $1 in
    --dummy-bot)
        ISDUMMY="dummy"
        ;;
esac

echo "Compiling java..." && \
nxjpcc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:."  $CLASSPC.java && \

echo "Starting bluetooth server..." && \
nxjpc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:." $CLASSPC $ISDUMMY && \

echo "Done!"
