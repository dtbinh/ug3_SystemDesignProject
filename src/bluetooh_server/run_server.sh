#!/bin/bash

CLASSPC=bluetooth_pc

echo "Compiling java..." && \
nxjpcc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:."  $CLASSPC.java && \

echo "Starting bluetooth server..." && \
nxjpc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:." $CLASSPC && \

echo "Done!"
