#!/bin/bash

CLASSNXT="robot/Robot"

java_src=$(find . -name *.java)

echo "Compiling java..." && \
javac -cp $classpath $java_src && \
nxjc -cp $classpath $CLASSNXT.java && \

echo "Linking java..."  && \
nxjlink -o $CLASSNXT.nxj $CLASSNXT && \

echo "Uploading to the brick over bluetooth..."  && \
nxjupload -b -n "#1" -r $CLASSNXT.nxj  && \

echo "Done!"
