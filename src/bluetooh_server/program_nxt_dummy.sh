#!/bin/bash

CLASSNXT=bluetooth_nxtd

echo "Compiling java..." && \
nxjc $CLASSNXT.java && \

echo "Linking java..."  && \
nxjlink -o $CLASSNXT.nxj $CLASSNXT && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n G5Dummy -d 00:16:53:0B:B5:A3 -r $CLASSNXT.nxj  && \

echo "NXT OK!" && \

#echo "Compiling java..." && \
#nxjpcc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:."  $CLASSPC.java && \

#read -p "Press ENTER when brick ready..." && \
#echo "Starting bluetooth server..." && \
#nxjpc -cp "$LD_LIBRARY_PATH/../share/java/zmq.jar:." $CLASSPC && \

echo "Done!"
