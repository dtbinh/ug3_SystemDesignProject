package Planning;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import JavaVision.*;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

public class RunnerOlderStill extends Thread {
 
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
	private static Context context;
	private static Socket socket;


	public static void main(String args[]) {
		context = ZMQ.context(1);

		//  Socket to talk to clients over IPC
		socket = context.socket(ZMQ.REQ);
		socket.connect("tcp://127.0.0.1:5555");

		instance = new RunnerOld();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public RunnerOlderStill() {

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

		int dist = move.getDist(blueRobot, ball);

		int yrX = yellowRobot.getCoors().getX();
		int yrY = yellowRobot.getCoors().getY();
		int yrD = (int) yellowRobot.getAngle();


		int brX = blueRobot.getCoors().getX();
		int brY = blueRobot.getCoors().getY();
		float brD = blueRobot.getAngle();

		int bX = ball.getCoors().getX();
		int bY = ball.getCoors().getY();


		// get real theta that we can use for real math
		double robotTheta = brD - Math.PI/2;
		if (robotTheta > Math.PI) robotTheta -= Math.PI*2;



		// get theta of vector from robot to ball
		double robToBallTheta = Math.atan2(bY - brY, bX - brX);
		System.out.println(" Ball x = :" + ball.getCoors().getX() + " Ball y = : " + ball.getCoors().getY());
		ballangle = robToBallTheta - robotTheta;

		System.out.println(ballangle);
//		System.out.println("YRobotX: " + yrX +" YRobotY: " + yrY + " YDir: " + yrD);
//		System.out.println("BRobotX: " + brX + " BRobotY: " + brY + " BDir: " + brD );
//		System.out.println("BallX: " + bX + " BallY: " + bY );
//		System.out.println("Distance : " + dist + " angle: " + ballangle + " " + ballangle*180/Math.PI);
//	
		double m2 = (Math.sin(ballangle));
		double m1 = -(Math.cos(ballangle));
		double m4 = (Math.cos(ballangle));
		double m3 = -(Math.sin(ballangle));



		boolean wantsToStop = false;
		if (dist < 52) { 
			m1 = 0;
			m2 = 0;
			m3 = 0;
			m4 = 0;
			wantsToStop = true;
		}

		boolean wantsToRotate = false;
		if (ballangle < 0 ){
			if ((-1*ballangle) > (Math.PI/9)) {
			m1 += 0.2;
			m2 += 0.2;
			m3 += 0.2;
			m4 += 0.2;	
			wantsToRotate = true;
			}

		} else if (ballangle > Math.PI/9) {
			m1 -= 0.2;
			m2 -= 0.2;
			m3 -= 0.2;
			m4 -= 0.2;
			wantsToRotate = true;
		}


		double[] motors = {m1,m2,m3,m4};
		double motormax = 0.01;
		for (int i = 0; i < 4; i++){
			motormax =  ((Math.pow(motors[i], 2))>(Math.pow(motormax,2))) ? motors[i] : motormax;
		}

		motormax = (motormax > 0) ? motormax : motormax*-1;
		double multfactor = 255/motormax;

		multfactor =  (wantsToRotate && wantsToStop) ? multfactor/4 : multfactor;
		int mot1 = (int) (m1 * multfactor);
		int mot2 = (int) (m2 * multfactor);
		int mot3 = (int) (m3 * multfactor);
		int mot4 = (int) (m4 * multfactor);	


		String sig = ("1 " + mot1 + " " + mot2 + " " + mot3 + " " + mot4);

		//sig = ("1 0 0 0 0");

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
	public String shimmy(){

		return "1 0 0 0 0";

	}
}


