#!/bin/bash

CLASSPC=BluetoothPC

echo "Compiling java..." && \
nxjpcc -cp "../../lib/share/java/zmq.jar:."  $CLASSPC.java && \

echo "Starting bluetooth server..." && \
nxjpc -cp "../../lib/share/java/zmq.jar:." $CLASSPC && \

echo "Done!"
