package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;

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
	static int ballangle = 0;
	static int oldballx = 0;
	static Ball oldball;

	public static void main(String args[]) {
		
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
		mainLoop();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		int dist = move.getDist(blueRobot, ball);
		
		int yrX = yellowRobot.getCoors().getX();
		int yrY = yellowRobot.getCoors().getY();
		int yrD = (int) yellowRobot.getAngle();
		
		int brX = blueRobot.getCoors().getX();
		int brY = blueRobot.getCoors().getY();
		float brD = blueRobot.getAngle();
		
		int bX = ball.getCoors().getX();
		int bY = ball.getCoors().getY();
	
		boolean hasBall = (dist < 52);
			
		
		try{
		ballangle = move.getAngleToBall(blueRobot, ball);
		} catch (NullPointerException e) {
		System.out.println("No value of oldball set");
			
		}
		
		System.out.println("YRobotX: " + yrX +" YRobotY: " + yrY + " YDir: " + yrD);
		System.out.println("BRobotX: " + brX + " BRobotY: " + brY + " BDir: " + brD );
		System.out.println("BallX: " + bX + " BallY: " + bY );
		System.out.println("Distance : " + dist + " angle: " + ballangle);
		System.out.println("Has Ball?" + hasBall);
	   
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

