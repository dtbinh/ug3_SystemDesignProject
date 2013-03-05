package Planning;
import java.awt.Point;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

import JavaVision.Position;


public class M3T2 extends Thread {
	private static Context context;
    private static Socket socket;
	static VisionReader vision = new VisionReader("yellow");
	static RobotMath rmaths = new RobotMath();
	private static boolean visitedCurrent = false;
	
	private static boolean shootingRight;
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	static CommandStack plannedCommands = new CommandStack();
	private static boolean finished = false;
	private static boolean needsRecovery = false;
	private static Position recoveryPosition;
	private static Robot theirGoal = new Robot();
	

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
				theirGoal.setCoors(shootingRight ?
						rmaths.goalR.getCoors() :
						rmaths.goalL.getCoors());
				theirGoal.setAngle(0);
				doStuff();				
			}
			
		}sendZeros();
	}
	
	static  void doStuff() {
		// AGAIN: THOU SHALT NOT NOT DO THESE
		ourRobot = vision.getOurRobot(); 
		ball = vision.getBall();
		rmaths.initLoop();
		
		System.out.println("Our coors X: " + ourRobot.getCoors().getX() + " y: " + ourRobot.getCoors().getY());
		System.out.println("Ball coors X: " + ball.getCoors().getX() + " y: " + ball.getCoors().getY());
		
		// START PLANNING
		Position destination = getDestination();
		Robot obstacle = getObstacle();
		ArrayList<Point> parsed = PathSearchHolly.getPath2(
			new Point (destination.getX(), destination.getY()), 
			new Point (ourRobot.getCoors().getX(),ourRobot.getCoors().getY()) , 
			(int) Math.toDegrees(ourRobot.getAngle()), 
			new Point (obstacle.getCoors().getX(),obstacle.getCoors().getY()),
			(int) Math.toDegrees(obstacle.getAngle()),
			1-vision.getDirection());
		Position movePos = new Position(parsed.get(1).x, parsed.get(1).y);
		Position rotatePos = getRotatePos(parsed.get(parsed.size()-1));

		plannedCommands.pushMoveCommand(movePos, rotatePos, 
				((RobotMath.euclidDist(movePos, ball.getCoors()) < 125)&&(!haveBall())));	
		// END PLANNING
		
		PathSearchHolly.printPath(parsed);
		System.out.println("Current suggested point X: " + movePos.getX() + " Y: " + movePos.getY());
		System.out.println("Size of command stack " + plannedCommands.size());
		
		// START EXECUTION
		Command commandContainer = plannedCommands.getFirst();
		if (haveBall() && closeToGoal() && rmaths.isTargeting(ourRobot, theirGoal.getCoors())) {
			sendZeros();
			sendKickCommand(new KickCommand());
//			finished = true;
		}
		else if (commandContainer instanceof MoveCommand) {
			MoveCommand moveCommand = (MoveCommand) commandContainer;
			double dist = RobotMath.euclidDist(ourRobot.getCoors(), 
					  					    moveCommand.moveTowardsPoint);
			if (dist < 20.0) {
				System.out.println("We have visited current Point");
				visitedCurrent = true;				
			} else {
				sendMoveCommand(moveCommand);
			}
		}
		else {
			sendZeros();
		}
		if (visitedCurrent) {
			visitedCurrent = false;
			plannedCommands.pop();
		}
		// END EXECUTION
				
		cleanOutput();
	}
	
	private static Position getDestination() {
		Position destination;
		if (needsRecovery && RobotMath.euclidDist(ourRobot.getCoors(), recoveryPosition) > 20) {
			// go behind ball if need to get there and didn't get there yet
			destination = recoveryPosition; 
			System.out.println("...... in recovery");
		} else {
			needsRecovery = false;
			if (!haveBall()) {
//				destination = ball.getCoors();
				destination = RobotMath.projectPoint(ball.getCoors(), 
						RobotMath.getAngleFromRobotToPoint(theirGoal, ball.getCoors()), 60);
			} else { 
				// dribble to a point in front of the ball if have ball
				destination = RobotMath.projectPoint(ball.getCoors(), 
					invert(RobotMath.getAngleFromRobotToPoint(theirGoal, ball.getCoors())), 
					(int)(RobotMath.euclidDist(theirGoal.getCoors(), ball.getCoors()))/2);
				if (closeToWall()) {
					// also move a little towards the centre
					destination.setY((int)(destination.getY()*0.7) + 72);
				}
			}
		} 
		if (RobotMath.euclidDist(theirGoal.getCoors(), destination) -
			RobotMath.euclidDist(theirGoal.getCoors(), ourRobot.getCoors()) > 50) {
			// if destination-goal distance larger than robot-goal distance
			// enable recovery mode to a position behind ball
			recoveryPosition = RobotMath.projectPoint(ball.getCoors(), 
				RobotMath.getAngleFromRobotToPoint(theirGoal, ball.getCoors()),	100);
			destination = recoveryPosition;
			needsRecovery = true;
		}
		return destination;
	}
	
	private static Robot getObstacle() {
		Robot obstacle = new Robot();
		if (needsRecovery) {
			// avoid ball if we are trying to get behind it
			obstacle.setCoors(ball.getCoors());
			obstacle.setAngle(theirGoal.getAngle());
		} else {
			// create dummy obstacle out of pitch
			obstacle.setCoors(new Position(-500, 240));
			obstacle.setAngle(theirGoal.getAngle());
		}
		return obstacle;
	}
	
	private static Position getRotatePos(Point targetPoint) {
		if (needsRecovery) {
			return ball.getCoors();
		} else if (haveBall() && !closeToWall()) {
			return theirGoal.getCoors();
		} else {
			return new Position(targetPoint); 
		}
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
        System.out.println("Receiving OK");
		
	}

	static void sendKickCommand(Command c) {
		System.out.println("THIS IS SPARTAAAAAAAAAAAAAAa");
		sendreceive("3");
	}
		
	static boolean haveBall() {
		
		return (RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors()) < 30 && rmaths.isFacing(ourRobot, ball.getCoors()));
	}
	
	static boolean closeToGoal() {
		return (RobotMath.euclidDist(ourRobot.getCoors(), theirGoal.getCoors()) < 140);
	}
	
	static boolean closeToWall() {
		int x = ourRobot.getCoors().getX();
		int y = ourRobot.getCoors().getY();
		if (x < 200 || x > 450) {
			return (y < 105 || y > 365); 
		} else {
			return (y < 95 || y > 370);
		}
	}
	
	static void cleanOutput(){
			System.out.println("**");
			System.out.println("**");
	}
	
	static double invert(double angle){
		return (angle+Math.PI) % (2*Math.PI); //reverse that stuff!!!
	}

}


