import math


class Circle(object):
    def __init__(self, centre, radius, drawinfo=None):
        self.centre = centre
        self.radius = radius
        self.drawinfo = drawinfo
        if drawinfo is not None:
            self.surface, self.color, self.width = drawinfo
    
    def contains_point(self, point):
        (x, y) = point
        (centre_x, centre_y) = self.centre
        return (x - centre_x) ** 2 + (y - centre_y) ** 2 <= self.radius ** 2
    
    def contains_line(self, line):
        start_in_circle = self.contains_point(line.start)
        end_in_circle = self.contains_point(line.end)
        return start_in_circle and end_in_circle
    
    def draw(self):
        import pygame
        pygame.draw.circle(self.surface, self.color, self.centre, self.radius,
            self.width)


class Line(object):
    def __init__(self, start, end, drawinfo=None):
        self.start = start
        self.end = end
        self.drawinfo = drawinfo
        if drawinfo is not None:
            self.surface, self.color, self.width = drawinfo
    
    def orthogonal_line(self, point):
        (Ax, Ay) = self.start
        (Bx, By) = self.end
        (Cx, Cy) = point
        Dx = Cx + (Ay - By)
        Dy = Cy + (Ax - Bx)
        return Line(point.copy(), Point(Dx, Dy), self.drawinfo)
    
    def draw(self):
        import pygame
        pygame.draw.line(self.surface, self.color, self.start, self.end,
            self.width)
    
    def length(self):
        return self.start.distance_to_point(self.end)
    
    def angle_with_line(self, other):
        angle1 = math.atan2(self.start.y - self.end.y,
                            self.start.x - self.end.x)
        angle2= math.atan2(other.start.y - other.end.y,
                            other.start.x - other.end.x)
        return math.degrees(angle1 - angle2)


class Point(tuple):
    def __new__(self, x, y):
        return super(Point, self).__new__(self, tuple((x, y)))
    
    def __init__(self, x, y):
        super(Point, self).__init__(x, y)
        self.x = x
        self.y = y
    
    def __eq__(self, other):
        return self.x == other.x and self.y == other.y
     
    def copy(self):
        return Point(self.x, self.y)
    
    def distance_to_point(self, point):
        return math.sqrt((self.x - point.x) ** 2 + (self.y - point.y) ** 2)
    
    def project_to_circle(self, circle):
        (Cx, Cy) = circle.centre
        norm = self.distance_to_point(circle.centre)
        X = Cx + (self.x - Cx) / norm * circle.radius
        Y = Cy + (self.y - Cy) / norm * circle.radius
        return Point(X, Y)
