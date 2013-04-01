package behaviors;

import robot.Robot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.subsumption.Behavior;

public class ExecutePlan implements Behavior {
	Robot superRobot;
	private boolean suppressed = false;
	public ExecutePlan(Robot thisRobot) {
		superRobot = thisRobot;
	}

	@Override
	public void action() {
		
		Navigator aNav = superRobot.getNav();
		aNav.singleStep(true);
		while (!suppressed && !aNav.pathCompleted()){
			aNav.followPath();
		}
		if (aNav.pathCompleted()){
			superRobot.requestData();
		}

	}

	@Override
	public void suppress() {
		suppressed = true;

	}

	@Override
	public boolean takeControl() {
		if (superRobot.hasPlan()){
			return true;
		}
		
	}

}
