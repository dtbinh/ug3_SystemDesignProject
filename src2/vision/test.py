import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
vision_socket = context.socket(zmq.REQ)
vision_socket.connect ("tcp://127.0.0.1:6666")

bt_socket = context.socket(zmq.REQ)
bt_socket.connect("tcp://127.0.0.1:5555")

# Kick!
while True:
    vision_socket.send ("E")
    data = vision_socket.recv()

    # gate angle
    bt_socket.send("10  " + data + " 180")
    print bt_socket.recv()

    time.sleep(5)

#time.sleep(5)
