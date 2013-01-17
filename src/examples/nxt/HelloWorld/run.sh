#!/bin/bash

CLASS=HelloWorld

nxjc $CLASS.java && \
nxjlink -o $CLASS.nxj $CLASS && \
nxjupload -r $CLASS.nxj