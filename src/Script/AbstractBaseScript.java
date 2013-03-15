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
	static volatile RobotMath robotMath;

	private static volatile Context context;
    private static volatile Socket socket;

	static volatile boolean shootingRight;
	static volatile Goal theirGoal;
	static volatile Goal ourGoal;
	static volatile Robot ourRobot;
	static volatile Robot theirRobot;
	static volatile Ball ball;
	static volatile long kickTimeOut;

	static volatile CommandStack plannedCommands;

	public AbstractBaseScript(String[] args) {
		String ourColor = args[0];
		String ourDirection = args[1];
		vision = new VisionReader(ourColor);
		robotMath = new RobotMath();
		shootingRight = ourDirection.equals("right");
		theirGoal = shootingRight ? Goal.goalR() : Goal.goalL();
		ourGoal = shootingRight ? Goal.goalL() : Goal.goalR();
		plannedCommands = new CommandStack();	
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://127.0.0.1:5555");
        // first thing we do is send zeros to make robot stop if it
        // had previous commands on the stack
        sendZeros();
	}

	static void updateWorldState() {
		ourRobot = vision.getOurRobot();
		theirRobot = vision.getTheirRobot();
		ball = vision.getBall();
		robotMath.initLoop();
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
				double distToMovePoint = ourRobot.getCoors().euclidDistTo(moveCommand.moveTowardsPoint); 
				if (distToMovePoint < GOT_THERE_DIST) {
					playExecute();
				}
				else {
					sendMoveCommand(moveCommand);
				}
			} else {
				playExecute();
			}
		}
	}

	static void planMove(Position coors, Robot obstacle, Position coorsToFace) {
		if (coors == null) {
			return;
		}
		ArrayList<Point> path = AStar.getPath2(
				new Point(coors.getX(), coors.getY()), 
				new Point(ourRobot.getCoors().getX(), ourRobot.getCoors().getY()), 
				(int) Math.toDegrees(ourRobot.getAngle()), 
				new Point(obstacle.getCoors().getX(), obstacle.getCoors().getY()), 
				(int) Math.toDegrees(obstacle.getAngle()), 
				shootingRight ? AStar.LEFT : AStar.RIGHT);
		        // if we're shootingRight, *our* side is LEFT
		plannedCommands.pushMoveCommand(path, coorsToFace);
	}

	static void planMove(Position coors, Robot obstacle) {
		planMove(coors, obstacle, coors);
	}

	static void planMove(Position coors, Position coorsToFace) {
		planMove(coors, theirRobot, coorsToFace);
	}

	static void planMove(Position coors) {
		planMove(coors, theirRobot);
	}

	static void sendreceive(String signal) {
		/*
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Receiving OK");
		*/
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
	
	public static VisionReader getVision() {
		return vision;
	}
}
