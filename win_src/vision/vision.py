from __future__ import print_function
import sys
import os
import time
import zmq
import math
import threading
from cv2 import cv

from optparse import OptionParser

from SimpleCV import Image, Camera, VirtualCamera
from preprocess import Preprocessor
from postprocess import Postprocessor
from features import Features
from threshold import Threshold
from display import Gui, ThresholdGui

HOST = 'localhost' 
PORT = 28546 

#PITCH_SIZE = (243.8, 121.9)

# Distinct between field size line or entity line
ENTITY_BIT = 'E';
PITCH_SIZE_BIT  = 'P';

class Vision:
    
    def __init__(self, pitchnum, stdout, sourcefile, resetPitchSize):
               
        self.running = True
        #self.connected = False
        
        self.stdout = stdout 

        if sourcefile is None:  
            self.cap = Camera()
        else:
            filetype = 'video'
            if sourcefile.endswith(('jpg', 'png')):
                filetype = 'image'

            self.cap = VirtualCamera(sourcefile, filetype)
        
        calibrationPath = os.path.join('calibration', 'pitch{0}'.format(pitchnum))
        self.cap.loadCalibration(os.path.join(sys.path[0], calibrationPath))

        self.gui = Gui()
        self.threshold = Threshold(pitchnum)
        self.thresholdGui = ThresholdGui(self.threshold, self.gui)
        self.preprocessor = Preprocessor(resetPitchSize)
        self.features = Features(self.gui, self.threshold)
        self.postprocessor = Postprocessor()
        
        self.current_ents = {}
        self.predicted_ents = {}
        
        eventHandler = self.gui.getEventHandler()
        eventHandler.addListener('q', self.quit)

    def run(self):
        while self.running:
            #try:
                #if not self.stdout:
                #    self.connect()
                #else:
                #    self.connected = True

             if self.preprocessor.hasPitchSize:
                 self.outputPitchSize()
                 self.gui.setShowMouse(False)
             else:
                 eventHandler.setClickListener(self.setNextPitchCorner)

             while self.running:
                 self.doStuff()

            #except socket.error:
             #   self.connected = False
                # If the rest of the system is not up yet/gets quit,
                # just wait for it to come available.
                #time.sleep(1)

                # Strange things seem to happen to X sometimes if the
                # display isn't updated for a while
              #  self.doStuff()

        #if not self.stdout:
            #self.socket.close()


    def quit(self):
        self.running = False
        
    def doStuff(self):
        if self.cap.getCameraMatrix is None:
            frame = self.cap.getImage()
        else:
            frame = self.cap.getImageUndistort()

        frame = self.preprocessor.preprocess(frame)
        
        self.gui.updateLayer('raw', frame)

        ents = self.features.extractFeatures(frame)
        self.updateEnts(ents)
        self.printState()
        #self.postprocessor.update(ents)

        self.gui.loop()

    def setNextPitchCorner(self, where):
        self.preprocessor.setNextPitchCorner(where)
        
        if self.preprocessor.hasPitchSize:
            print("Pitch size: {0!r}".format(self.preprocessor.pitch_size))
            self.outputPitchSize()
            self.gui.setShowMouse(False)
            self.gui.updateLayer('corner', None)
        else:
            self.gui.drawCrosshair(where, 'corner')
    
    def outputPitchSize(self):
        print(self.preprocessor.pitch_size)
        #sys.stdout.write('{0} {1} {2} \n'.format(PITCH_SIZE_BIT, self.preprocessor.pitch_size[0], self.preprocessor.pitch_size[1]))

    def updateEnts(self, ents):
        if not self.preprocessor.hasPitchSize:
            return

        self.last_update_time = int(time.time() * 1000)
        self.current_ents = ents
        self.predicted_ents = self.postprocessor.predict(ents, self.last_update_time)

    def printState(self):
        print(self.getStateString())
        
    def getStateString(self):
        reply = ""
        
        for ents in [self.current_ents, self.predicted_ents]:
            for name in ['yellow', 'blue', 'ball']:
                entity = ents[name]
                x, y = entity.coordinates()
                
                if (name == 'ball'):
                    reply += '{0} {1} '.format(int(x), int(y))
                else:
                    angle = 360 - (((entity.angle() * (180/math.pi)) - 360) % 360)
                    reply += '{0} {1} {2} '.format(int(x), int(y), int(angle))
                                     
        reply += str(self.last_update_time)
        return reply

    def handle_request(self, request):
        reply = ""
        
        if (request == 'E'):
            reply = self.getStateString()        
        elif (request == 'P'):
            for c in self.preprocessor._cropRect:
                reply += str(c) + ' '
        else:
            reply = "UNDEF"
        
        return reply


class OptParser(OptionParser):
    """
    The default OptionParser exits with exit code 2
    if OptionParser.error() is called. Unfortunately this
    screws up our vision restart script which tries to indefinitely
    restart the vision system with bad options. This just exits with
    0 instead so everything works.
    """
    def error(self, msg):
        self.print_usage(sys.stderr)
        self.exit(0, "%s: error: %s\n" % (self.get_prog_name(), msg))


if __name__ == "__main__":

    parser = OptParser()
    parser.add_option('-p', '--pitch', dest='pitch', type='int', metavar='PITCH',
                      help='PITCH should be 0 for main pitch, 1 for the other pitch')

    parser.add_option('-f', '--file', dest='file', metavar='FILE',
                      help='Use FILE as input instead of capturing from Camera')

    parser.add_option('-s', '--stdout', action='store_true', dest='stdout', default=False,
                      help='Send output to stdout instead of using a socket')

    parser.add_option('-r', '--reset', action='store_true', dest='resetPitchSize', default=False,
                      help='Don\'t restore the last run\'s saved pitch size')

    (options, args) = parser.parse_args()

    if options.pitch not in [0,1]:
        parser.error('Pitch must be 0 or 1')

    vision = Vision(options.pitch, options.stdout, options.file, options.resetPitchSize)
    
    # Socket
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    socket.bind("tcp://127.0.0.1:6666")
    socket.setsockopt(zmq.RCVTIMEO, 1000)
    
    # Run the vision thread
    thread = threading.Thread(target=vision.run)
    thread.start()

    # Wait for request
    while vision.running:
        request = None
        
        try:
            request = socket.recv()
        except:
            pass
        
        if (request != None):
            reply = vision.handle_request(request)
            socket.send(reply)
        
        #socket.send( vision.handle_request(socket.recv()) )
        
        
    thread.join(2)


