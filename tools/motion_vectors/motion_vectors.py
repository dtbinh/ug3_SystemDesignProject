#!/usr/bin/python

# Info:
#     Blue line: origin
#     Red line: robot rotation-direction (switch with left click)
#     Orange line: robot movement-direction (switch with right click)
#     Printed to stdout:
#         Angle between rotation-/movement-direction
#         Angle between origin/movement-direction
#         Rotation speed
#         Movement speed
#     Angles are measured in counter-clockwise direction (in [0:360))
#     Rotation/Movement speed is the rotation/movement vector norm (in [0:255])


import pygame
from src.geometry import Circle, Line, Point
from src.pygame_helpers import rotate_center


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

LIMIT_POS = CENTRE.copy()
LIMIT_RADIUS = 255
LIMIT_COLOR = (0, 0, 0)
LIMIT_WIDTH = 3

rotation_vector_pos = Point(WIDTH / 2 + LIMIT_RADIUS, HEIGHT / 2)
ROTATION_VECTOR_COLOR = (255, 0, 0)
ROTATION_VECTOR_WIDTH = 2

ORIGIN_POS = Point(0, HEIGHT / 2)
COORDS_COLOR = (0, 0, 255)
COORDS_WIDTH = 1

movement_vector_pos = Point(WIDTH / 2 - LIMIT_RADIUS, HEIGHT / 2)
DIRECTION_VECTOR_COLOR = (255, 127, 0)
DIRECTION_VECTOR_WIDTH = 2

# resources
robot_base = pygame.image.load('res/base.png')
robot_top = pygame.image.load('res/top.png')

# set up pygame
pygame.init()
screen = pygame.display.set_mode((WIDTH, HEIGHT))
clock = pygame.time.Clock()

# main loop
robot = Circle(ROBOT_POS, ROBOT_RADIUS, (screen, ROBOT_COLOR, ROBOT_WIDTH))
limit = Circle(LIMIT_POS, LIMIT_RADIUS, (screen, LIMIT_COLOR, LIMIT_WIDTH))
origin_line = Line(ROBOT_POS, ORIGIN_POS, (screen, COORDS_COLOR, COORDS_WIDTH))
done = False
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
            q_pressed = event.key == pygame.K_q
            rctrl_pressed = pygame.key.get_mods() & pygame.KMOD_RCTRL
            lctrl_pressed = pygame.key.get_mods() & pygame.KMOD_LCTRL
            if q_pressed and (rctrl_pressed or lctrl_pressed):
                done = True
        elif event.type == pygame.MOUSEBUTTONDOWN:
            if event.button == 1:
                rotation_vector_pos = mouse_pos
            elif event.button == 3:
                movement_vector_pos = mouse_pos
    # recompute
    rotation_vector = Line(ROBOT_POS, rotation_vector_pos,
        (screen, ROTATION_VECTOR_COLOR, ROTATION_VECTOR_WIDTH))
    movement_vector = Line(ROBOT_POS, movement_vector_pos,
        (screen, DIRECTION_VECTOR_COLOR, DIRECTION_VECTOR_WIDTH))
    # log values to stdout
    _movement_rotation = movement_vector.angle_with_line(rotation_vector)
    angle_movement_rotation = int(_movement_rotation) % 360
    _origin_movement = origin_line.angle_with_line(movement_vector)
    angle_origin_movement = int(_origin_movement) % 360
    rotation_speed = int(rotation_vector.length())
    movement_speed = int(movement_vector.length())
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
    (rot_robot_base, rot_robot_base_rect) = rotate_center(robot_base,
        angle_origin_movement)
    (rot_robot_top, rot_robot_top_rect) = rotate_center(robot_top,
        (angle_movement_rotation + angle_origin_movement) % 360)
    rot_robot_base_rect.center = CENTRE
    rot_robot_top_rect.center = CENTRE
    screen.blit(rot_robot_base, rot_robot_base_rect)
    screen.blit(rot_robot_top, rot_robot_top_rect)
    pygame.display.flip()
