import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
socket = context.socket(zmq.REQ)
socket.connect("tcp://127.0.0.1:5555")
print "Connected!"

# Kick!
#socket.send ("1 0 0 0 0")

while 1:
    #socket.send ("1 1 2 3 4")
    socket.send("3")

    print socket.recv()
    
    
    time.sleep(5)