package Planning;
import java.awt.Point;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

import JavaVision.Position;


public class M3T1 extends Thread {
	private static Context context;
    private static Socket socket;
	static VisionReader vision = new VisionReader("blue");
	static RobotMath rmaths = new RobotMath();
	private static boolean visitedCurrent = false;
	
	private static boolean shootingRight;
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	static CommandStack plannedCommands = new CommandStack();
	private static boolean finished = false;
	
	
	public static void main(String args[]) throws InterruptedException{
		
		rmaths.init(); // THOU SHALT NOT NOT DO THIS!
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
        socket.connect("ipc:///tmp/nxt_bluetooth_robott");
        sendZeros();
     
		while(!finished ) {
			Thread.sleep(70);
			if (vision.readable()) {
				shootingRight = vision.getDirection() ==0;
				doStuff();				
			}
			
		}sendZeros();
	}
	
	static  void doStuff() {
		// AGAIN: THOU SHALT NOT NOT DO THESE
		ourRobot = vision.getOurRobot(); 
		theirRobot = vision.getTheirRobot(); 
		ball = vision.getBall();
		rmaths.initLoop();
		
		//TODO: implement looping re-make of these commands like in the game strategy.
		//A* deosn't plan an avoidance path till pretty late for some reason
		if (plannedCommands.isEmpty()) {
			//OR USE THIS CODE.
			//plannedCommands = ObjectAvoidance.planAvoidance(ourRobot, theirRobot, 
								//0, true, ourGoal, ball,
								//plannedCommands);
			ArrayList<Point> parsed = PathSearchHolly.getPath2(
					new Point (ball.getCoors().getX(),ball.getCoors().getY()), 
					new Point (ourRobot.getCoors().getX(),ourRobot.getCoors().getY()) , 
					(int) Math.toDegrees(ourRobot.getAngle()), 
					new Point (theirRobot.getCoors().getX(),theirRobot.getCoors().getY()),
					(int) Math.toDegrees(theirRobot.getAngle()),
					vision.getDirection());
			Position movePos = new Position(parsed.get(1).x, parsed.get(1).y);
			Position rotatePos = new Position (parsed.get(parsed.size()-1).x, parsed.get(parsed.size()-1).y) ;
			plannedCommands.pushMoveCommand(movePos, rotatePos, (RobotMath.euclidDist(movePos, ball.getCoors()) < 150) );
		
		}
		
		//MoveCommand moveCommandlol = (MoveCommand) plannedCommands.getFirst();
		//System.out.println("GOODX: " +moveCommandlol.getMoveTowardspoint().getX() + " GOODY: " + moveCommandlol.getMoveTowardspoint().getY());
		
		
		Command commandContainer = plannedCommands.getFirst();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand();
			plannedCommands.pop();
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = RobotMath.euclidDist(ourRobot.getCoors(), 
					  					    moveCommand.moveTowardsPoint);
			if (RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors())
												< 100){
				if ((RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors())
						< 40 && rmaths.isFacing(ourRobot, ball.getCoors()))) {
					sendZeros();
					finished = true;
				}else {
					
				sendMoveCommand(moveCommand);
				rmaths.toggleWantsToStop();
				}
			}
			if (dist < 20.0) {
				visitedCurrent = true;				
			}
			else{
				sendMoveCommand(moveCommand);
			}
		}
		else {
			sendZeros();
		}
		// !!!! execution phase !!!!
		if (visitedCurrent) {
			visitedCurrent = false;
			plannedCommands.pop();
		}
		//cleanOutput();
	}
	
	private static boolean weRecievedSomeSensorInput() {
		//Project a point PI degrees from your current position,
		//head there,
		//loosely face the ball
		//TODO: make some sense of an uninterruptable command.
		//maybe an attribute of Command - running time (cycles)?
		return false;
	}

	static void sendZeros() {
		sendrecieve("1 0 0 0 0");
		
	}

	static boolean opponentIsCloserToOurGoal() {
		// TODO: implement
		return false;
	}
	
	static boolean opponentHasBall() {
		// TODO: implement
		return false;
	}
	
	static boolean wantToKick() {
		double positionScore = rmaths.getPositionScore(ourRobot.getCoors(),
													   shootingRight);
		return positionScore > Math.random();
	}
	
	static void sendMoveCommand(MoveCommand moveCommand) {
		String signal = rmaths.getSigToPoint(ourRobot, moveCommand.moveTowardsPoint,
				moveCommand.rotateTowardsPoint,
				moveCommand.shouldMovementEndFacingRotateTowardsPoint);
		sendrecieve(signal);
		
	}
	
	static void sendrecieve(String signal) {
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Recieving OK");
		
	}

	static void sendKickCommand() {
		// TODO: implement
	}
	
	static boolean haveBall() {
		//TODO: implement
		return false;
	}
	
	static void cleanOutput(){
		for (int i = 0; i<9; i++){
			System.out.println("");
		}
	}
	
	
	static Float invert(Float angle){
		return (float) ((float) (angle+Math.PI) % (2*Math.PI)); //reverse that stuff!!!
	}
	
}


