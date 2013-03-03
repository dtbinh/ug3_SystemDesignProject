import pygame

def ctrl_pressed():
    kmods = pygame.key.get_mods()
    rctrl_down = kmods & pygame.KMOD_RCTRL
    lctrl_down = kmods & pygame.KMOD_LCTRL
    return rctrl_down or lctrl_down

def is_quit_event(event):
    if event.type == pygame.QUIT:
        return True
    elif event.type == pygame.KEYDOWN:
        if event.key == pygame.K_q:
            if ctrl_pressed():
                return True
    return False

def is_leftmouse_event(event):
    return event.type == pygame.MOUSEBUTTONDOWN and event.button == 1

def is_rightmouse_event(event):
    return event.type == pygame.MOUSEBUTTONDOWN and event.button == 3

def draw_crosshair(surface, (x, y), radius=20, width=1, color=(255, 0, 0)):
    xmin = x - radius + width
    xmax = x + radius - width
    ymin = y - radius + width
    ymax = y + radius - width
    pygame.draw.circle(surface, color, (x, y), radius, width)
    pygame.draw.line(surface, color, (xmin, y), (xmax, y), width)
    pygame.draw.line(surface, color, (x, ymin), (x, ymax), width)
