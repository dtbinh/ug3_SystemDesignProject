#!/bin/bash

CLASS0=M1_kick
CLASS1=M1_drive

echo "Compiling java..." && \
nxjc $CLASS0.java && \
nxjc $CLASS1.java && \

echo "Linking java..."  && \
nxjlink -o $CLASS0.nxj $CLASS0 && \
nxjlink -o $CLASS1.nxj $CLASS1 && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n G5 -d 00:16:53:07:D5:5F $CLASS0.nxj  && \
nxjupload -b -n G5 -d 00:16:53:07:D5:5F $CLASS1.nxj  && \


echo "NXT OK!" && \

echo "Done!"
