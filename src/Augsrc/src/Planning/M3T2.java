package Planning;
import java.awt.Point;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

import JavaVision.Position;


public class M3T2 {
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
	static PathSearch ps = new PathSearch();
	private static boolean finished = false;
	private static Robot ourGoal = new Robot();
	
	
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
				ourGoal.setCoors(shootingRight ?
						rmaths.goalR.getCoors() :
						rmaths.goalL.getCoors());
				ourGoal.setAngle(shootingRight ?
						rmaths.goalR.getAngle() :
						rmaths.goalL.getAngle());
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
		
		
		if (plannedCommands.isEmpty()) {
			if (!haveBall()){
				
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
			System.out.println("Have Command");
			}
			else {
				Ball shootPoint = new Ball();
				shootPoint.setCoors(RobotMath.projectPoint(ball.getCoors(), 
				invert((float) RobotMath.getAngleFromRobotToPoint(ourGoal, ball.getCoors())),(int) (RobotMath.euclidDist(ourGoal.getCoors(), ball.getCoors())/2)));
				ArrayList<Point> parsed = PathSearchHolly.getPath2(
						new Point (shootPoint.getCoors().getX(), shootPoint.getCoors().getY()), 
						new Point (ourRobot.getCoors().getX(),ourRobot.getCoors().getY()) , 
						(int) Math.toDegrees(ourRobot.getAngle()), 
						new Point (theirRobot.getCoors().getX(),theirRobot.getCoors().getY()),
						(int) Math.toDegrees(theirRobot.getAngle()),
						vision.getDirection());
				Position movePos = new Position(parsed.get(1).x, parsed.get(1).y);
				Position rotatePos = new Position (parsed.get(parsed.size()-1).x, parsed.get(parsed.size()-1).y) ;
				plannedCommands.pushMoveCommand(movePos, rotatePos, (RobotMath.euclidDist(movePos, ball.getCoors()) < 150) );
			}
		}
		if (haveBall() && RobotMath.euclidDist(ourRobot.getCoors(), ourGoal.getCoors())< 100){
			sendKickCommand();
			finished = true;
		}
		
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
				rmaths.toggleWantsToStop();
				sendMoveCommand(moveCommand);
				
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
		// TODO Auto-generated method stub
		return false;
	}

	static void sendZeros() {
		sendrecieve("1 0 0 0 0");
		
	}

	static boolean opponentIsCloserToOurGoal() {
		
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
	
		return (RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors()) < 30 && rmaths.isFacing(ourRobot, ball.getCoors()));
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





