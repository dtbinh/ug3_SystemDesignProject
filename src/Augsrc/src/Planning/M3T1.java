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
        socket.connect("tcp://127.0.0.1:5555");
        sendZeros();
        
		while(!finished ) {
			Thread.sleep(70);
			if (vision.readable()) {
				shootingRight = vision.getDirection() ==0;
				findPath();				
			}
			
		}sendZeros();
	}
	
	static  void findPath() {
		// AGAIN: THOU SHALT NOT NOT DO THESE
		ourRobot = vision.getOurRobot(); 
		theirRobot = vision.getTheirRobot(); 
		ball = vision.getBall();
		rmaths.initLoop();
		System.out.println("Our coors X: " + ourRobot.getCoors().getX() + " y: " + ourRobot.getCoors().getY());
		System.out.println("Their coors X: " + theirRobot.getCoors().getX() + " y: " + theirRobot.getCoors().getY());
		System.out.println("Ball coors X: " + ball.getCoors().getX() + " y: " + ball.getCoors().getY());
		
		//TODO: implement looping re-make of these commands like in the game strategy.
		//A* deosn't plan an avoidance path till pretty late for some reason
		/*if (plannedCommands.isEmpty()) {
			//OR USE THIS CODE.
			//plannedCommands = ObjectAvoidance.planAvoidance(ourRobot, theirRobot, 
								//0, true, ourGoal, ball,
								//plannedCommands);
			
//			if(weRecievedSomeSensorInput()){
			if(RobotMath.euclidDist(ourRobot.getCoors(), theirRobot.getCoors()) < 70){ // TEMP statement This doesnt produce the right point TODO:
				//float reverseAngle = invert(ourRobot.getAngle());
				float reverseAngle = invert((float)(RobotMath.getAngleFromRobotToPoint(ourRobot, theirRobot.getCoors())));
				Position backOff = (RobotMath.projectPoint(ourRobot.getCoors(), reverseAngle, 70));
				System.out.println("Back off suggested point X: " + backOff.getX() + " Y: " + backOff.getY());
				plannedCommands.pushMoveCommand(backOff, ball.getCoors(), false);
			}else{
				ArrayList<Point> parsed = PathSearchHolly.getPath2(
						new Point (ball.getCoors().getX(),ball.getCoors().getY()), 
						new Point (ourRobot.getCoors().getX(),ourRobot.getCoors().getY()) , 
						(int) Math.toDegrees(ourRobot.getAngle()), 
						new Point (theirRobot.getCoors().getX(),theirRobot.getCoors().getY()),
						(int) Math.toDegrees(theirRobot.getAngle()),
						vision.getDirection());
				PathSearchHolly.printPath(parsed);
				Position movePos = new Position(parsed.get(1).x, parsed.get(1).y);
				System.out.println("Current suggested point X: " + movePos.getX() + " Y: " + movePos.getY());
				Position rotatePos = new Position (parsed.get(parsed.size()-1).x, parsed.get(parsed.size()-1).y) ;
				plannedCommands.pushMoveCommand(movePos, rotatePos, (RobotMath.euclidDist(movePos, ball.getCoors()) < 150));	
			}
			
		}*/
		ArrayList<Point> parsed = PathSearchHolly.getPath2(
				new Point (ball.getCoors().getX(),ball.getCoors().getY()), 
				new Point (ourRobot.getCoors().getX(),ourRobot.getCoors().getY()) , 
				(int) Math.toDegrees(ourRobot.getAngle()), 
				new Point (theirRobot.getCoors().getX(),theirRobot.getCoors().getY()),
				(int) Math.toDegrees(theirRobot.getAngle()),
				1-vision.getDirection());
		PathSearchHolly.printPath(parsed);
		Position movePos = new Position(parsed.get(1).x, parsed.get(1).y);
		System.out.println("Current suggested point X: " + movePos.getX() + " Y: " + movePos.getY());
		Position rotatePos = new Position (parsed.get(parsed.size()-1).x, parsed.get(parsed.size()-1).y) ;
		plannedCommands.pushMoveCommand(movePos, rotatePos, (RobotMath.euclidDist(movePos, ball.getCoors()) < 150));	
		
		System.out.println("Size of command stack " + plannedCommands.size());
		Command commandContainer = plannedCommands.getFirst();
		if (commandContainer instanceof KickCommand) {
			sendKickCommand(plannedCommands.pop());
			//plannedCommands.pop(); Dont think this is needed now?
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = RobotMath.euclidDist(ourRobot.getCoors(), 
					  					    moveCommand.moveTowardsPoint);
			if (RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors())
												< 100){
				System.out.println("We are within 100 of ball");
				if ((RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors())
						< 40 && rmaths.isFacing(ourRobot, ball.getCoors()))) {
					sendZeros();
					finished = true;
					System.out.println("We are within 40 of ball and facing the ball. FINISHED!");
				}else {
					System.out.println("We are between 100-0 of ball but not facing");
					sendMoveCommand(moveCommand);
					rmaths.toggleWantsToStop();
				}
			}
			if (dist < 20.0) {
				System.out.println("We have visited current Point");
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
		cleanOutput();
	}
	
	private static boolean weReceivedSomeSensorInput() {
		//Project a point PI degrees from your current position,
		//head there,
		//loosely face the ball
		//TODO: make some sense of an uninterruptable command.
		//maybe an attribute of Command - running time (cycles)?
		
		//Once Sensor signal received **find out from Andrew where the signal is**

		return false;
	}

	static void sendZeros() {
		sendreceive("1 0 0 0 0");
		
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
		sendreceive(signal);
		
	}
	
	static void sendreceive(String signal) {
		
		socket.send(signal, 0);
        System.out.println("Sending OK");
        socket.recv(0);
        System.out.println("Recieving OK");
		
	}

	static void sendKickCommand(Command c) {
		sendreceive("3");
	}
	
	static void cleanOutput(){
			System.out.println("**");
			System.out.println("**");
	}
	
	
	static Float invert(Float angle){
		return (float) ((float) (angle+Math.PI) % (2*Math.PI)); //reverse that stuff!!!
	}
	
}


