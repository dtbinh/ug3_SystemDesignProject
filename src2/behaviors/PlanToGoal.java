package behaviors;

import java.util.ArrayList;

import robot.Robot;
import lejos.geom.Point;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
import lejos.robotics.subsumption.Behavior;

public class PlanToGoal implements Behavior {
	private Robot superRobot;
	private Pose us;
	private Pose goal;
	private boolean suppressed;
	private Pose them;
	
	//Does the whole craycray turn around (BRIGGGGHT EYEEES), drive backwards, then return-around to da ball. 
	//3 commands or GTFO
	public PlanToGoal(Robot thisRobot) {
		superRobot = thisRobot;
	}

	@Override
	public void action() {
		PathPlanner pp = new PathPlanner();
		suppressed = false;
		System.out.println("WantNewPath");
		us = superRobot.getOurPose();
		them = superRobot.getTheirPose();
		goal = superRobot.getGoalPose();
		//Fuck da current path - shit's changed or I wouldn't be here!
		//I AM A {DIFFERENTIAL, STEERED} ROBOT. HEAR ME ROAR
		
		superRobot.doSetup();
		Navigator myNav = superRobot.getNav();
		myNav.getPoseProvider().setPose(us); 
		
		if (us.distanceTo(them.getLocation()) < 0.2) pp.updateEnemyPos(them);
		ArrayList<Waypoint> wps = pp.calculatePath(us, goal);
		for (int i = 0; i<wps.size();i++){
			myNav.addWaypoint(wps.get(i));
		}
		
		//done brah
		superRobot.setNewPath(false);	
		superRobot.setNav(myNav);
		System.out.println("PTG done");
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
