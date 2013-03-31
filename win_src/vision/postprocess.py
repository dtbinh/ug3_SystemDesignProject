import sys
import os
import time
import math
import socket
import copy


from features import Entity


class Postprocessor:
    
    def __init__(self):
        pass
    
    def predict(self, current_ents, current_time):
        predict_ents = copy.deepcopy(current_ents)
        
        # Predict
        predict_ents['yellow']._coordinates = (666, 666)
        
        return predict_ents