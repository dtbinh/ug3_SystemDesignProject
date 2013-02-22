package Planning;

import JavaVision.Position;

public class M2T1 {
	static VisionReader vision;
	static RobotMath rmaths = new RobotMath();
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
			try {	
				Thread.sleep(40);
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
		
		
		
		Command commandContainer = plannedCommands.getFirst();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand();
			plannedCommands.pop();
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = rmaths.euclidDist(ourRobot.getCoors(), 
					  					    moveCommand.moveTowardsPoint);
			if (rmaths.euclidDist(ourRobot.getCoors(), ball.getCoors())
												< 100){
				rmaths.toggleWantsToStop();
			}
			if (dist < 20.0) {
				visitedCurrent = true;				
			}
			else {
				sendMoveCommand(moveCommand);
			}
		}
		
		// !!!! execution phase !!!!
		if (visitedCurrent) {
			visitedCurrent = false;
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
		
	}
	
	static void sendKickCommand() {
		// TODO: implement
	}
	
	static boolean haveBall() {
		//TODO: implement
		return false;
	}
	
	static void getCommands() {
		// TODO: implement (From ObjectAvoidance)
	}
}



