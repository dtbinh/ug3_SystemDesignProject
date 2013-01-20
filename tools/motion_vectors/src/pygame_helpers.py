import pygame

def rotate_center(image, angle):
    orig_rect = image.get_rect()
    rot_image = pygame.transform.rotate(image, angle)
    rot_rect = rot_image.get_rect()
    rot_rect.center = orig_rect.center
    return (rot_image, rot_rect)
