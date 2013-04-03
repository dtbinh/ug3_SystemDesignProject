package Script;

import java.awt.Point;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import JavaVision.VisionReader;
import PathSearch.AStar;
import Command.*;
import PitchObject.*;

public abstract class AbstractBaseScript extends Thread {
	public final static int GOT_THERE_DIST = 20;
    public final static int DRIBBLE_DIST = 60;
    public final static int BEHIND_BALL_DIST = 50;
	public final static int KICK_ALLOWANCE = 1500;
    

	static volatile RobotMode robotMode;

	static volatile VisionReader vision;

	static volatile Context context;
	static volatile Socket socket;

	static volatile boolean shootingRight;
	static volatile Goal theirGoal;
	static volatile Goal ourGoal;
	static volatile Robot ourRobot;
	static volatile Robot theirRobot;
	static volatile Ball ball;

	static volatile long kickTimeOut = System.currentTimeMillis();

    static volatile boolean started = false;

	static volatile CommandStack plannedCommands;

	volatile int _argsParsed = 0; // amount of `String[] args` we have used

	public AbstractBaseScript(String[] args) {
		String ourColor = args[0]; _argsParsed++;
		String ourDirection = args[1]; _argsParsed++;

		shootingRight = ourDirection.equals("right");
		theirGoal = shootingRight ? Goal.goalR() : Goal.goalL();
		ourGoal = shootingRight ? Goal.goalL() : Goal.goalR();

		vision = new VisionReader(ourColor);
		plannedCommands = new CommandStack();

		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		socket.connect("tcp://127.0.0.1:5555");

		// first thing we do is send zeros to make robot stop if it
		// had previous commands on the stack
		sendZeros();
	}

	public static void updateWorldState() {
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		started = vision.getStarted();
		robotMode = vision.getRobotMode();
	}

	/**
	 * Send Kick/Rotate/Move commands -- pops a command off plannedCommands
	 * <ul><li> if good command, send it </li>
	 *     <li> if bad, recurse </li> </ul>
	 */
	public static void playExecute() {
		if (plannedCommands.isEmpty()) { return; }
		Command commandContainer = plannedCommands.pop();
		if (commandContainer instanceof KickCommand) {
			sendCommand(commandContainer);
			kickTimeOut = System.currentTimeMillis() + KICK_ALLOWANCE;
		}
		else if (commandContainer instanceof RotateCommand) {
			sendCommand(commandContainer);
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double distToMovePoint = ourRobot.getCoors().euclidDistTo(moveCommand.getMovePoint());
			if (distToMovePoint < GOT_THERE_DIST) {	playExecute(); }
			else                                  { sendCommand(moveCommand); }
		}
		else {
			System.out.println("GUUUUUUUUUUYYYS. THERE WAS AN UNRECOGNIZED COMMAND.");
			playExecute();
		}
	}

	public static void planKick() {
		plannedCommands.pushKickCommand();
	}

	/**
	 * Just rotate in place
	 * @param coorsToFace turn to face this Position
	 */
	public static void planRotate(Position coorsToFace) {
		if (nullInput(coorsToFace)) { return; }
		plannedCommands.pushRotateCommand(ourRobot.getCoors().getAngleToPosition(coorsToFace));
	}

	/**
	 * Get the path to using A* search
	 * @param coors build path to this Position
	 * @param obstacle avoid this Robot
	 * @return
	 */
	public static ArrayList<Position> getPath(Position coors, Robot obstacle) {
		return AStar.getPath2(
				new Point(coors.getX(), coors.getY()),
				new Point(ourRobot.getCoors().getX(), ourRobot.getCoors().getY()),
				(int) Math.toDegrees(ourRobot.getAngle()),
				new Point(obstacle.getCoors().getX(), obstacle.getCoors().getY()),
				(int) Math.toDegrees(obstacle.getAngle()),
				shootingRight ? AStar.LEFT : AStar.RIGHT);
		// if we're shootingRight, *our* side is LEFT
	}

	/**
	 * Get the path to using A* search
	 * The obstacle is assumed to be theirRobot by default
	 * @param coors build path to this Position
	 * @return
	 */
	public static ArrayList<Position> getPath(Position coors) {
		return getPath(coors, theirRobot);
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * @param coors destination Position
	 */
	public static void planMoveStraight(Position coors) {
		if (nullInput(coors)) { return; }
		plannedCommands.pushMoveStraightPath(getPath(coors));
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * Also make robot turn while moving
	 * @param coors destination Position
	 * @param coorsToFace turn to face this Position
	 */
	public static void planMoveAndTurn(Position coors, Position coorsToFace) {
		if (nullInput(coors, coorsToFace)) { return; }
		plannedCommands.pushMoveAndTurnPath(getPath(coors), coorsToFace);
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * Also make robot turn while moving -- try to end up moving forwards
	 * For this we need to project a point
	 * @param coors destination Position
	 */
	public static void planMoveAndTurn(Position coors) {
		if (nullInput(coors)) { return; }
		double angle = ourRobot.getCoors().getAngleToPosition(coors);
		Position coorsToFace = coors.projectPoint(angle, 100);
		plannedCommands.pushMoveAndTurnPath(getPath(coors), coorsToFace);
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * Also make robot turn while moving
	 * Near the end, HARD rotate to face that Position
	 * @param coors destination Position
	 * @param coorsToFace turn to face this Position
	 */
	public static void planMoveToFace(Position coors, Position coorsToFace) {
		if (nullInput(coors, coorsToFace)) { return; }
		plannedCommands.pushMoveToFacePath(getPath(coors), coorsToFace);
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * Also make robot turn while moving -- try to end up moving forwards
	 * For this we need to project a point
	 * Near the end, HARD rotate to face that Position
	 * @param coors destination Position
	 */
	public static void planMoveToFace(Position coors) {
		if (nullInput(coors)) { return; }
		double angle = ourRobot.getCoors().getAngleToPosition(coors);
		Position coorsToFace = coors.projectPoint(angle, 100);
		plannedCommands.pushMoveToFacePath(getPath(coors), coorsToFace);
	}

	public static void sendreceive(String signal) {
		if (started) {
			socket.send(signal, 0);
	        System.out.println("Sending OK: " + signal);
	        socket.recv(0);
	        System.out.println("Receiving OK");
		} else {
			socket.send("1 0 0 0 0", 0);
			socket.recv(0);
		}
	}

	public static void sendZeros() {
		sendreceive("1 0 0 0 0");
	}

	/**
	 * Send proper command
	 * @param command
	 */
	public static void sendCommand(Command command) {
		if (command instanceof KickCommand) {
			sendreceive("3");
			System.out.println("SENT KICK COMMAND!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("SENT KICK COMMAND!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("SENT KICK COMMAND!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("SENT KICK COMMAND!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("SENT KICK COMMAND!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		else if (command instanceof MoveAndTurnCommand){
			MoveAndTurnCommand container = (MoveAndTurnCommand) command;
			Position move = container.getMovePoint();
			double direction = container.getDirection();
			sendreceive(ourRobot.moveAndTurn(move, direction));
		}
		else if (command instanceof MoveToFaceCommand){
			MoveToFaceCommand container = (MoveToFaceCommand) command;
			Position move = container.getMovePoint();
			double direction = container.getDirection();
			sendreceive(ourRobot.moveToFace(move, direction));
		}
		else if (command instanceof RotateCommand){
			RotateCommand container = (RotateCommand) command;
			sendreceive(ourRobot.rotate(container.getDirection()));
		}
		else if (command instanceof MoveStraightCommand){
			MoveStraightCommand container = (MoveStraightCommand) command;
			Position move = container.getMovePoint();
			sendreceive(ourRobot.moveStraight(move));
		}
		else {
			sendZeros();
		}
	}

	/**
	 * Just if we really want to kick *now*.
	 * I would advise using planKick() and then execute ...
	 */
	static void sendKickCommand() {
		sendCommand(new KickCommand());
	}

	public static boolean nullInput(Object o) {
		return (o==null) || (ourRobot.getCoors()==null);
	}

	public static boolean nullInput(Object o, Object p) {
		return (o==null) || (p==null) || (ourRobot.getCoors()==null);
	}

	public static VisionReader getVision() {
		return vision;
	}

	public static boolean openPlay() {
		return Math.abs(ball.getCoors().getX() - ourGoal.getCoors().getX()) < 150;
	}

	public static Position getRetreatPoint(boolean shootingRight) {
		// close to goal (on x axis)
		int x =  ourGoal.getOptimalPosition().getX() + (shootingRight ? +40 : -40);
		// close to where the ball will be (on y axis)
		// int y = ball.getCoors().getY();
		int y = ball.getCoors().getY();
		return new Position (x, y);
	}

	public static boolean wantToKick() {
		double positionScore = ourRobot.getPositionScore(shootingRight);
		double hitScore = ourRobot.getHitScore(shootingRight);
		boolean kickingAllowed = System.currentTimeMillis() > kickTimeOut;
		return kickingAllowed && (positionScore > 0.0) && (hitScore > 0.4);
	}

	public static boolean penaltyTimeUp() {
		return vision.penaltyTimeUp();
	}

	public static void endPenalty() {
		if (started) vision.setRobotMode(RobotMode.PLAY);
	}

	static boolean timeOut(long startTime, int allowance) {
		return startTime + allowance < System.currentTimeMillis();
	}
}
