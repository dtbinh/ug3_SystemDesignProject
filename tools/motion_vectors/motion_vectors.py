import pygame
from geometry import *


# constants
MAX_FPS = 50
BG_COLOR = (0, 255, 0)
SCREEN_WIDTH = 800
SCREEN_HEIGHT = 600
CENTRE = Point(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2)

pygame.init()
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
clock = pygame.time.Clock()

ROBOT_POS = CENTRE.copy()
ROBOT_RADIUS = 20
ROBOT_COLOR = (0, 0, 0)
ROBOT_WIDTH = 0

LIMIT_POS = CENTRE.copy()
LIMIT_RADIUS = 255
LIMIT_COLOR = (0, 0, 0)
LIMIT_WIDTH = 3

VECTOR_COLOR = (255, 0, 0)
VECTOR_WIDTH = 2

ORIGIN_POS = Point((SCREEN_WIDTH / 2 - LIMIT_RADIUS) / 3, SCREEN_HEIGHT / 2)
anchor_pos = ORIGIN_POS.copy()
ANCHOR_RADIUS = 10
COORDS_COLOR = (0, 0, 255)
COORDS_WIDTH = 1

# main loop
done = False
log = True
_angle_anchor_vector = 0.0
_angle_origin_anchor = 0.0
_norm = 0
while not done:
    # TODO: move anchor
    # recompute
    robot = Circle(ROBOT_POS, ROBOT_RADIUS, (screen, ROBOT_COLOR, ROBOT_WIDTH))
    limit = Circle(LIMIT_POS, LIMIT_RADIUS, (screen, LIMIT_COLOR, LIMIT_WIDTH))
    mouse_pos = Point(*pygame.mouse.get_pos())
    vector = Line(mouse_pos, ROBOT_POS, (screen, VECTOR_COLOR, VECTOR_WIDTH))
    coord_drawinfo = (screen, COORDS_COLOR, COORDS_WIDTH)
    anchor = Circle(anchor_pos, ANCHOR_RADIUS, coord_drawinfo)
    anchor_line = Line(anchor_pos, ROBOT_POS, coord_drawinfo)
    origin_line = Line(ORIGIN_POS, ROBOT_POS, coord_drawinfo)
    # limit framerate
    time_passed = clock.tick(MAX_FPS)
    # handle user events
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            done = True
        if event.type == pygame.KEYDOWN:
            pass
            # TODO: (de)activate logging on 'l'
            # TODO: quit on ctrl q
    # redraw
    screen.fill(BG_COLOR)
    robot.draw()
    limit.draw()
    if not limit.contains_line(vector):
        vector = vector.project_to_circle(limit)
    vector.draw()
    anchor.draw()
    if anchor_pos != ORIGIN_POS:
        anchor_line.draw()
    origin_line.draw()
    pygame.display.flip()
    # log values to stdout
    if log:
        logged = False
        angle_anchor_vector = anchor_line.angle_with_line(vector)
        if _angle_anchor_vector != angle_anchor_vector:
            print 'angle(anchor, vector) = ', angle_anchor_vector
            _angle_anchor_vector = angle_anchor_vector
            logged = True
        angle_origin_anchor = origin_line.angle_with_line(anchor_line)
        if _angle_origin_anchor != angle_origin_anchor:
            print 'angle(origin, anchor) = ', angle_origin_anchor
            _angle_origin_anchor = angle_origin_anchor
            logged = True
        norm = int(vector.length())
        if norm != _norm:
            print 'norm(vector) = ', int(vector.length())
            _norm = norm
            logged = True
        if logged:
            print '-' * 80
