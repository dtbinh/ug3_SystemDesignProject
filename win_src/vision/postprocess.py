import sys
import os
import time
import math
import socket
import copy


from features import Entity


class Postprocessor:
    
    def __init__(self):
        self.past_entities = [None, None, None]
        self.past_times = [None, None, None]
    
    # TODO: start with empty
    def predict(self, future_time):
        print("PREDICT")
        return self.past_entities[0]
    
    def update(self, current_ents, current_time):
        new_ents = copy.deepcopy(current_ents)
        
        #past_entities[3] = past_entities[2]
        self.past_entities[2] = self.past_entities[1]
        self.past_entities[1] = self.past_entities[0]
        self.past_entities[0] = new_ents;
        
        #past_times[3] = past_times[2]
        self.past_times[2] = self.past_times[1]
        self.past_times[1] = self.past_times[0]
        self.past_times[0] = current_time
        
        #for name in ['yellow', 'blue', 'ball']:
        #    entity = self.past_entities[0][name]
        #    entity._velocity = self.getVelocity(name) 
        
        
    def getVelocity(self, entity_name):
        if (self.past_entities[0] == None or 
            self.past_entities[1] == None or
            self.past_entities[2] == None):
            return (0, 0)
        
        interval0 = self.past_times[0] - self.past_times[1]  
        if (interval0 == 0):
            return (0, 0)
        
        speedX = (self.past_entities[1][entity_name].coordinates()[0] - self.past_entities[0][entity_name].coordinates()[0]) / interval0;
        speedY = (self.past_entities[1][entity_name].coordinates()[1] - self.past_entities[0][entity_name].coordinates()[1]) / interval0; 
        
        interval1 = self.past_times[1] - self.past_times[2]
        if (interval1 == 0):
            return (0, 0)
        
        
        speedX = 0.7 * speedX + (0.3 * (self.past_entities[2][entity_name].coordinates()[0] - self.past_entities[1][entity_name].coordinates()[0]) / interval1)
        speedY = 0.7 * speedY + (0.3 * (self.past_entities[2][entity_name].coordinates()[1] - self.past_entities[1][entity_name].coordinates()[1]) / interval1)
        
        return (speedX, speedY)
        
        