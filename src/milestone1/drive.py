import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
socket = context.socket(zmq.REQ)
socket.connect ("ipc:///tmp/nxt_bluetooth_robot")


socket.send("4 2500 -2500")
print socket.recv()

