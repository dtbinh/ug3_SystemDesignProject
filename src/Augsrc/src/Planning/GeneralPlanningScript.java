package Planning;

import java.util.ArrayList;

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
 */
public class GeneralPlanningScript extends Thread {
	static VisionReader vision;
	static RobotMath rmaths = new RobotMath();
	static ArrayList<Position> movementDestinations = new ArrayList<Position>(); 
	static ArrayList<Position> rotationDestinations = new ArrayList<Position>();
	static ArrayList<Boolean> hardRotates = new ArrayList<Boolean>();
	private static boolean hasCommands = false;
	private static boolean visitedCurrent = false;
	public static void main(String[] args){
		vision = new VisionReader();
		do{
		//sending too many requests to the vision system is bad.
		//sleeping for 40 just about fixes the issue.
		//this still gives us ~25 commands per second, 
		//and since we can only get about 30 fps from camera,
		//it's not too bad.
		try {	
			sleep(40);
		} catch (InterruptedException e) {
			System.out.println("Sleep interruption in Planning script");
			e.printStackTrace();
		}
		if (vision.readable()) {
			doStuff();			
		}
		} while (true);
	}
	static void doStuff() {
		Robot ourRobot = vision.getOurRobot(); 
		Robot theirRobot = vision.getTheirRobot(); 
		Ball ball = vision.getBall();
		rmaths.init(); //MUST do this.
		if (!hasCommands){
			//get commands
		}
		//be sure to note this function,
		//if (euclidian distance to point) < 100
		
		rmaths.toggleWantsToStop();
		
		//this will do nothing if hardRotate is off, 
		//but will make the robot stop and rotate if it is on. 
		
		String signal = rmaths.getSigToPoint(ourRobot, movementDestinations.get(0), rotationDestinations.get(0), hardRotates.get(0));
		//something.sendSignal(signal)
		if (visitedCurrent){
			flushCommand();		
			//deletes the head of the arraylist and moves all elements down one index. 
			visitedCurrent = false;
		}
	}
	
	static void flushCommand(){
		movementDestinations.remove(0);
		rotationDestinations.remove(0);
		hardRotates.remove(0);
	}
	

}
