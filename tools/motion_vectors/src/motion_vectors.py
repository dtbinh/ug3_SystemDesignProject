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
    Holonomic motor speeds for current vectors
    Angle between rotation-/movement-direction
    Angle between origin/movement-direction
    Rotation speed
    Movement speed
The motor speeds are also sent to nxt_bluetooth_robot via IPC. This can be
disabled (e.g. for debugging purposes) via passing --no-ipc to the script.
Angles are measured in counter-clockwise direction (in [0:360))
Rotation/Movement speed is the rotation/movement vector norm (in [0:255])
"""


from math import cos, sin, radians
import pygame

from geometry import Circle, Line, Point
from pygame_helpers import rotate_center

# robot constants
ROBOT_MAX_SPEED = 255

# pygame/app constants
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
LIMIT_RADIUS = ROBOT_MAX_SPEED
LIMIT_COLOR = (0, 0, 0)
LIMIT_WIDTH = 3
ROTATION_VECTOR_COLOR = (255, 0, 0)
ROTATION_VECTOR_WIDTH = 2
COORDS_COLOR = (0, 0, 255)
COORDS_WIDTH = 1
DIRECTION_VECTOR_COLOR = (255, 127, 0)
DIRECTION_VECTOR_WIDTH = 2

def run_app(enable_ipc=True):
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
    
    # set up IPC
    if enable_ipc:
        import time
        import zmq
        context = zmq.Context()
        print "Connecting to server..."
        socket = context.socket(zmq.REQ)
        socket.connect("ipc:///tmp/nxt_bluetooth_robot")
    
    # main loop
    done = False
    left_mouse_pressed = False
    stop_rotation = False
    stop_movement = False
    while not done:
        time_passed = clock.tick(MAX_FPS)
        mouse_pos = Point(*pygame.mouse.get_pos())
        # restrict vectors to a maximum length of ROBOT_MAX_SPEED
        if not limit.contains_point(mouse_pos):
            mouse_pos = mouse_pos.project_to_circle(limit)
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
        _movement_rotation = movement_vector.angle_with_line(rotation_vector)
        angle_movement_rotation = int(_movement_rotation) % 360
        _origin_movement = origin_line.angle_with_line(movement_vector)
        angle_origin_movement = int(_origin_movement) % 360
        rotation_speed = int(rotation_vector.length())
        movement_speed = int(movement_vector.length())
        
        # convert speeds/angles to holonomic motor instructions
        # same idea as res/legacy/WPILib/RobotDrive.cpp:MecanumDrive_Polar
        # TODO consider the main differences:
        #  1) WPILib gets the cos/sin values from the movement angle
        #     (here: |angle_origin_movement|) and adds the rotation angle on
        #     top of that (here: |angle_movement_rotation|)
        #     --> why is this not done here?
        #  2) WPILib normalizes the motor speeds (see code below)
        #     --> why would you want to/not want to do this?
        dirInRad = radians(angle_movement_rotation)
        sinD = sin(dirInRad)
        cosD = cos(dirInRad)
        m1 = rotation_speed * sinD
        m2 = rotation_speed * cosD
        m3 = -rotation_speed * cosD
        m4 = -rotation_speed * sinD
        motor_speeds = [m1, m2, m3, m4]
        """
        # normalize motor speeds if any one is higher than ROBOT_MAX_SPEED
        max_speed = float(max([abs(speed) for speed in motor_speeds]))
        if max_speed > ROBOT_MAX_SPEED:
            m1, m2, m3, m4 = [speed / max_speed for speed in motor_speeds]
        """
        # convert to integers for communication
        m1 = int(m1)
        m2 = int(m2)
        m3 = int(m3)
        m4 = int(m4)
        
        # send to bluetooth server + consume/print reply
        if enable_ipc:
            socket_message = "1 %d %d %d %d" % (m1, m2, m3, m4)
            socket.send(socket_message)
            print socket.recv()
            time.sleep(0.05)
        
        # log values to stdout
        print m1, m2, m3, m4
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
    import sys
    enable_ipc = True
    if len(sys.argv) > 1:
        if sys.argv[1] == '--no-ipc':
            enable_ipc = False
    
    run_app(enable_ipc)
