package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;



public class RunnerOld extends Thread {
 
	// Objects
	public static Ball ball;
	static WorldState state;
	static Move move;
	static RunnerOld instance = null;
	static Robot blueRobot;
	static Robot yellowRobot;
	private static ControlGUI thresholdsGUI;
	Vision vision;
	static double ballangle = 0;
	static Ball oldBall;
	static Ball olderBall;
	static Ball oldestBall;


	
	public static void main(String args[]) {
				
		instance = new RunnerOld();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public RunnerOld() {
		
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
		oldBall = new Ball();
		olderBall = new Ball();
		oldestBall = new Ball();
		move = new Move();
		ball.setCoors(new Position(0, 0));
		oldBall.setCoors(new Position (0,0));
		olderBall.setCoors(new Position(0, 0));
		oldestBall.setCoors(new Position (0,0));
		//BallCalculator ballCalculator = new BallCalculator();
		start();
	}

	/**
	 * Planning thread which begins planning loop - bluetooth server start will also go here later
	 */
	public void run() {	
    	startVision();
		
		do {
		try {
			sleep(0);
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
		
		
	}

	/**
	 * Get the most recent information from vision
	 */
	public void getPitchInfo() {
		oldestBall.setCoors(ball.getCoors());
		try {
			sleep(39);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		olderBall.setCoors(ball.getCoors());
		try {
			sleep(39);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		oldBall.setCoors(ball.getCoors());
		try {
			sleep(39);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Get pitch information from vision
		state = vision.getWorldState();
		ball.setCoors(new Position(state.getBallX(), state.getBallY()));	
		
		yellowRobot.setAngle(state.getYellowOrientation());
		yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
		
		blueRobot.setAngle(state.getBlueOrientation());
		blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
		//double speed = BallCalculator.getBallSpeed(oldBall, ball);
		//System.out.println("Speed of Ball: " + speed);
		
		double acceleration = BallCalculator.getBallAcceleration(oldestBall, olderBall, oldBall, ball);
		System.out.println("Acceleration of Ball: " + acceleration);
			
			
			
		
		}
	public String shimmy(){
		
		return "1 0 0 0 0";
		
	}
}


	
	

	

