package behaviors;

import robot.Robot;
import lejos.geom.Point;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;

public class PlanWithoutBall implements Behavior {
	private Robot superRobot;
	private Pose us;
	private Pose goal;
	private boolean suppressed;
	
	//Does the whole craycray turn around (BRIGGGGHT EYEEES), drive backwards, then return-around to da ball. 
	//3 commands or GTFO
	public PlanWithoutBall(Robot thisRobot) {
		superRobot = thisRobot;
	}

	@Override
	public void action() {
		suppressed = false;
		System.out.println("PWB start");
		us = superRobot.getOurPose();
		goal = superRobot.getGoalPose();
		//Fuck da current path - shit's changed or I wouldn't be here!
		superRobot.diffSetup();
		superRobot.getNav().getPoseProvider().setPose(us); 
		Navigator myNav = superRobot.getNav();
		//Initial Rotation
		float relativeBearing = us.getLocation().angleTo(goal.getLocation());
		float firstAngle = (relativeBearing < 180 ) ? relativeBearing + 180 : relativeBearing - 180; //face the opposite way
		Point firstPoint = us.getLocation();
		Pose firstPose = new Pose(firstPoint.x, firstPoint.y, firstAngle);
		//myNav.addWaypoint(new Waypoint(firstPose));
		
		//Reverse movement
		Point goalLoc = goal.getLocation();
		//myNav.addWaypoint(new Waypoint(goalLoc.x, goalLoc.y, firstAngle));
		
		//final turn, homie
		myNav.addWaypoint(new Waypoint(goal));
		
		//done brah
		superRobot.setNewPath(false);	
		superRobot.setNav(myNav);
		System.out.println("PWB done");
		Thread.yield();
		
	}

	@Override
	public void suppress() {
		suppressed = true;

	}

	@Override
	public boolean takeControl() {//TODO:Add conditions
		return (superRobot.needsNewPlan());
	}

}
