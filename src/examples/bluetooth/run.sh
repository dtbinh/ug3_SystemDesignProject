#!/bin/bash

CLASS=bluetooth_nxt

#echo "Compiling java..." && \
#nxjc $CLASS.java && \

#echo "Linking java..."  && \
#nxjlink -o $CLASS.nxj $CLASS && \

#echo "Uploading to the brick over bluetooth..."  && \
#nxjupload -b -n G5 -d 00:16:53:07:D5:5F -r $CLASS.nxj  && \

#echo "NXT OK!" && \

echo "Compiling java..." && \
nxjpcc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:."  bluetooth_pc.java && \

read -p "Press ENTER when brick ready..." && \
echo "Starting bluetooth server..." && \
nxjpc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:." bluetooth_pc && \

echo "Done!"
