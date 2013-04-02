package behaviors;

import robot.Robot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.pathfinding.Path;
import lejos.robotics.subsumption.Behavior;

public class ExecutePlan implements Behavior {
	Robot superRobot;
	private boolean suppressed = false;
	public ExecutePlan(Robot thisRobot) {
		superRobot = thisRobot;
	}

	public void action() {
		suppressed = false;
		Navigator aNav = superRobot.getNav();
		aNav.singleStep(true);
		while (!aNav.pathCompleted()){
			Thread.yield();
			aNav.followPath();
			superRobot.setNav(aNav);
		}
		Thread.yield();
	
	}


	public void suppress() {
		suppressed = true;
	}


	public boolean takeControl() {
		return (superRobot.hasPlan());	
	}
	
	
	
	
	
	
	
	
	
	
	

}
