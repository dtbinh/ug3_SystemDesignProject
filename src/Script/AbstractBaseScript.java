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
	public final static int GOT_THERE_DIST = 10;
	final static int kickAllowance = 1500;

	static volatile VisionReader vision;

	private static volatile Context context;
	private static volatile Socket socket;

	static volatile boolean shootingRight;
	static volatile Goal theirGoal;
	static volatile Goal ourGoal;
	static volatile Robot ourRobot;
	static volatile Robot theirRobot;
	static volatile Ball ball;
	static volatile boolean started = false;
	static volatile long kickTimeOut;

	static volatile CommandStack plannedCommands;

	public AbstractBaseScript(String[] args) {
		String ourColor = args[0];
		String ourDirection = args[1];
		vision = new VisionReader(ourColor);
		shootingRight = ourDirection.equals("right");
		theirGoal = shootingRight ? Goal.goalR() : Goal.goalL();
		ourGoal = shootingRight ? Goal.goalL() : Goal.goalR();
		plannedCommands = new CommandStack();	
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		socket.connect("tcp://127.0.0.1:5555");
		kickTimeOut = System.currentTimeMillis();
		// first thing we do is send zeros to make robot stop if it
		// had previous commands on the stack
		sendZeros();
	}

	static void updateWorldState() {
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		started = vision.getStarted();
		//		ourRobot.setWantsToRotate(false);
		//		ourRobot.setWantsToStop(false);
	}

	/**
	 * Send Kick/Rotate/Move commands -- pops a command off plannedCommands
	 * <ul><li> if good command, send it </li>
	 *     <li> if bad, recurse </li> </ul>
	 */
	static void playExecute() {
		if (plannedCommands.isEmpty()) { return; }
		Command commandContainer = plannedCommands.pop();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand(commandContainer);
			kickTimeOut = System.currentTimeMillis() + kickAllowance;
		} 
		else if (commandContainer instanceof RotateCommand) {
			sendMRCommand(commandContainer);
		} 
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double distToMovePoint = ourRobot.getCoors().euclidDistTo(moveCommand.getMovePoint()); 
			if (distToMovePoint < GOT_THERE_DIST) {	playExecute(); }
			else                                  { sendMRCommand(moveCommand); }
		} 
		else {
			System.out.println("GUUUUUUUUUUYYYS. THERE WAS AN UNRECOGNIZED COMMAND.");
			playExecute();
		}
	}

	static void planKick() {
		plannedCommands.pushKickCommand();
	}

	/**
	 * Just rotate in place
	 * @param coorsToFace turn to face this Position
	 */
	static void planRotate(Position coorsToFace) {
		if (nullInput(coorsToFace)) { return; }
		plannedCommands.pushRotateCommand(ourRobot.getCoors().getAngleToPosition(coorsToFace));
	}

	/**
	 * Get the path to using A* search
	 * @param coors build path to this Position
	 * @param obstacle avoid this Robot
	 * @return
	 */
	static ArrayList<Position> getPath(Position coors, Robot obstacle) {
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
	static ArrayList<Position> getPath(Position coors) {
		return getPath(coors, theirRobot);
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * @param coors destination Position
	 */
	static void planMoveStraight(Position coors) {
		if (nullInput(coors)) { return; }
		plannedCommands.pushMoveStraightPath(getPath(coors));
	}

	/**
	 * Use A* to plan a path and push it as commands
	 * Also make robot turn while moving
	 * @param coors destination Position
	 * @param coorsToFace turn to face this Position
	 */
	static void planMoveAndTurn(Position coors, Position coorsToFace) {
		if (nullInput(coors, coorsToFace)) { return; }
		plannedCommands.pushMoveAndTurnPath(getPath(coors), coorsToFace);
	}
	/**
	 * Use A* to plan a path and push it as commands
	 * Also make robot turn while moving -- try to end up moving forwards
	 * For this we need to project a point 
	 * @param coors destination Position
	 */
	static void planMoveAndTurn(Position coors) {
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
	static void planMoveToFace(Position coors, Position coorsToFace) {
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
	static void planMoveToFace(Position coors) {
		if (nullInput(coors)) { return; }
		double angle = ourRobot.getCoors().getAngleToPosition(coors);
		Position coorsToFace = coors.projectPoint(angle, 100);
		plannedCommands.pushMoveToFacePath(getPath(coors), coorsToFace);
	}

	static void sendreceive(String signal) {
		if (started) {
			socket.send(signal, 0);
			System.out.println("Sending OK: " + signal);
			socket.recv(0);
			System.out.println("Receiving OK");
		}
	}

	static void sendZeros() {
		sendreceive("1 0 0 0 0");
	}

	static void sendKickCommand(Command c) {
		sendZeros();
		sendreceive("3");
	}

	/**
	 * Send Move/Rotate command
	 * @param command process &  send this Command
	 */
	static void sendMRCommand(Command command) {
		String signal = getMRCommandSignal(command);
		sendreceive(signal);
	}

	/**
	 * Get Move/Rotate command signal
	 * @param command process this Command
	 * @return signal String
	 */
	static String getMRCommandSignal(Command command){
		if (command instanceof MoveAndTurnCommand){
			MoveAndTurnCommand container = (MoveAndTurnCommand) command;
			Position move = container.getMovePoint();
			double direction = container.getDirection();
			return ourRobot.moveAndTurn(move, direction);
		}
		else if (command instanceof MoveToFaceCommand){
			MoveToFaceCommand container = (MoveToFaceCommand) command;
			Position move = container.getMovePoint();
			double direction = container.getDirection();
			return ourRobot.moveToFace(move, direction);
		}
		else if (command instanceof RotateCommand){
			RotateCommand container = (RotateCommand) command;
			return ourRobot.rotate(container.getDirection());
		}
		else if (command instanceof MoveStraightCommand){
			MoveStraightCommand container = (MoveStraightCommand) command;
			Position move = container.getMovePoint();
			return ourRobot.moveStraight(move);
		} 
		else {
			return null;
		}
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
}
