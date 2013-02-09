package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

public class Dribble extends Thread {
	public static final double TWOPI = Math.PI * 2;
	// Objects
	public static Ball ball;
	static WorldState state;
	static Move move;
	static Dribble instance = null;
	static Robot blueRobot;
	static Robot yellowRobot;
	private static ControlGUI thresholdsGUI;
	Vision vision;
	static double ballangle = 0;
	private static Context context;
	private static Socket socket;
	boolean wantsToRotate = false;
	boolean wantsToStop = false;
	boolean visitedBall = false;
	Position dribblepoint;
	boolean hasSet = false;
	static String colour;
	static Robot ourRobot;
	
	public static void main(String args[]) {
		context = ZMQ.context(1);
		colour = args[0];

		//  Socket to talk to clients over IPC
		socket = context.socket(ZMQ.REQ);
		socket.connect("ipc:///tmp/nxt_bluetooth_robott");
		
		instance = new Dribble();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public Dribble() {
		
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
		move = new Move();
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
		getPitchInfo();
		wantsToRotate = false;
		wantsToStop = false;
		visitedBall =false;
		if (colour.equals("yellow")	){
			ourRobot = yellowRobot;
		} else{
			ourRobot = blueRobot;
		}
		
		
		int dist = 0;
		dist = move.getDist(ourRobot, ball);
		System.out.println(ball.getCoors().getX() + " "+ ball.getCoors().getY());
		visitedBall = (dist < 80 && dist > 0 && isFacing(ourRobot, ball.getCoors())) ? true : false; 
		System.out.println("Output :" +isFacing(ourRobot, ball.getCoors()));
		//set first time only
		if (!(hasSet)) {
			if(ourRobot.getCoors().getX() < ball.getCoors().getX()){
				dribblepoint = new Position ((ball.getCoors().getX() + 100), ball.getCoors().getY());
			}else{
				dribblepoint = new Position ((ball.getCoors().getX() - 100), ball.getCoors().getY());
			}
		}
		
		String sig;
		if (visitedBall) {
			sig = getSigToPoint(ourRobot, dribblepoint, dribblepoint);
			System.out.println("Goal is dribble");
		} else { 
			sig = getSigToPoint(ourRobot, ball.getCoors(), ball.getCoors());
			
			if(ourRobot.getCoors().getX() < ball.getCoors().getX()){
				dribblepoint = new Position ((ball.getCoors().getX() + 100), ball.getCoors().getY());
				hasSet = true;
			}else{
				dribblepoint = new Position ((ball.getCoors().getX() - 100), ball.getCoors().getY());
				hasSet = true;
			}
			
			System.out.println("Goal is ball");
		}
		Ball dribbleBall = new Ball();
		dribbleBall.setCoors(dribblepoint);
		
		int pointdist = move.getDist(ourRobot, dribbleBall);
		if (pointdist < 52){
			sig = ("1 0 0 0 0");
		}
		
		
		
						
		//socket.send(sig, 0);
		System.out.println("Sending OK");
		//socket.recv(0);
		System.out.println("Recieving OK");
		
	   
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
	
	
	public double getRobotAngle(Robot robot){
		// robot.getAngle() returns the angle between the robot and the left bottom
		// corner of the screen
		double robAngle = robot.getAngle();
		// we rotate robAngle by 90 degrees in order to have the angle between the robot and
		// the top left corner of the screen
		// remember: top left corner of screen = (0, 0)
		robAngle = robAngle - Math.PI/2;
		if (robAngle < 0) {
			robAngle += TWOPI;
		}
		if (robAngle > TWOPI) {
			robAngle -= TWOPI;
		}
		
		return robAngle;
		
	}
	
	public double getAngleFromRobotToPoint(Robot robot, Position point) {
		// angleToPoint is the angle between the top left corner of the pitch and the point
		// angleToRobot is the angle between the top left corner of the pitch and the robot
		// angleBetweenRobotAndPoint is the clockwise angle between the robot and the point
		double angleToPoint = Math.atan2(point.getY(), point.getX());
		double angleToRobot = getRobotAngle(robot);
		double angleBetweenRobotAndPoint = angleToPoint - angleToRobot;
		if (angleBetweenRobotAndPoint < 0) {
			angleBetweenRobotAndPoint += TWOPI;
		}
		if (angleBetweenRobotAndPoint > TWOPI) {
			angleBetweenRobotAndPoint -= TWOPI;
		}
		System.out.println(angleBetweenRobotAndPoint);
		return angleBetweenRobotAndPoint;
	}
	
	public double getRotationValue(double angle){
		double value = 0;;
		if (angle > (Math.PI/2) ){
			if ((360-angle) > (Math.PI/9)) {
				value = 0.2; 
				System.out.println("CCW rotation");
			}
			
		} else if (angle > Math.PI/9) {
			value = -0.2;
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
	
}


	
	

	

