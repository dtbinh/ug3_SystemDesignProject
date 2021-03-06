#!/usr/bin/python

"""
"""

import pygame
import pygame.camera
import sys
import time
import zmq

FORWARD_SPEED = 1000
BACKWARD_SPEED = 1000
TURN_SPEED = 1000

pygame.init()
pygame.camera.init()

class VideoInput(object):
    def __init__(self, device):
        self.camera = pygame.camera.Camera(device)
        self.camera.start()

    def __del__(self):
        self.camera.stop()

    def get_image(self, snapshot):
        if self.camera.query_image():
            snapshot = self.camera.get_image(snapshot)
        return snapshot


class Communications(object):
    SLEEP_TIME = 0.1    

    def __init__(self, address='tcp://127.0.0.1:5555', ipc=True):
        if ipc:
            self.context = zmq.Context()
            print 'Connecting to server...'
            self.socket = self.context.socket(zmq.REQ)
            self.socket.connect(address)
        else:
            self.context = None
            self.socket = None

    def _send_message(self, message, sleep_time=SLEEP_TIME):
        if self.socket:
            self.socket.send(message)
            print 'Server response: %s' % self.socket.recv()
            time.sleep(sleep_time)
        else:
            print message

    def send_kick_command(self):
        # TODO: implement
        if self.socket:
            #self._send_message(message, sleep_time)
            pass        
        
        print 'DummyBot: Kick!'

    def send_anticlockwise_command(self):
        # TODO: implement
        if self.socket:
            self._send_message("1 %d %d %d %d" % (TURN_SPEED, -TURN_SPEED, 0, 0))

        print 'DummyBot: Rotating anti clockwise'

    def send_clockwise_command(self):
        # TODO: implement
        if self.socket:
            self._send_message("1 %d %d %d %d" % (-TURN_SPEED, TURN_SPEED, 0, 0))
        
        print 'DummyBot: Rotating clockwise'

    def send_forwards_command(self):
        # TODO: implement
        if self.socket:
            self._send_message("1 %d %d %d %d" % (FORWARD_SPEED, FORWARD_SPEED, 0, 0))
        
        print 'DummyBot: Moving forwards'

    def send_backwards_command(self):
        # TODO: implement
        if self.socket:
            self._send_message("1 %d %d %d %d" % (-BACKWARD_SPEED, -BACKWARD_SPEED, 0, 0))
            
        print 'DummyBot: Moving backwards'

    def send_stop_command(self):
        # TODO: implement
        if self.socket:
            self._send_message("1 %d %d %d %d" % (0, 0, 0, 0))
        
        print 'DummyBot: Stopping'

    def __del__(self):
        if self.socket:
            self.socket.close()


class DummyBotGUI(object):
    FPS = 60
    STOP_KEYS = [pygame.K_ESCAPE]
    KICK_KEYS = [pygame.K_RETURN, pygame.K_SPACE]
    FORWARDS_KEYS = [pygame.K_w, pygame.K_UP]
    BACKWARDS_KEYS = [pygame.K_s, pygame.K_DOWN]
    ANTICLOCKWISE_KEYS = [pygame.K_a, pygame.K_LEFT]
    CLOCKWISE_KEYS = [pygame.K_d, pygame.K_RIGHT]

    def __init__(self, camera='/dev/video0', size=(640, 480), ipc=False):
        self.clock = pygame.time.Clock()
        self.camera = VideoInput(camera)
        self.size = size
        self.display = pygame.display.set_mode(size, 0)
        pygame.display.set_caption(self.__class__.__name__)
        self.snapshot = pygame.surface.Surface(self.size, 0, self.display)
        self.communications = Communications(ipc=ipc)
        self.robot_is_stopped = True

    def get_camera_image(self):
        self.snapshot = self.camera.get_image(self.snapshot)
        self.display.blit(self.snapshot, (0, 0))

    def draw(self):
        self.get_camera_image()
        pygame.display.flip()

    def send_commands(self, stop_keys=STOP_KEYS, kick_keys=KICK_KEYS,
                      forwards_keys=FORWARDS_KEYS,
                      backwards_keys=BACKWARDS_KEYS,
                      clockwise_keys=CLOCKWISE_KEYS,
                      anticlockwise_keys=ANTICLOCKWISE_KEYS):
        pressed = pygame.key.get_pressed()
        if any([pressed[key] for key in stop_keys]):
            self.communications.send_stop_command()
            self.robot_is_stopped = True
        elif any([pressed[key] for key in kick_keys]):
            self.communications.send_kick_command()
        elif any([pressed[key] for key in forwards_keys]):
            self.communications.send_forwards_command()
            self.robot_is_stopped = False
        elif any([pressed[key] for key in backwards_keys]):
            self.communications.send_backwards_command()
            self.robot_is_stopped = False
        elif any([pressed[key] for key in clockwise_keys]):
            self.communications.send_clockwise_command()
            self.robot_is_stopped = False
        elif any([pressed[key] for key in anticlockwise_keys]):
            self.communications.send_anticlockwise_command()
            self.robot_is_stopped = False
        else:
            if not self.robot_is_stopped:
                self.communications.send_stop_command()
                self.robot_is_stopped = True

    def run(self):
        done = False
        move_point = None
        while not done:
            for event in pygame.event.get():
                if is_quit_event(event):
                    done = True
            self.send_commands()
            self.draw()
            self.clock.tick(DummyBotGUI.FPS)

def is_quit_event(event):
    if event.type == pygame.QUIT:
        return True
    elif event.type == pygame.KEYDOWN:
        if event.key == pygame.K_q:
            kmods = pygame.key.get_mods()
            rctrl_down = kmods & pygame.KMOD_RCTRL
            lctrl_down = kmods & pygame.KMOD_LCTRL
            if lctrl_down or rctrl_down:
                return True
    return False

if __name__ == '__main__':
    enable_ipc = True
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            if arg == '--no-ipc':
                enable_ipc = False

    DummyBotGUI(ipc=enable_ipc).run()
