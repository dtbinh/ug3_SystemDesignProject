package JavaVision;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import PitchObject.Ball;
import PitchObject.Position;
import PitchObject.Robot;

public class VisionReader extends Thread {
	private static final int YELLOW = 0;
	private static final int BLUE = 1;
	// Objects
	private static volatile WorldState state;
	private static volatile ControlGUI thresholdsGUI;
	private static volatile Robot blueRobot;
	private static volatile Robot yellowRobot;
	private static volatile Vision vision;
	private static volatile Ball ball;
	private static volatile int colour;
	
	public static void main(String args[]) {
		VisionReader vr = new VisionReader(args[0]);
		vr.run();
	}

	public VisionReader(String string) {
		colour = string.equalsIgnoreCase("yellow") ? YELLOW : BLUE;
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
		start();
	}

	public void run() {
		startVision();
		
		while (true) {
			updatePitchVariables();
		}
	}

	private void startVision() {	    
		/*
		 * Creates the control
		 * GUI, and initialises the image processing.
		 */
		WorldState worldState = new WorldState();
		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */
		VisionConstants pitchConstants = new VisionConstants(0);

		/* Default values for the main vision window. */
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 80; //dropped compression of the camera slightly - feel free to experiment further

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
	public void updatePitchVariables() {
		// Get pitch information from vision
		state = vision.getWorldState();
		ball.setCoors(new Position(state.getBallX(), state.getBallY()));	

		yellowRobot.setAngle(state.getYellowOrientation());
		yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
		blueRobot.setAngle(state.getBlueOrientation());
		blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
	}

	//getters for planning thread.
	public Robot getOurRobot(){
		if (colour == YELLOW) {
			return yellowRobot;
		} else {
			return blueRobot;
		}
	}
	
	public Robot getTheirRobot(){
		if (colour == YELLOW) {
			return blueRobot;
		} else {
			return yellowRobot;
		}
	}
	
	public Ball getBall() {
		return ball;
	}
	
	public int getDirection() {
		return state.getDirection();
		// 0 = right, 1 = left.
	}
	
	public boolean getStarted() {
		if (state == null) {
			return false;
		}
		
		return state.getStarted();
	}
		
	public int getMinX() { return vision.getMinX(); }
	public int getMaxX() { return vision.getMaxX(); }
	public int getMinY() { return vision.getMinY(); }
	public int getMaxY() { return vision.getMaxY(); }
}