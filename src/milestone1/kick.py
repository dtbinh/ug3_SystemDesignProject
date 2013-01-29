import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
socket = context.socket(zmq.REQ)
socket.connect ("ipc:///tmp/nxt_bluetooth_robot")

# Kick!
socket.send ("3")
print socket.recv()
