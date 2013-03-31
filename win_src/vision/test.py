import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
socket = context.socket(zmq.REQ)
socket.connect ("tcp://127.0.0.1:6666")

# Kick!
while True:
    socket.send ("P")
    print socket.recv()
    
    socket.send ("E")
    print socket.recv()

#time.sleep(5)
