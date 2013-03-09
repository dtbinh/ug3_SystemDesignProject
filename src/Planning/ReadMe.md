1) define a Ball, 2 Robots, a WorldState, a ControlGUI, a null instance of a Runner and a Vision. Oh and a ControlGUI.

2) set up the robots and start the thread

3) Planning thread starts, start the vision here, and communication if doing so, then finally call the method that calculates all the stuff

4) startVision(), create a new WorldState, ThresholdState and PitchConstants(0), define camera parameters, 
	try {
		give the Vision some params, and the ControlGUI, then .initGUI();
	}
	catch stupid exceptions

5) write the magic method that does the work. Great success will be had.
