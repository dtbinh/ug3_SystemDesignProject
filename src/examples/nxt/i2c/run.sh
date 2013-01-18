#!/bin/bash

CLASS=i2c

nxjc $CLASS.java && \
nxjlink -o $CLASS.nxj $CLASS && \
nxjupload -r $CLASS.nxj