#!/usr/bin/python

"""
"""

import pygame
import pygame.camera
import sys
import time
import zmq

import pygame_helpers

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
    SLEEP_TIME = 0.05

    def __init__(self, address='ipc:///tmp/nxt_bluetooth_robot', ipc=True):
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
            print 'Server response: %s' % socket.recv()
            time.sleep(sleep_time)
        else:
            print message

    def send_kick_command(self):
        # TODO: implement
        print 'DummyBot: Kick!'

    def send_move_command(self, (x, y)):
        # TODO: implement
        print 'DummyBot: Moving to (%d, %d)' % (x, y)

    def __del__(self):
        if self.socket:
            self.socket.close()


class DummyBotGUI(object):
    def __init__(self, camera='/dev/video0', size=(640, 480), ipc=False):
        self.camera = VideoInput(camera)
        self.size = size
        self.display = pygame.display.set_mode(size, 0)
        pygame.display.set_caption(self.__class__.__name__)
        self.snapshot = pygame.surface.Surface(self.size, 0, self.display)
        self.communications = Communications(ipc=ipc)
        self.move_point = None

    def get_camera_image(self):
        self.snapshot = self.camera.get_image(self.snapshot)
        self.display.blit(self.snapshot, (0, 0))

    def draw(self):
        self.get_camera_image()
        if self.move_point is not None:
            pygame_helpers.draw_crosshair(self.display, self.move_point)
        pygame.display.flip()

    def run(self):
        done = False
        move_point = None
        while not done:
            for event in pygame.event.get():
                if pygame_helpers.is_quit_event(event):
                    done = True
                elif pygame_helpers.is_leftmouse_event(event):
                    self.move_point = pygame.mouse.get_pos()
                    self.communications.send_move_command(self.move_point)
                elif pygame_helpers.is_rightmouse_event(event):
                    self.communications.send_kick_command() 
            self.draw()


if __name__ == '__main__':
    enable_ipc = True
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            if arg == '--no-ipc':
                enable_ipc = False

    DummyBotGUI(ipc=enable_ipc).run()
