package robot;

import lejos.geom.Point;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;

public class PlanWithoutBall implements Behavior {
	private Robot superRobot;
	private Pose us;
	private Pose goal;
	
	//Does the whole craycray turn around (BRIGGGGHT EYEEES), drive backwards, then return-around to da ball. 
	//3 commands or GTFO
	public PlanWithoutBall(Robot thisRobot) {
		superRobot = thisRobot;
	}

	@Override
	public void action() {
		us = superRobot.ourPose;
		goal = superRobot.goalPose;
		//Fuck da current path - shit's changed or I wouldn't be here!
		superRobot.pathNav.clearPath();
		superRobot.pathNav.getPoseProvider().setPose(us); 
		
		//Initial Rotation
		float relativeBearing = us.getLocation().angleTo(goal.getLocation());
		float firstAngle = (relativeBearing < 180 ) ? relativeBearing + 180 : relativeBearing - 180; //face the opposite way
		Point firstPoint = us.getLocation();
		Pose firstPose = new Pose(firstPoint.x, firstPoint.y, firstAngle);
		superRobot.pathNav.addWaypoint(new Waypoint(firstPose));
		
		//Reverse movement
		Point goalLoc = goal.getLocation();
		superRobot.pathNav.addWaypoint(new Waypoint(goalLoc.x, goalLoc.y, firstAngle));
		
		//final turn, homie
		superRobot.pathNav.addWaypoint(new Waypoint(goal));
		
		//done brah
		superRobot.needsNewPath = false;
		
		
		

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
