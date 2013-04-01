package behaviors;

import robot.Robot;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;

public class PlanWithBall implements Behavior {
	private boolean suppressed = false;
	private Robot superRobot;
	private Pose us;
	private Pose goal;
	private Pose them;
	
	public PlanWithBall(Robot thisRobot) {
		superRobot = thisRobot;
	}

	
	@Override
	public void action() {//TODO: Implement properly.
		us = superRobot.ourPose;
		them = superRobot.theirPose;
		goal = superRobot.goalPose;
		superRobot.steerSetup(); //Simulate 
		superRobot.pathNav.getPoseProvider().setPose(us); 
		

	}

	@Override
	public void suppress() {
		suppressed = true;

	}

	@Override
	public boolean takeControl() { //TODO:Add conditions
		// TODO Auto-generated method stub
		return false;
	}

}
