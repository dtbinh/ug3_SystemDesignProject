#!/bin/bash

CLASSNXT=BluetoothNXT

echo "Compiling java..." && \
nxjc $CLASSNXT.java && \

echo "Linking java..."  && \
nxjlink -o $CLASSNXT.nxj $CLASSNXT && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n "#1" -d 00:16:53:0B:B5:A3 -r $CLASSNXT.nxj  && \

echo "Done!"