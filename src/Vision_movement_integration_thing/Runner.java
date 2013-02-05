package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

public class Runner extends Thread {
 
	// Objects
	public static Ball ball;
	static WorldState state;
	static Move move;
	static Runner instance = null;
	static Robot blueRobot;
	static Robot yellowRobot;
	private static ControlGUI thresholdsGUI;
	Vision vision;
	static int maxballspeed = 0;
	static double ballangle = 0;
	static int oldballx = 0;
	static Ball oldball;
	private static Context context;
	private static Socket socket;
	
	public static void main(String args[]) {
		context = ZMQ.context(1);

		//  Socket to talk to clients over IPC
		socket = context.socket(ZMQ.REQ);
		socket.connect("ipc:///tmp/nxt_bluetooth_robott");
			
		instance = new Runner();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public Runner() {
		
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
		oldball = new Ball();
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
			sleep(100);
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
		 try {
			    if (ball.getCoors().getX() > 0) {
				oldball.setCoors(ball.getCoors());
			    }
		 		}catch (NullPointerException e){
			    System.out.println("Oldball not set again");
			    }
		getPitchInfo();
		
		int dist = move.getDist(blueRobot, ball);
		
		int yrX = yellowRobot.getCoors().getX();
		int yrY = yellowRobot.getCoors().getY();
		int yrD = (int) yellowRobot.getAngle();
		
		int brX = blueRobot.getCoors().getX();
		int brY = blueRobot.getCoors().getY();
		float brD = blueRobot.getAngle();
		
		int bX = ball.getCoors().getX();
		int bY = ball.getCoors().getY();
		try {
		int currspeed = move.getBallDist(ball, oldball);
		maxballspeed = (maxballspeed > currspeed) ? maxballspeed : currspeed;
		} catch (NullPointerException e) {
		System.out.println("No value of oldball set");
		}
//		
//		try{
//		ballangle = move.getAngleToBall(blueRobot, ball);
//		} catch (NullPointerException e) {
//		System.out.println("No value of oldball set");
//			
//		}
//		
		// get real theta that we can use for real math
		double robotTheta = brD - Math.PI/2;
		if (robotTheta > Math.PI) robotTheta -= Math.PI*2;
	
		// get theta of vector from robot to ball
		double robToBallTheta = Math.atan2(bY - brY, bX - brX);
		
		ballangle = robToBallTheta - robotTheta;
		
		//ballangle = ((ballangle*(-1)) > ballangle) ? (2*Math.PI + ballangle) : ballangle;
		System.out.println("YRobotX: " + yrX +" YRobotY: " + yrY + " YDir: " + yrD);
		System.out.println("BRobotX: " + brX + " BRobotY: " + brY + " BDir: " + brD );
		System.out.println("BallX: " + bX + " BallY: " + bY + " maxspeed :" + maxballspeed);
		System.out.println("Distance : " + dist + " angle: " + ballangle + " " + ballangle*180/Math.PI);
		int m2 = (int) (Math.sin(ballangle)*255);
		int m1 = (int) -(Math.cos(ballangle)*255);
		int m4 = (int) (Math.cos(ballangle)*255);
		int m3 = (int) -(Math.sin(ballangle)*255);
		String sig = (dist > 52) ? ("1" + " " + m1 + " "+ m2 + " " + m3 + " " + m4) : ("1 0 0 0 0");
		
		System.out.println("I math ok daddy " + sig);
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
	

	}

	

