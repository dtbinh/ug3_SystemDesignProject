package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

public class Runner extends Thread {
	public static final double TWOPI = Math.PI * 2;
	public static final double TENPI = Math.PI * 10;
	// Objects
	public static Ball ball;
	static WorldState state;
	static Move move;
	static Runner instance = null;
	static Robot blueRobot;
	static Robot yellowRobot;
	private static ControlGUI thresholdsGUI;
	Vision vision;
	static double ballangle = 0;
	private static Context context;
	private static Socket socket;
	boolean wantsToRotate = false;
	boolean wantsToStop = false;
	static String colour;
	static Robot ourRobot;
	static Robot goalL;
	static Robot goalR;
	
	public static void main(String args[]) {
		context = ZMQ.context(1);

		//  Socket to talk to clients over IPC
		socket = context.socket(ZMQ.REQ);
		socket.connect("ipc:///tmp/nxt_bluetooth_robott");
		//colour = args[0];
		
		instance = new Runner();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public Runner() {
		
		blueRobot = new Robot();
		yellowRobot = new Robot();
		goalL = new Robot();
		goalR = new Robot();
		ball = new Ball();
		move = new Move();
		
		//Defining goals as robots
		goalL.setAngle((float)Math.PI);
		goalL.setCoors(new Position(35,240));
		goalR.setAngle(0);
		goalR.setCoors(new Position(603,240));
		start();
	}

	/**
	 * Planning thread which begins planning loop - bluetooth server start will also go here later
	 */
	public void run() {		
		startVision();
		
		do {
		try {
			sleep(40);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mainLoop();
		}while(true);
	}

	/**
	 * Method to initiate the vision
	 */
	private void startVision() {	    
		/**
		 * Creates the control
		 * GUI, and initialises the image processing.
		 * 
		 * @param args        Program arguments. Not used.
		 */
		WorldState worldState = new WorldState();
		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */
		PitchConstants pitchConstants = new PitchConstants(0);

		/* Default values for the main vision window. */
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 100; //dropped compression of the camera slightly - feel free to experiment further

		try {
			/* Create a new Vision object to serve the main vision window. */
			vision = new Vision(videoDevice, width, height, channel, videoStandard,
					compressionQuality, worldState, thresholdsState, pitchConstants);

			/* Create the Control GUI for threshold setting/etc. */
			thresholdsGUI = new ControlGUI(thresholdsState, worldState, pitchConstants);
			thresholdsGUI.initGUI();

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void mainLoop() {

		//Defining goals as robots
		//goalL.setAngle((float)Math.PI);
		//goalL.setCoors(new Position(35,240));
		//goalR.setAngle(0);
		//goalR.setCoors(new Position(603,240));
				
		getPitchInfo();
		System.out.println("New point behind ball X: " + pointBehindBall(goalL,ball.getCoors()).getX() + "Y: " + pointBehindBall(goalR,ball.getCoors()).getY());
		System.out.println("Actual ball coordinates X: " + ball.getCoors().getX() + " Y: " + ball.getCoors().getY());
		
		
		wantsToRotate = false;
		wantsToStop = false;
		
		if (false){
			ourRobot = yellowRobot;
		} else{
			ourRobot = blueRobot;
		}
		
		
		
		int balldist = move.getDist(ourRobot, ball);
		String sig = getSigToPoint(ourRobot, ball.getCoors(), ball.getCoors());
			
		
		
		if (balldist < 70 && isFacing(ourRobot, ball.coors)){
			sig = ("1 0 0 0 0");
			
		}
		
		
		/*
		socket.send(sig, 0);
		System.out.println("Sending OK");
		socket.recv(0);
		System.out.println("Recieving OK");
		*/
	   
	}


	/**
	 * Get the most recent information from vision
	 */
	public void getPitchInfo() {

		
		// Get pitch information from vision
		state = vision.getWorldState();
		ball.setCoors(new Position(state.getBallX(), state.getBallY()));	
		
		yellowRobot.setAngle(state.getYellowOrientation());
		yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
		
		blueRobot.setAngle(state.getBlueOrientation());
		blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
		
		}
	public String shimmy(){
		
		return "1 0 0 0 0";
		
	}
	
	public String goStraight (){
		
		
		return "";
	}
	
	//TODO - generalise the rotation part so it can rotate to a point and move to a different one.
	//Can't remember maths, someone can fix this.
	//TODO - replace the old code with basically just the last method here. 
	//GLHF
	//ALSO TODO - move this to a dedicated planning class, will need to mess about with bools. 
	
	
	public static double getRobotAngle(Robot robot){
		// robot.getAngle() returns the angle between the robot and the left bottom
		// corner of the screen
		double robAngle = robot.getAngle();
		// we rotate robAngle by 90 degrees in order to have the angle between the robot and
		// the top left corner of the screen
		// remember: top left corner of screen = (0, 0)
		robAngle = robAngle - Math.PI/2;
		robAngle += TENPI;
		robAngle = robAngle % TWOPI;
		
		
		return robAngle;
		
	}
	
	public static double getAngleFromRobotToPoint(Robot robot, Position point) {
		// angleToPoint is the angle between the top left corner of the pitch and the point
		// angleToRobot is the angle between the top left corner of the pitch and the robot
		// angleBetweenRobotAndPoint is the clockwise angle between the robot and the point
		double angleToPoint = Math.atan2( point.getY() - robot.getCoors().getY(), point.getX()-robot.getCoors().getX());
		double angleToRobot = getRobotAngle(robot);
		double angleBetweenRobotAndPoint = angleToPoint - angleToRobot;
		//it was giving a weird slightly negative number here in the robot north, ball in q2 case. Resolved.
		angleBetweenRobotAndPoint += TENPI;
		angleBetweenRobotAndPoint = angleBetweenRobotAndPoint % TWOPI;

		return angleBetweenRobotAndPoint;
	}
	
	public double getRotationValue(double angle){
		double value = 0;;
		if (angle > (Math.PI) ){
			if (((Math.PI*2) - angle) > (Math.PI/9)) {
				value = 0.1; 
				System.out.println("CCW rotation");
			}
			
		} else if (angle > Math.PI/9) {
			value = -0.1;
			System.out.println("CW rotation");
		}
		if (!(value == 0)) wantsToRotate = true;
	
		return value;
	}
	
	public int[] getMotorValues(double rotationfactor, double angle){
		double[] motors = {0,0,0,0};
		int[] returnvalues = {0,0,0,0};
		double multfactor = 255;
		double maxval = 0.0001;
		if (wantsToRotate) {
			for (int i = 0; i<4;i++){
				motors[i] += rotationfactor;
			}
		}
		if (!(wantsToStop)) {
			motors[0] -= (Math.cos(angle));
			motors[1] += (Math.sin(angle));
			motors[2] -= (Math.sin(angle));
			motors[3] += (Math.cos(angle));
		}
		
		if (wantsToStop && wantsToRotate){
			multfactor = (multfactor/Math.abs(rotationfactor))/4;
		} else {
			for (int i = 0; i<4;i++){
				if (Math.abs(motors[i]) > maxval) maxval = Math.abs(motors[i]); 
			}
			multfactor = (multfactor/maxval);
			
		}
		
		for (int i = 0;i<4;i++)	{
			returnvalues[i] = (int) (motors[i]*multfactor);
		}
				
		return returnvalues;
		
	}
	
	public String createSignal(int[] codes){
		String sig = "1 "+codes[0]+" "+codes[1]+" "+codes[2]+" "+codes[3];
		
		return sig;
	}
	
	public String getSigToPoint(Robot robot, Position destination, Position rotation){
		double movementangle = getAngleFromRobotToPoint(robot,destination);
		double rotationangle = getAngleFromRobotToPoint(robot,rotation);
		return createSignal(getMotorValues(getRotationValue(rotationangle),movementangle));
		
	}
	
	public boolean isFacing(Robot robot, Position point){
		double angle = getAngleFromRobotToPoint(robot,point);
		double value = getRotationValue(angle);
		
		if (!(value == 0)) return true; else return false;
	}
	
	public Position pointBehindBall(Robot goal, Position ball){
	
		double goalBallAng = getAngleFromRobotToPoint(goal,ball);
		
		double rvrsBallToGoal = Math.PI - goalBallAng;
		
		Position goPoint;
		
		if(goal == goalL){
			int newX = (int) (ball.getX() - (30*Math.sin(rvrsBallToGoal)));
			int newY = (int) (ball.getY() - (30*Math.cos(rvrsBallToGoal)));
			goPoint = new Position(newX,newY);
		}else{
			int newX = (int) (ball.getX() + (30*Math.sin(rvrsBallToGoal)));
			int newY = (int) (ball.getY() + (30*Math.cos(rvrsBallToGoal)));
			goPoint = new Position(newX,newY);
		}
		
		if (!withinPitch(goPoint)){
			goPoint.setX((ball.getX()));
			goPoint.setY((ball.getY()));
		}
		return goPoint;
		
	}
	
	public static boolean withinPitch(Position coors){
		int coorX = coors.getX();
		int coorY = coors.getY();
		
		if(coorX > 39 && coorX < 602 && coorY > 100 && coorY < 389) return true;
		return false;
	}
	
}


	
	

	

