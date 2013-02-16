package Planning;
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
		System.out.println(ball.getCoors().getX());
		//TODO: Write a class to send commands to the robot.
	}
	

}
