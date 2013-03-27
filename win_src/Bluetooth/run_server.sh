#!/bin/bash

CLASSPC=BluetoothPC

echo "Compiling java..." && \
nxjpcc -cp "/home/andrew/workspace/UoE/SDP2013/ug3_SDP/lib/share/java/zmq.jar:."  $CLASSPC.java && \

echo "Starting bluetooth server..." && \
nxjpc -cp "/home/andrew/workspace/UoE/SDP2013/ug3_SDP/lib/share/java/zmq.jar:." $CLASSPC && \

echo "Done!"