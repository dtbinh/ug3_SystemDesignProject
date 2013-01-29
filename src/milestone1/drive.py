import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
socket = context.socket(zmq.REQ)
socket.connect ("ipc:///tmp/nxt_bluetooth_robot")

# Change acceleration [motorA motorB], default [2500 2500]
socket.send("6 2500 2500")
socket.recv()
time.sleep(0.01) # Not sure if needed, just in case at the moment


# Change speed [motorA motorB], default [6000 6000]
socket.send("5 6000 6000")
socket.recv()
time.sleep(0.01) # Not sure if needed, just in case at the moment

# Drive distance in "ticks" [motorA motorB], default [2500 -2500]
socket.send("4 2500 -2500")
print socket.recv()

