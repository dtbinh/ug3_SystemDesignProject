package Planning;

// TODO: remove ArrayList legacy
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
public class GeneralPlanningScript extends Thread {
	public static double HAS_BALL_DISTANCE_THRESHOLD = 60.0;
	public static double END_MOVE_START_ROTATE_DISTANCE_THRESHOLD = 100.0;
	
	static VisionReader vision;
	static RobotMath rmaths;
	
	private static Context context;
    private static Socket socket;
	
	static boolean shootingRight;
	static Position ourGoal;
	static Position theirGoal;
	static Position optimumGP;
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	static int runs = 0;
	static int kickWait = 0;
	
	static CommandStack plannedCommands;
	static boolean skipNextPlanningPhase;
	private static Position safePoint;
	static float theirGoalAngle;
	static float ourGoalAngle;
	
	static boolean playMode;
	static boolean penaltyDefMode;
	static boolean penaltyAtkMode;
	static double penaltyAngle;
	
	static int timeToTakePenalty = 20000;
	static long matchStartTime;
	
	public static void main(String[] args) {
		int argc = args.length;
		vision = new VisionReader(args[0]); //Our Colour - MAKE GUI 4 DIS
		rmaths = new RobotMath(); rmaths.init();
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://127.0.0.1:5555");
        sendZeros();
        System.out.println(args[1].toString());
		shootingRight = args[1].equals("right");
		System.out.println(shootingRight);
		
		if (!shootingRight){
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
		optimumGP = theirGoal;
		safePoint = RobotMath.projectPoint(ourGoal, ourGoalAngle, 70 );
		
		playMode = argc < 3;
		penaltyDefMode = argc == 3 && args[2].equalsIgnoreCase("defendPenalty");
		penaltyAtkMode = argc == 3 && args[2].equalsIgnoreCase("shootPenalty");
		penaltyAngle = Math.random() > 0.5 ? 0 : Math.PI;

		matchStartTime = System.currentTimeMillis();
		
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
				runs++;
			}
			else if (penaltyDefMode) {
				System.out.println("********* Start Defend Penalty *********");
				penaltyDefMode();
				boolean timeUp = matchStartTime + timeToTakePenalty > System.currentTimeMillis();
				boolean theyShot = RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors()) < 40;
				if (timeUp || theyShot) {
					penaltyDefMode = false;
					playMode = true;
					System.out.println("********* End Defend Penalty *********");
				}
			}
			else if (penaltyAtkMode) {
				System.out.println("********* Start Attack Penalty *********");
				penaltyAtkMode();
				boolean timeUp = matchStartTime + timeToTakePenalty > System.currentTimeMillis();
				boolean weShot = !haveBall();
				if (timeUp || weShot) {
					penaltyAtkMode = false;
					playMode = true;
					System.out.println("********* End Attack Penalty *********");
				}
			}
		}
	}
	
	static void updateWorldState() {
		// update state of world
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		rmaths.initLoop();
	}
	
	static void playMode() {
		if (plannedCommands.isEmpty()) {
			getDefaultCommands();
		}
		
		// !!!! planning phase !!!!
		if (!skipNextPlanningPhase) {
			if (END_MOVE_START_ROTATE_DISTANCE_THRESHOLD > 
					RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors()) 
					&& (!(rmaths.isFacing(ourRobot, theirGoal)))) {
				//shimmy();
				Robot goalRobot = new Robot();
				goalRobot.setAngle(theirGoalAngle);
				goalRobot.setCoors(theirGoal);
				//move(rmaths.pointBehindBall(goalRobot, ball.getCoors()), ball.getCoors(),0);
				//System.out.println("SHIMMEHYEHE");
				
				
			} else if (haveBall()) {
				System.out.println("We have ball");
				// opportunistic strategy:
				// if the current position has a good enough probability to
				// score a goal then drop everything the robot is doing and
				// perform a kick as the next action (and then resume with
				// whatever you were doing)
				if (wantToKick() && (kickWait<runs) ) {
					if (opponentIsInWay()) {
						System.out.println("We want to kick but opponent in way");
							//move to the optimum position
							move(optimumGP);
					} else {
						System.out.println("We want to kick and have clear shot");
						plannedCommands.pushKickCommand(ourRobot.getCoors(),
												theirGoal);
						skipNextPlanningPhase = true;
					}	
				} else {
					move(optimumGP);
					//plannedCommands.pushMoveCommand(ourRobot.getCoors(), optimumGP, true);
				}
			} else {
				if (opponentHasBall()) {
					System.out.println("They have ball");
					plannedCommands.clear();
					if (opponentIsCloserToOurGoal()) {
						System.out.println("They are closer to our goal than us");
						//Run to da goal.
						move(safePoint, ball.getCoors, false);
					}
					else {
						if (weAreBlocking()){
							// TODO: mirror opponent movement
							// TODO: move into intersection of opponent direction and centre of goal
							move(theirRobot.getCoors());
							System.out.println("CHAAARGE!");
						} else {
							System.out.println("RUUUUUUUN!");
							move(safePoint);
						}
					}
				}
				else {
					System.out.println("Ball is in open play going to ball");
					move(ball.getCoors());
//					Position futureCoors = ball.getReachableCoors(ourRobot.getCoors(), 
//					                                              2);
//					if (futureCoors != null) { move(futureCoors); }
//					else                     { move(ball.getCoors()); }
				}
			}
		}
		
		// !!!! execution phase !!!!
		Command commandContainer = plannedCommands.getFirst();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand(plannedCommands.pop());
			skipNextPlanningPhase = false;
			kickWait = runs+25;
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = RobotMath.euclidDist(ourRobot.getCoors(), 
					  					       moveCommand.moveTowardsPoint);
			if (dist < END_MOVE_START_ROTATE_DISTANCE_THRESHOLD) {
				rmaths.toggleWantsToStop();
				sendMoveCommand(moveCommand);
				if (dist < HAS_BALL_DISTANCE_THRESHOLD && 
						(!moveCommand.getHardRotate() || 
						 rmaths.isFacing(ourRobot, moveCommand.getRotateTowardsPoint()))) {
					plannedCommands.pop();
				}
			}
			else {
				sendMoveCommand(moveCommand);
			}
		}
	}
	
	private static void move(Position coors, Position obsCoors, float obsAngle) {
		ArrayList<Point> path = PathSearchHolly.getPath2(
				new Point(coors.getX(), coors.getY()), 
				new Point(ourRobot.getCoors().getX(), ourRobot.getCoors().getY()), 
				(int) Math.toDegrees(ourRobot.getAngle()), 
				new Point(obsCoors.getX(), obsCoors.getY()), 
				(int) Math.toDegrees(obsAngle), 
				shootingRight ? PathSearchHolly.LEFT : PathSearchHolly.RIGHT);
		        // if we're shootingRight, *our* side is LEFT
		plannedCommands.pushMoveCommand(path);
	}
	private static void move(Position coors) {
		move(coors, theirRobot.getCoors(), theirRobot.getAngle());
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
												 theirGoal);
	}
	
	static boolean weAreBlocking() {
		return ObjectAvoidance.obstacleDetection(theirRobot.getCoors(),
												 ourRobot.getCoors(),
												 ourGoal);
	}
	
	static boolean wantToKick() {
		double positionScore = rmaths.getPositionScore(ourRobot.getCoors(),
													!shootingRight, 0.1);
		System.out.println(positionScore);
		return positionScore > Math.pow(Math.random(), 0.5);
	//Makes kicking much more unlikely - was generating 
	//~ 5 kicks a second before at terrible positions.
											
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
		sendZeros();
		sendreceive("3");
	}

	static void sendreceive(String signal) {
		///*
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Receiving OK");
		// */
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
	static void sendZeros() {
		sendreceive("1 0 0 0 0");
	}
	
	private static void shimmy() {
		double step = -(Math.PI/6);
		Robot goalRobot = new Robot();
		goalRobot.setAngle(0);
		goalRobot.setCoors(theirGoal);
		Robot ballRobot = new Robot();
		ballRobot.setCoors(ball.getCoors());
		ballRobot.setAngle(0);
		double angleBehindBall = RobotMath.getAngleFromRobotToPoint(goalRobot, ball.getCoors());
		double relativeRobot = RobotMath.getAngleFromRobotToPoint(ballRobot, ourRobot.getCoors());
		double angleBetween = relativeRobot-angleBehindBall;
		angleBetween = (angleBetween + RobotMath.TENPI) % RobotMath.TWOPI;
		double currentAngle = relativeRobot;
		boolean hitWall = false;
		if (currentAngle < Math.PI) {
			step = -step;
		}
		while (true) {
			currentAngle += step;
			currentAngle = (currentAngle + RobotMath.TENPI) % RobotMath.TWOPI;
			if (Math.abs(currentAngle - angleBehindBall) < Math.abs(step)){
				break;
			}
			if (!RobotMath.withinPitch(RobotMath.projectPoint(ball.getCoors(), currentAngle, 70))) {
				hitWall = true;
				System.out.println("I had a wee accident");
				break;
			}
		}
		if (hitWall) {
			step *= -1;
		}
		int dist = (int) RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors());
		double offset = ((relativeRobot+step) + RobotMath.TENPI) % RobotMath.TWOPI;
		Position moveToPoint = RobotMath.projectPoint(ball.getCoors(),offset,  dist);
		System.out.println(step);
		plannedCommands.pushMoveCommand(moveToPoint, ball.getCoors(), true);
	}
	
	static void penaltyDefMode() {
		if (ourRobot.getCoors().getY() > ball.getCoors().getY()) {
			sendMoveCommand(new MoveCommand(RobotMath.projectPoint(
					ourRobot.getCoors(), 0.0, 100),
					ball.getCoors(), false));
		} else {
			sendMoveCommand(new MoveCommand(RobotMath.projectPoint
					(ourRobot.getCoors(), Math.PI, 100),
					ball.getCoors(), false));
		}
	}
	
	static void penaltyAtkMode() {
		Position target = RobotMath.projectPoint(
			theirGoal,
			penaltyAngle,
			30);
		boolean facingTarget = rmaths.isFacing(ourRobot, target);
		if (!facingTarget) {
			sendMoveCommand(new MoveCommand(ourRobot.getCoors(), target, true));
		}
		else {
			sendKickCommand(new KickCommand());
		}
	}
}
