#!/usr/bin/python

"""
App to visualise/control holonomic robot (movement/rotation).
Explanation of GUI:
    The white square in the centre of the screen represents the robot:
        Direction of travel: towards the 'teeth' of the robot
        Direction of rotation: away from the 'eyelashes' of the robot
        The black circle around the robot indicates the maximum speed at which
        the robot can move/rotate
    Blue line: origin (move up/down with keys 'a'/'d')
    Red line: robot rotation-direction (follows mouse on left button down)
    Orange line: robot movement-direction (change with right-click)
Pretty printed to stdout:
    Angle between rotation-/movement-direction
    Angle between origin/movement-direction
    Rotation speed
    Movement speed
For easier communication with other scripts, these values are also printed to
stderr without formatting.
Angles are measured in counter-clockwise direction (in [0:360))
Rotation/Movement speed is the rotation/movement vector norm (in [0:255])
"""


import pygame
import sys
import time

from math import cos, sin, radians

import zmq

from geometry import Circle, Line, Point
from pygame_helpers import rotate_center


# constants
MAX_FPS = 50
BG_COLOR = (0, 255, 0)
WIDTH = 800
HEIGHT = 600
CENTRE = Point(WIDTH / 2, HEIGHT / 2)
ROBOT_POS = CENTRE.copy()
ROBOT_RADIUS = 40
ROBOT_COLOR = (0, 0, 0)
ROBOT_WIDTH = 0
LIMIT_POS = ROBOT_POS.copy()
LIMIT_RADIUS = 255
LIMIT_COLOR = (0, 0, 0)
LIMIT_WIDTH = 3
ROTATION_VECTOR_COLOR = (255, 0, 0)
ROTATION_VECTOR_WIDTH = 2
COORDS_COLOR = (0, 0, 255)
COORDS_WIDTH = 1
DIRECTION_VECTOR_COLOR = (255, 127, 0)
DIRECTION_VECTOR_WIDTH = 2

def run_app():
    # set up pygame
    pygame.init()
    screen = pygame.display.set_mode((WIDTH, HEIGHT))
    clock = pygame.time.Clock()
    pygame.display.set_caption('Motion Visualiser')
    robot_base = pygame.image.load('../res/base.bmp')
    robot_top = pygame.image.load('../res/top.bmp')
    # initial positions of movement-, rotation-vector, and origin
    origin_pos = Point(0, HEIGHT / 2)
    rotation_vector_pos = ROBOT_POS.copy()
    movement_vector_pos = ROBOT_POS.copy()
    robot = Circle(ROBOT_POS, ROBOT_RADIUS, (screen, ROBOT_COLOR, ROBOT_WIDTH))
    limit = Circle(LIMIT_POS, LIMIT_RADIUS, (screen, LIMIT_COLOR, LIMIT_WIDTH))
    # main loop
    done = False
    left_mouse_pressed = False
    stop_rotation = False
    stop_movement = False
    
    
    context = zmq.Context()

    print "Connecting to server..."
    socket = context.socket(zmq.REQ)
    socket.connect ("ipc:///tmp/nxt_bluetooth_robot")
    
    while not done:
        mouse_pos = Point(*pygame.mouse.get_pos())
        if not limit.contains_point(mouse_pos):
            mouse_pos = mouse_pos.project_to_circle(limit)
        time_passed = clock.tick(MAX_FPS)
        # handle user events
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done = True
            elif event.type == pygame.KEYDOWN:
                # quit on ctrl-q
                if event.key == pygame.K_q:
                    rctrl_pressed = pygame.key.get_mods() & pygame.KMOD_RCTRL
                    lctrl_pressed = pygame.key.get_mods() & pygame.KMOD_LCTRL
                    if rctrl_pressed or lctrl_pressed:
                        done = True
                # move origin up on press of 'a'
                elif event.key == pygame.K_a:
                    origin_pos = Point(origin_pos.x, origin_pos.y - 1)
                # move origin down on press of 'd'
                elif event.key == pygame.K_d:
                    origin_pos = Point(origin_pos.x, origin_pos.y + 1)
            elif event.type == pygame.MOUSEBUTTONDOWN:
                # make direction-vector follow mouse if left button is held
                if event.button == 1:
                    left_mouse_pressed = True
                # make movement-vector point to mouse on right-click
                elif event.button == 3:
                    movement_vector_pos = mouse_pos
            elif event.type == pygame.MOUSEBUTTONUP:
                # stop rotation-vector following mouse on left-mouse up
                if event.button == 1:
                    left_mouse_pressed = False
                    stop_rotation = True
                    stop_movement = True
        if left_mouse_pressed:
            rotation_vector_pos = mouse_pos
        if stop_rotation:
            rotation_vector_pos = ROBOT_POS
            stop_rotation = False
        if stop_movement:
            movement_vector_pos = ROBOT_POS
            stop_movement = False
        
        # recompute
        rotation_vector = Line(ROBOT_POS, rotation_vector_pos,
            (screen, ROTATION_VECTOR_COLOR, ROTATION_VECTOR_WIDTH))
        movement_vector = Line(ROBOT_POS, movement_vector_pos,
            (screen, DIRECTION_VECTOR_COLOR, DIRECTION_VECTOR_WIDTH))
        origin_line = Line(ROBOT_POS, origin_pos,
            (screen, COORDS_COLOR, COORDS_WIDTH))
        
        # log values to stdout
        _movement_rotation = movement_vector.angle_with_line(rotation_vector)
        angle_movement_rotation = int(_movement_rotation) % 360
        _origin_movement = origin_line.angle_with_line(movement_vector)
        angle_origin_movement = int(_origin_movement) % 360
        rotation_speed = int(rotation_vector.length())
        movement_speed = int(movement_vector.length())
        
        
        # DO MAGIC
        #print >> sys.stderr, angle_movement_rotation, angle_origin_movement, rotation_speed, movement_speed
        Vd = rotation_speed / 255.0;
        
        m1 = Vd * sin(radians(angle_movement_rotation))
        m2 = Vd * cos(radians(angle_movement_rotation))
        m3 = Vd * cos(radians(angle_movement_rotation))
        m4 = Vd * sin(radians(angle_movement_rotation)) 
        
        m1 *= 255
        m2 *= 255
        m3 *= -255
        m4 *= -255
        
        # Send to bluetooth server
        socket.send ("1 " + str(int(m1)) + " " + str(int(m2)) + " "+ str(int(m3)) + " "+ str(int(m4)) )
        # Read reply
        print socket.recv()
        
        time.sleep(0.05)
        print int(m1), int(m2), int(m3), int(m4)
        print 'angle(movement, rotation) = %d' % angle_movement_rotation
        print 'angle(origin, movement) = %d' % angle_origin_movement
        print 'speed(rotation) = %d' % rotation_speed
        print 'speed(movement) = %d' % movement_speed
        print '-' * 80
        
        
        # redraw
        screen.fill(BG_COLOR)
        robot.draw()
        limit.draw()
        origin_line.draw()
        rotation_vector.draw()
        movement_vector.draw()
        
        # FIXME: moving origin line rotates robot-sprite
        (rot_robot_base, rot_robot_base_rect) = rotate_center(robot_base,
            angle_origin_movement)
        (rot_robot_top, rot_robot_top_rect) = rotate_center(robot_top,
            (angle_movement_rotation + angle_origin_movement) % 360)
        rot_robot_base_rect.center = CENTRE
        rot_robot_top_rect.center = CENTRE
        screen.blit(rot_robot_base, rot_robot_base_rect)
        screen.blit(rot_robot_top, rot_robot_top_rect)
        pygame.display.flip()

if __name__ == '__main__':
    run_app()
