import sys
import time

import zmq

context = zmq.Context()

print "Connecting to server..."
socket = context.socket(zmq.REQ)
socket.connect ("ipc:///tmp/nxt_bluetooth_robot")


socket.send ("1 -255 255 -255 255")
print socket.recv()
time.sleep(3)

socket.send ("1 255 -255 255 -255")
print socket.recv()
time.sleep(3)


socket.send ("1 0 0 0 0")
print socket.recv()


#  Do 10 requests, waiting each time for a response
#for request in range (1,10):
#    print "Sending request ", request,"..."
#    socket.send ("1 255 255 255 255")
#    #  Get the reply.
#    message = socket.recv()
#    print "Received reply ", request, "[" + str(message) + "]"