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
public class GeneralPlanningScriptB extends Thread {
	public final static int GOT_THERE_DIST = 20;
	public final static int BEHIND_BALL_DIST = 40;
	public final static int DRIBBLE_DIST = 60;

	static VisionReader vision;
	static RobotMath robotMath;
	
	private static Context context;
    private static Socket socket;
	
	static boolean shootingRight;
	static Position theirGoal;
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	static long kickTimeOut;
	final static int kickAllowance = 2000;
	static long penaltyTimeOut;
	final static int penaltyAllowance = 20000;
	
	static CommandStack plannedCommands;

	static boolean playMode;
	static boolean penaltyDefMode;
	static boolean penaltyAtkMode;
	
	public static void main(String[] args) {
		int argc = args.length;
		vision = new VisionReader(args[0]); // Our Colour - MAKE GUI 4 DIS
		robotMath = new RobotMath(); robotMath.init();
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://127.0.0.1:5555");
        sendZeros();
		shootingRight = args[1].equals("right");
		System.out.println("Shooting right: " + shootingRight);
		
		theirGoal = shootingRight ? robotMath.goalR.getCoors() : robotMath.goalL.getCoors();
		
		plannedCommands = new CommandStack();
		
		playMode = argc < 3;
		penaltyDefMode = argc == 3 && args[2].equalsIgnoreCase("defendPenalty");
		penaltyAtkMode = argc == 3 && args[2].equalsIgnoreCase("shootPenalty");
		penaltyTimeOut = System.currentTimeMillis() + penaltyAllowance;
		
		while (true) {
			try {
				sleep(40);
			} catch (InterruptedException e) {
				System.err.println("Sleep interruption in Planning script");
				e.printStackTrace();
			}
			if (!vision.readable()) {
				continue;
			}
			
			updateWorldState();
			if (playMode) {
				playMode();
			}
			else {
				// penalty mode
				boolean timeUp = System.currentTimeMillis() > penaltyTimeOut;
				if (penaltyDefMode) {
					// System.out.println("********* Start Defend Penalty *********");
					penaltyDefMode();
					if (timeUp || ball.isMoving()) {
						penaltyDefMode = false;
						playMode = true;
						// System.out.println("********* End Defend Penalty *********");
					}
				}
				else {
					// System.out.println("********* Start Attack Penalty *********");
					penaltyAtkMode();
					if (timeUp || !haveBall()) {
						penaltyAtkMode = false;
						playMode = true;
						// System.out.println("********* End Attack Penalty *********");
					}
				}
			}
		}
	}
	
	static void updateWorldState() {
		// update state of world
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		// robotMath.initLoop();
	}
	
	static void playMode() {
		// System.out.println(robotMath.getPositionScore(ourRobot.getCoors(),
		// 											shootingRight, 0.5));
		// System.out.println("hitScore: "+robotMath.getHitScore(ourRobot, shootingRight));
		System.out.println(robotMath.getHitScore(ourRobot, shootingRight));
		// !!!! planning phase !!!!
		if (!haveBall()) {
			Position target = robotMath.pointBehindBall(theirGoal, ball.getCoors(), BEHIND_BALL_DIST);
			// System.out.println("  BALL X: " + ball.getCoors().getX() + " Y: " + ball.getCoors().getY());
			// System.out.println("TARGET X: " + target.getX() + " Y: " + target.getY());
			planMove(target, theirGoal);
		} 
		else {
			System.out.println("we have ball X: " + ball.getCoors().getX() + " Y: " + ball.getCoors().getY());
			if (wantToKick()) {
				plannedCommands.push(new KickCommand());
				// System.out.println("planning to kick");
			}
			else {
				planMove(theirGoal);
				// System.out.println("planning to DRIBBLE");
			}
		}
		// !!!! execution phase !!!!
		playExecute();
	}
	
	static void playExecute() {
		if (!plannedCommands.isEmpty()) {
			System.out.println("Size of cmst: "+plannedCommands.size());
			Command commandContainer = plannedCommands.pop();
			if (commandContainer instanceof KickCommand) {
				sendKickCommand(commandContainer);
				kickTimeOut = System.currentTimeMillis() + kickAllowance;
				// System.out.println("--and we kick");
			}
			else {
				MoveCommand moveCommand = (MoveCommand) commandContainer;
				if (RobotMath.euclidDist(ourRobot.getCoors(), moveCommand.moveTowardsPoint) < GOT_THERE_DIST) {
					System.out.println("-----fuck this shit");
					playExecute();
				}
				else {
					// System.out.println("--and we GOOO");
					sendMoveCommand(moveCommand);
				}
			}
		}
	}
	
	static void penaltyDefMode() {
		Position frontOfUs = RobotMath.projectPoint(ourRobot.getCoors(), ourRobot.getAngle(), 30);
		Position projectedPoint = RobotMath.projectPoint(theirRobot.getCoors(), theirRobot.getAngle(),
				 (int) RobotMath.euclidDist(ourRobot.getCoors(), theirRobot.getCoors()));
		if (ourRobot.getCoors().getY() > projectedPoint.getY()) {
			sendMoveCommand(new MoveCommand(RobotMath.projectPoint(
					ourRobot.getCoors(), Math.PI, 100), frontOfUs, false));
			// Position p = RobotMath.projectPoint(ourRobot.getCoors(), Math.PI, 100);
			// System.out.println("robot is: " + ourRobot.getCoors().getX() + " y: " + ourRobot.getCoors().getY());
			// System.out.println("going to: " + p.getX() + " y: " + p.getY());
		} else {
			sendMoveCommand(new MoveCommand(RobotMath.projectPoint(
					ourRobot.getCoors(), 0.0, 100), frontOfUs, false));
			// Position p = RobotMath.projectPoint(ourRobot.getCoors(), 0.0, 100);
			// System.out.println("robot is: " + ourRobot.getCoors().getX() + " y: " + ourRobot.getCoors().getY());
			// System.out.println("going to: " + p.getX() + " y: " + p.getY());
		}
	}
	
	static void penaltyAtkMode() {
		sendKickCommand(new KickCommand());
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
		System.out.println("Size of path: "+path.size());
	}
	
	private static void planMove(Position coors, Robot obstacle) {
		planMove(coors, obstacle, coors);
	}
	
	private static void planMove(Position coors, Position toFace) {
		planMove(coors, theirRobot, toFace);
	}
	
	private static void planMove(Position coors) {
		planMove(coors, theirRobot);
	}
	
	static boolean haveBall() {
		double distToBall = RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors());
		boolean closeToBall = distToBall < DRIBBLE_DIST;
		boolean facingBall = RobotMath.isFacing(ourRobot, ball.getCoors());
		// boolean targetingBall = RobotMath.isTargeting(ourRobot, ball.getCoors());
		
		return closeToBall && facingBall;
	}

	static boolean wantToKick() {
		double positionScore = robotMath.getPositionScore(ourRobot.getCoors(),
													shootingRight, 0.5);
		boolean kickingAllowed = System.currentTimeMillis() > kickTimeOut;
		return kickingAllowed && positionScore > 0.75;								
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

	static void sendreceive(String signal) {
		/*
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Receiving OK");
		 */
	}
	
	static void sendKickCommand(Command c) {
		sendZeros();
		sendreceive("3");
	}

	static void sendZeros() {
		sendreceive("1 0 0 0 0");
	}

}
