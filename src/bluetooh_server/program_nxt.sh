#!/bin/bash

# default to main robot
NXTCLASS="bluetooth_nxt"
BTADDRESS="00:16:53:07:D5:5F"
BTNAME="G5"

# switch to bummy robot if $1 is set
case $1 in
    --dummy-bot)
        NXTCLASS="bluetooth_nxtd"
        BTADDRESS="00:16:53:0B:B5:A3"
        BTNAME="G5Dummy"
        ;;
esac

echo "Compiling java..." && \
nxjc $NXTCLASS.java && \

echo "Linking java..."  && \
nxjlink -o $NXTCLASS.nxj $NXTCLASS && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n $BTNAME -d $BTADDRESS -r $NXTCLASS.nxj  && \

echo "NXT OK!" && \
echo "Done!"
