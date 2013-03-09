package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;


public class VisionReader extends Thread {
	// Objects
	private Ball ball;
	private WorldState state;
	static VisionReader instance = null;
	private Robot blueRobot;
	private Robot yellowRobot;
	private ControlGUI thresholdsGUI;
	private Vision vision;
	private static String colour;
	private Robot ourRobot;
	private Robot theirRobot;

	static int its = 0;
	
	public static void main(String args[]) {
		
		instance = new VisionReader(args[0]);

	}

	/**
	 * Instantiate objects and start the planning thread
	 * @param string 
	 */
	public VisionReader(String string) {
		colour = string;
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
			
		start();
	}

	
	public void run() {		
		startVision();
		
		do {
		try {
			sleep(40);
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted");
			e.printStackTrace();
		}
		its++;
		getPitchInfo();
		}while(true);
	}

	/**
	 * Method to initiate the vision
	 */
	private void startVision() {	    
		/**
		 * Creates the control
		 * GUI, and initialises the image processing.
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
		if (colour.equals("yellow")){
			ourRobot = yellowRobot;
			theirRobot = blueRobot;
		} else{
			ourRobot = blueRobot;
			theirRobot = yellowRobot;
		}
			
	}
	//getters for planning thread.
	public Robot getOurRobot(){
		if (colour.equals("yellow")){
			return yellowRobot;
		} else{
			return blueRobot;
		}
	}
	
	public Robot getTheirRobot(){
		if (colour.equals("yellow")){
			return blueRobot;
		} else{
			return yellowRobot;
		}
	}
	
	public Ball getBall(){
		return this.ball;
	}
	
	public boolean readable(){
		return (its>30);
	}
	public int getDirection(){
		return state.getDirection();
		// 0 = right, 1 = left.
	}
		
	public int getMinX() { return vision.getMinX(); }
	public int getMaxX() { return vision.getMaxX(); }
	public int getMinY() { return vision.getMinY(); }
	public int getMaxY() { return vision.getMaxY(); }
		
}


	
	

	


