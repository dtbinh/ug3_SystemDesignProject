package Planning;

import java.awt.Point;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

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
public class Milestone4 extends Thread {
	public final static int GOT_THERE_DIST = 10;
	public final static int BEHIND_BALL_DIST = 50;
	public final static int DRIBBLE_DIST = 60;

	static VisionReader vision;
	static RobotMath robotMath;
	
	private static Context context;
    private static Socket socket;
	
	static boolean shootingRight;
	static Position theirGoal;
	static Position ourGoal;
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	static long kickTimeOut;
	final static int kickAllowance = 1500;
	
	static CommandStack plannedCommands;
	
	public static void main(String[] args) {
		vision = new VisionReader(args[0]); // Our Colour - MAKE GUI 4 DIS
		//vision = new VisionReader("yellow");;
		robotMath = new RobotMath(); robotMath.init();
		
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://127.0.0.1:5555");
        
        sendZeros();
        
		shootingRight = args[1].equals("right");
		System.out.println("Shooting right: " + shootingRight);
		
		int taskNo = Integer.parseInt(args[2]); // Milestone task number
		
		theirGoal = shootingRight ? robotMath.goalR.getCoors() : robotMath.goalL.getCoors();
		ourGoal = !shootingRight ? robotMath.goalL.getCoors() : robotMath.goalR.getCoors() ;
		plannedCommands = new CommandStack();
		
		while (true) {
			if (!vision.readable()) {
				continue;
			}
			
			updateWorldState();
			doTask(taskNo);
		}
	}
	
	static void updateWorldState() {
		// update state of world
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		robotMath.initLoop();
	}

	static void doTask(int taskNo) {
		/*
		 * Do we need to wait for the ball to move before we 
		 * start going towards it?
		 * For part 3 do we have to score past the robot?
		 */
		if(!haveBall()){
			Position intersectionPoint = ball.getReachableCoors(ourRobot);
			planMove(intersectionPoint);
			System.out.println("Moving to predicted point " + intersectionPoint);
		}
		else {
			if (taskNo == 1 || taskNo == 2) {
				// just stop when we have the ball
				sendZeros();
			} else if (taskNo == 3) {
				// TODO: score a goal
				if (wantToKick()) {
					plannedCommands.pushKickCommand();
					System.out.println("planning to kick");
				}
				else {
					planMove(theirGoal);
					System.out.println("planning to DRIBBLE");
				}
			} else {
				System.err.println("Task not specified.");
				System.exit(0);
			}
		}
		
		playExecute();
	}
	
	static void playExecute() {
		if (!plannedCommands.isEmpty()) {
			Command commandContainer = plannedCommands.pop();
			if (commandContainer instanceof KickCommand) {
				sendKickCommand(commandContainer);
				kickTimeOut = System.currentTimeMillis() + kickAllowance;
			}
			else if (commandContainer instanceof MoveCommand) {
				MoveCommand moveCommand = (MoveCommand) commandContainer;
				if (RobotMath.euclidDist(ourRobot.getCoors(), moveCommand.moveTowardsPoint) 
						< GOT_THERE_DIST) {
					System.out.println("-----been there, done that");
					playExecute();
				}
				else {
					sendMoveCommand(moveCommand);
				}
			}
			else { playExecute(); }
		}
	}
	
	
	private static void planMove(Position coors, Robot obstacle, Position coorsToFace) {
		ArrayList<Point> path = PathSearchHolly.getPath2(
				new Point(coors.getX(), coors.getY()), 
				new Point(ourRobot.getCoors().getX(), ourRobot.getCoors().getY()), 
				(int) Math.toDegrees(ourRobot.getAngle()), 
				new Point(obstacle.getCoors().getX(), obstacle.getCoors().getY()), 
				(int) Math.toDegrees(obstacle.getAngle()), 
				shootingRight ? PathSearchHolly.LEFT : PathSearchHolly.RIGHT);
		        // if we're shootingRight, *our* side is LEFT
		plannedCommands.pushMoveCommand(path, coorsToFace);
	}
	
	private static void planMove(Position coors, Robot obstacle) {
		planMove(coors, obstacle, coors);
	}
	
	private static void planMove(Position coors, Position coorsToFace) {
		planMove(coors, theirRobot, coorsToFace);
	}
	
	private static void planMove(Position coors) {
		planMove(coors, theirRobot);
	}
	
	static boolean haveBall() {
		double distToBall = RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors());
		boolean closeToBall = distToBall < DRIBBLE_DIST;
		boolean facingBall = RobotMath.isTargeting(ourRobot, ball.getCoors());
		
		return closeToBall && facingBall;
	}

	static boolean wantToKick() {
		double positionScore = robotMath.getPositionScore(ourRobot.getCoors(),
													shootingRight, 0.5);
		double hitScore = robotMath.getHitScore(ourRobot, shootingRight);
		boolean kickingAllowed = System.currentTimeMillis() > kickTimeOut;
		return kickingAllowed && (positionScore > 0.0) && (hitScore > 0.32);								
	}
	
	static boolean opponentHasBall() {
		Position ballPos = ball.getCoors();
		double distToBall = RobotMath.euclidDist(theirRobot.getCoors(), ballPos);
		boolean closeToBall = distToBall <= DRIBBLE_DIST;
		boolean facingBall = RobotMath.isTargeting(theirRobot, ballPos);
		
		return closeToBall && facingBall;
	}
	
	static boolean opponentHasClearShot(){
			if(opponentHasBall() && ObjectAvoidance.obstacleDetection
					(theirRobot.getCoors(), ourRobot.getCoors(), ourGoal)){
				return true;
			}
		return false;
	}

	static void sendreceive(String signal) {
		///*
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Receiving OK");
		// */
	}

	static void sendZeros() {
		sendreceive("1 0 0 0 0");
	}
	
	static void sendKickCommand(Command c) {
		sendZeros();
		sendreceive("3");
	}

	static void sendMoveCommand(Command command) {
		MoveCommand moveCommand = (MoveCommand) command;
		String signal = robotMath.getSigToPoint(
				ourRobot,
				moveCommand.moveTowardsPoint,
				moveCommand.rotateTowardsPoint,
				moveCommand.shouldMovementEndFacingRotateTowardsPoint);
		sendreceive(signal);
	}

}
