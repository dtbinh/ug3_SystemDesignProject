#!/bin/bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib
java -Djava.library.path=./lib -cp ./lib/bcel.jar:./lib/bluecove-gpl.jar:./lib/bluecove.jar:./lib/classes.jar:./lib/commons-cli.jar:./lib/jtools.jar:./lib/pccomm.jar:./lib/pctools.jar:./lib/v4l4j.jar:./bin JavaVision.RunVision
