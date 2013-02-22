package Planning;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

public class M3T1 {
	private static Context context;
    private static Socket socket;
	static VisionReader vision;
	static RobotMath rmaths = new RobotMath();
	private static boolean hasCommands = false;
	private static boolean visitedCurrent = false;
	
	private static boolean shootingRight = vision.getDirection() == 0;
	private static Robot ourGoal; 
	static Robot ourRobot;
	static Robot theirRobot;
	static Ball ball;
	
	static CommandStack plannedCommands = new CommandStack()
	;
	
	public static void main(String[] args){
		vision = new VisionReader();
		rmaths.init(); // THOU SHALT NOT NOT DO THIS!
		socket = context.socket(ZMQ.REQ);
        socket.connect("ipc:///tmp/nxt_bluetooth_robott");
        context = ZMQ.context(1);
		if (shootingRight) {
				ourGoal.setCoors(RobotMath.goalR.getCoors()) ;
				ourGoal.setAngle(0) ;
		} else {
				RobotMath.goalL.getCoors();
				ourGoal.setAngle((float) Math.PI);
				}
		while(true) {
			try {	
				Thread.sleep(40);
			} catch (InterruptedException e) {
				System.out.println("Sleep interruption in Planning script");
				e.printStackTrace();
			}
			if (vision.readable()) {
				doStuff();
			}
		}
	}
	
	static void doStuff() {
		// AGAIN: THOU SHALT NOT NOT DO THESE
		ourRobot = vision.getOurRobot(); 
		theirRobot = vision.getTheirRobot(); 
		ball = vision.getBall();
		rmaths.initLoop();
		
		if (!hasCommands) {
			ObjectAvoidance.planAvoidance(ourRobot, theirRobot, 0, true, ourGoal, ball);
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
			}
			if (dist < 20.0) {
				visitedCurrent = true;				
			}
			else {
				sendMoveCommand(moveCommand);
			}
		}
		
		// !!!! execution phase !!!!
		if (visitedCurrent) {
			visitedCurrent = false;
			plannedCommands.pop();
		}
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
	
}


