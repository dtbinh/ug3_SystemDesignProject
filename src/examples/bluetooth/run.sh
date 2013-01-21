#!/bin/bash

CLASS=bluetooth_nxt

echo "Compiling java..." && \
nxjc $CLASS.java && \

echo "Linking java..."  && \
nxjlink -o $CLASS.nxj $CLASS && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n G5 -d 00:16:53:07:D5:5F -r $CLASS.nxj  && \

echo "NXT OK!" && \

echo "Compiling java..." && \
nxjpcc bluetooth_pc.java && \

read -p "Press any key when brick ready..." && \
nxjpc bluetooth_pc && \

echo "Done!"
