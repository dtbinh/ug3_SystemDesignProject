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

	@Override
	public void action() {
		suppressed = false;
		Navigator aNav = superRobot.getNav();
		aNav.singleStep(true);
		while (!suppressed && !aNav.pathCompleted()){
			aNav.followPath();
			Thread.yield();
		}
		if (aNav.pathCompleted()){
			System.out.println("done exe");
			superRobot.requestData();
		}

		
		
	}

	@Override
	public void suppress() {
		suppressed = true;

	}

	@Override
	public boolean takeControl() {
		return (superRobot.hasPlan());	
	}
	
	
	//TODO: This is shit and not needed anymore.
	public Navigator doAMove(Navigator aNav){
		Navigator newNav = aNav;
		Path ps = aNav.getPath();
		Pose wPoint =  ps.get(0).getPose(); 
		Pose startLoc = superRobot.getOurPose();
		Pose endLoc = superRobot.getGoalPose();
		
		double d2point = startLoc.getLocation().distance(endLoc.getLocation());
		
		if (wPoint.getHeading() != startLoc.getHeading() && 
			wPoint.getLocation().equals(startLoc.getLocation())){ //initial movement
			newNav.rotateTo(wPoint.getHeading());
			System.out.println("iROT");
		} else {
			if (wPoint.getLocation().equals(endLoc.getLocation())){ //end rotate
				newNav.rotateTo(wPoint.getHeading());
				System.out.println("nRot");
			} else { //SL movement
				System.out.println("SLM");
				superRobot.getPilot().travel(-d2point);
				
			}
			
		}
		ps.remove(0);
		System.out.println(ps.size());
		newNav.clearPath();
		for ( int i = 0; i < ps.size(); i++){
			newNav.addWaypoint(ps.get(i));
		}
		return newNav;
	}
		

}
