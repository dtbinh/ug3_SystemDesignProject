package robot;

import lejos.robotics.subsumption.Behavior;

public class PlanWithBall implements Behavior {

	private Robot superRobot;
	
	public PlanWithBall(Robot thisRobot) {
		superRobot = thisRobot;
	}

	
	@Override
	public void action() {
		// TODO Auto-generated method stub

	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean takeControl() {
		// TODO Auto-generated method stub
		return false;
	}

}
