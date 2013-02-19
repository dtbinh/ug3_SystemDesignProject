package Planning;

// TODO: remove ArrayList legacy
import java.util.ArrayList;

import JavaVision.Position;

/**
 * GeneralPlanningScript is an outline for how all 
 * strategy scripts should look.
 * I recommend reading the documentation in RobotMath and 
 * ObjectAvoidance before starting any planning task. 
 * Being familiar with the ObjectDetails subclasses (balls and robots)
 * might also be of use
 * 
 * @see RobotMath
 * @see ObjectAvoidance
 * @see ObjectDetails
 * 
 * @author      Caithan Moore s1024940
 */
public class GeneralPlanningScript extends Thread {
	static VisionReader vision;
	static RobotMath rmaths = new RobotMath();
	
	// TODO: remove and migrate to CommandStack implementation
	static ArrayList<Position> movementDestinations = new ArrayList<Position>(); 
	static ArrayList<Position> rotationDestinations = new ArrayList<Position>();
	static ArrayList<Boolean> hardRotates = new ArrayList<Boolean>();
	private static boolean hasCommands = false;
	private static boolean visitedCurrent = false;
	
	private static boolean shootingRight = vision.getDirection() == 0;
	private static Position ourGoal = shootingRight ?
										RobotMath.goalR.getCoors() :
										RobotMath.goalL.getCoors();
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	
	static CommandStack plannedCommands = new CommandStack();
	
	public static void main(String[] args){
		vision = new VisionReader();
		rmaths.init(); // THOU SHALT NOT NOT DO THIS!
		while(true) {
			//sending too many requests to the vision system is bad.
			//sleeping for 40 just about fixes the issue.
			//this still gives us ~25 commands per second, 
			//and since we can only get about 30 fps from camera,
			//it's not too bad.
			try {	
				sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Sleep interruption in Planning script");
				e.printStackTrace();
			}
			if (vision.readable()) {
				doStuff();
			}
		}
	}
	
	static void doStuff() {
		// AGAIN: THOU SHALT NOT NOT DO THESE
		ourRobot = vision.getOurRobot(); 
		theirRobot = vision.getTheirRobot(); 
		ball = vision.getBall();
		rmaths.initLoop();
		
		if (!hasCommands) {
			getCommands();
		}
		
		// !!!! planning phase !!!!
		if (haveBall()) {
			// opportunistic strategy:
			// if the current position has a good enough probability to
			// score a goal then drop everything the robot is doing and
			// perform a kick as the next action (and then resume with
			// whatever you were doing)
			if (wantToKick() && !opponentIsInWay()) {
				plannedCommands.pushKickCommand(ourRobot.getCoors(),
												ball.getCoors());
			}
		}
		else {
			if (opponentHasBall()) {
				plannedCommands.clear();
				if (opponentIsCloserToOurGoal()) {
					plannedCommands.pushMoveCommand(ourGoal);
				}
				else {
					// TODO: mirror opponent movement
					plannedCommands.pushMoveCommand(null);
				}
			}
			else {
				// TODO: make this predicted position of ball w.r.t ball
				// trajectory/speed and own speed
				plannedCommands.pushMoveCommand(ball.getCoors());
			}
		}
		
		Command commandContainer = plannedCommands.getFirst();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand();
			plannedCommands.pop();
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = rmaths.euclidDist(ourRobot.getCoors(), 
					  					    moveCommand.moveTowardsPoint);
			if (dist < 100.0) {
				visitedCurrent = true;
				//be sure to note this function,
				//if (euclidian distance to point) < 100
				
				//rmaths.toggleWantsToStop();
				
				//this will do nothing if hardRotate is off, 
				//but will make the robot stop and rotate if it is on. 
				
				//String signal = rmaths.getSigToPoint(ourRobot, movementDestinations.get(0), rotationDestinations.get(0), hardRotates.get(0));
				//something.sendSignal(signal)
			}
			else {
				sendMoveCommand(moveCommand);
			}
		}
		
		// !!!! execution phase !!!!
		if (visitedCurrent) {
			visitedCurrent = false;
			// TODO: remove legacy arraylist version
			movementDestinations.remove(0);
			rotationDestinations.remove(0);
			hardRotates.remove(0);
			// new linked list version
			plannedCommands.pop();
		}
	}
	
	static boolean opponentIsCloserToOurGoal() {
		// TODO: implement
		return false;
	}
	
	static boolean opponentHasBall() {
		// TODO: implement
		return false;
	}
	
	static boolean opponentIsInWay() {
		// TODO: implement
		return false;
	}
	
	static boolean wantToKick() {
		double positionScore = rmaths.getPositionScore(ourRobot.getCoors(),
													   shootingRight);
		return positionScore > Math.random();
	}
	
	static void sendMoveCommand(MoveCommand moveCommand) {
		// TODO: implement
	}
	
	static void sendKickCommand() {
		// TODO: implement
	}
	
	static boolean haveBall() {
		//TODO: implement
		return false;
	}
	
	static void getCommands() {
		// TODO: implement
	}
}
