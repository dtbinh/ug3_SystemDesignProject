package Planning;

// TODO: remove ArrayList legacy
import java.awt.Point;
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
 * @author		c-w
 */
public class GeneralPlanningScript extends Thread {
	public static double HAS_BALL_DISTANCE_THRESHOLD = 30.0;
	public static double END_MOVE_START_ROTATE_DISTANCE_THRESHOLD = 100.0;
	
	static VisionReader vision;
	static RobotMath rmaths;
	
	static boolean shootingRight;
	static Position ourGoal;
	static Position theirGoal;
	static Position optimumGP;
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	
	static CommandStack plannedCommands;
	static boolean skipNextPlanningPhase;
	private static Position safePoint;
	
	public static void main(String[] args) {
		vision = new VisionReader(args[0]);
		rmaths = new RobotMath(); rmaths.init();
		shootingRight = vision.getDirection() == 0;
		float theirGoalAngle;
		float ourGoalAngle;
		if (shootingRight){
		ourGoal	= rmaths.goalR.getCoors();
		theirGoal = rmaths.goalL.getCoors();
		ourGoalAngle = rmaths.goalR.getAngle();
		theirGoalAngle = rmaths.goalL.getAngle();
		} else {
			ourGoal	= rmaths.goalL.getCoors();
			theirGoal = rmaths.goalR.getCoors();
			ourGoalAngle = rmaths.goalL.getAngle();
			theirGoalAngle = rmaths.goalR.getAngle();
		}
		plannedCommands = new CommandStack();
		skipNextPlanningPhase = false;
		optimumGP = rmaths.projectPoint(theirGoal, theirGoalAngle, 70);
		safePoint = rmaths.projectPoint(ourGoal, ourGoalAngle, 70 );
		
		
		while (true) {
			try {
				sleep(40);
			} catch (InterruptedException e) {
				System.err.println("Sleep interruption in Planning script");
				e.printStackTrace();
			}
			if (vision.readable()) {
				controlLoop();
			}
		}
	}
	
	static void controlLoop() {
		// update state of world
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		rmaths.initLoop();
		
		if (plannedCommands.isEmpty()) {
			getDefaultCommands();
		}
		
		// !!!! planning phase !!!!
		if (!skipNextPlanningPhase) {
			if (haveBall()) {
				// opportunistic strategy:
				// if the current position has a good enough probability to
				// score a goal then drop everything the robot is doing and
				// perform a kick as the next action (and then resume with
				// whatever you were doing)
				if (wantToKick()) {
					if (opponentIsInWay()) {
						//move to the optimum position
							move(optimumGP);
						} else {
							
							plannedCommands.pushKickCommand(ourRobot.getCoors(),
													ball.getCoors());
							skipNextPlanningPhase = true;
						}	
				} 
			}
			else {
				if (opponentHasBall()) {
					plannedCommands.clear();
					if (opponentIsCloserToOurGoal()) {
						//Run to da goal.
						move(safePoint);
						}
					else {
						// TODO: mirror opponent movement
						// TODO: move into intersection of opponent direction and centre of goal
						move(theirRobot.getCoors());
					}
				}
				else {
					// TODO: use predicted position of ball w.r.t ball trajectory/speed and own speed
					//
					// Task assigned to: Ozgur
					move(ball.getCoors());
				}
			}
		}
		
		// !!!! execution phase !!!!
		Command commandContainer = plannedCommands.getFirst();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand(plannedCommands.pop());
			skipNextPlanningPhase = false;
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = RobotMath.euclidDist(ourRobot.getCoors(), 
					  					       moveCommand.moveTowardsPoint);
			if (dist < END_MOVE_START_ROTATE_DISTANCE_THRESHOLD) {
				rmaths.toggleWantsToStop();
				if (dist < HAS_BALL_DISTANCE_THRESHOLD) {
					plannedCommands.pop();
				}
			}
			else {
				sendMoveCommand(moveCommand);
			}
		}
	}
	
	private static void move(Position coors) {
		ArrayList<Point> path = PathSearchHolly.getPath2(
				new Point(coors.getX(), coors.getY()), 
				new Point(ourRobot.getCoors().getX(), ourRobot.getCoors().getY()), 
				(int) Math.toDegrees(ourRobot.getAngle()), 
				new Point(theirRobot.getCoors().getX(), theirRobot.getCoors().getY()), 
				(int) Math.toDegrees(theirRobot.getAngle()), 
				shootingRight ? 1 : 0);
		plannedCommands.pushMoveCommand(path);
	}

	static boolean opponentIsCloserToOurGoal() {
		double ourDist = ObjectAvoidance.getDist(ourRobot.getCoors(), ourGoal);
		double theirDist = ObjectAvoidance.getDist(theirRobot.getCoors(),ourGoal);
		
		return theirDist < ourDist;
	}
	
	static boolean opponentHasBall() {
		Position ballPos = ball.getCoors();
		double distToBall = RobotMath.euclidDist(theirRobot.getCoors(), ballPos);
		boolean closeToBall = distToBall <= HAS_BALL_DISTANCE_THRESHOLD;
		boolean facingBall = rmaths.isFacing(theirRobot, ballPos);
		
		return closeToBall && facingBall;
	}
	
	static boolean opponentIsInWay() {
		return ObjectAvoidance.obstacleDetection(ourRobot.getCoors(),
												 theirRobot.getCoors(),
												 ourGoal);
	}
	
	static boolean wantToKick() {
		double positionScore = rmaths.getPositionScore(ourRobot.getCoors(),
													   shootingRight);
		return positionScore > Math.random();
	}
	
	static void sendMoveCommand(Command command) {
		MoveCommand moveCommand = (MoveCommand) command;
		String signal = rmaths.getSigToPoint(
				ourRobot,
				moveCommand.moveTowardsPoint,
				moveCommand.rotateTowardsPoint,
				moveCommand.shouldMovementEndFacingRotateTowardsPoint);
		sendreceive(signal);
	}
	
	static void sendKickCommand(Command c) {
		sendreceive("3");
	}

	static void sendreceive(String signal) {
		/*
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Receiving OK");
		 */
	}
	
	static boolean haveBall() {
		double distToBall = RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors());
		boolean closeToBall = distToBall < HAS_BALL_DISTANCE_THRESHOLD;
		boolean facingBall = rmaths.isFacing(ourRobot, ball.getCoors());
		
		return closeToBall && facingBall;
	}
	
	static void getDefaultCommands() {
		move(ball.getCoors());
	}
}