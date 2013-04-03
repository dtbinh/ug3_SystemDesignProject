package JavaVision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import PitchObject.Position;
import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.ImageFormatException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * The main class for showing the video feed and processing the video data.
 * Identifies ball and robot locations, and robot orientations.
 * 
 * @author s0840449
 */
public class Vision extends WindowAdapter {
	//private VideoDevice videoDev;
	private JLabel label;
	private JFrame windowFrame;
	//private FrameGrabber frameGrabber;
	private int width = 640;
	private int height = 480;

	private int minX, minY, maxX, maxY;
	private long last_update = -1;
	private WorldState worldState;
	private boolean ready = false;
	//private ThresholdsState thresholdsState;
	//private VisionConstants pitchConstants;
	//private static final double barrelCorrectionX = -0.016;	
	//private static final double barrelCorrectionY = -0.06;
	//BufferedImage frameImage;
	// private int[] xDistortion;
	// private int[] yDistortion;

	static volatile Context context;
	static volatile Socket socket;

	/**
	 * Default constructor.
	 * 
	 * @param videoDevice
	 *            The video device file to capture from.
	 * @param width
	 *            The desired capture width.
	 * @param height
	 *            The desired capture height.
	 * @param videoStandard
	 *            The capture standard.
	 * @param channel
	 *            The capture channel.
	 * @param compressionQuality
	 *            The JPEG compression quality.
	 * @param worldState
	 * @param thresholdsState
	 * @param pitchConstants
	 * 
	 * @throws V4L4JException
	 *             If any parameter if invalid.
	 */
	public Vision(String videoDevice, int width, int height, int channel,
			int videoStandard, int compressionQuality, WorldState worldState,
			ThresholdsState thresholdsState, VisionConstants pitchConstants)
	throws V4L4JException {

		/* Set the state fields. */
		this.worldState = worldState;
		//this.thresholdsState = thresholdsState;
		//this.pitchConstants = pitchConstants;

		/* Initialise the GUI that displays the video feed. */
		//initFrameGrabber(videoDevice, width, height, channel, videoStandard,
		//		compressionQuality);

	}

	public Vision(WorldState worldState)
	throws V4L4JException {

		/* Set the state fields. */
		this.worldState = worldState;
		System.out.println("Gets to vision");
		initGUI();
		initSocket();

		//mainLoop();
	}

	private void initSocket() {
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		socket.connect("tcp://127.0.0.1:6666");

		//System.out.println("Gets here");
		// Update mins/maxs
		socket.send("P", 0);
		String pitchConstants = socket.recvStr();
		String[] tokens = split(pitchConstants, " ");
		//System.out.println("Pitch constants + " + pitchConstants);

		// TODO: verify
		this.minX = Integer.parseInt(tokens[0]);
		this.minY = Integer.parseInt(tokens[1]);
		this.maxX = Integer.parseInt(tokens[2]);
		this.maxY = Integer.parseInt(tokens[3]);

		// Update time
		//socket.send("E");
		ready = true;
		processAndUpdateImage();
	}

	public void windowClosing(WindowEvent e) {
		// TODO: clear Bluetooth on window close
		this.socket.close();
		System.exit(0);
	}

	private void initGUI() {
		windowFrame = new JFrame("Vision Window");
		label = new JLabel();
		windowFrame.getContentPane().add(label);
		windowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		windowFrame.addWindowListener(this);
		windowFrame.setVisible(true);
		windowFrame.setSize(width, height);
	}

	/**
	 * Processes an input image, extracting the ball and robot positions and
	 * robot orientations from it, and then displays the image (with some
	 * additional graphics layered on top for debugging) in the vision frame.
	 * 
	 * @param image
	 *            The image to process and then show.
	 * @param counter
	 */
	public void processAndUpdateImage() {
		if (!ready) {
			return;
		}
		
		String[] tokens;
		long time;
		do {
			socket.send("E", 0);
			String data = socket.recvStr();
			//System.out.println("Data  = " + data);
			
			tokens = split(data, " ");

			time = Long.parseLong(tokens[8]);
		} while (time <= this.last_update);

		this.last_update = time;

		// TODO: verify
		int yellowX = Integer.parseInt(tokens[0]);
		int yellowY = Integer.parseInt(tokens[1]);
		float yellowAngle = Integer.parseInt(tokens[2]);

		int blueX = Integer.parseInt(tokens[3]);
		int blueY = Integer.parseInt(tokens[4]);
		float blueAngle = Integer.parseInt(tokens[5]);

		int ballX = Integer.parseInt(tokens[6]);
		int ballY = Integer.parseInt(tokens[7]);

		Position ball;
		Position blue;
		Position yellow;

		if (ballX != -1){
			ball = new Position(ballX, ballY);
			//ball.fixValues(worldState.getBallX(), worldState.getBallY());
		} else {
			ball = new Position(worldState.getBallX(), worldState.getBallY());
		}

		if (blueX != -1) {
			blue = new Position(blueX, blueY);
			//blue.fixValues(worldState.getBlueX(), worldState.getBlueY());
		} else {
			blue = new Position(worldState.getBlueX(), worldState.getBlueY());
		}

		if (yellowX != -1) {
			yellow = new Position(yellowX, yellowY);
			//yellow.fixValues(worldState.getYellowX(), worldState.getYellowY());
		} else {
			yellow = new Position(worldState.getYellowX(), worldState.getYellowY());
		}

		worldState.setBlueOrientation((float) Math.toRadians(360 - blueAngle));
		worldState.setYellowOrientation((float) Math.toRadians(360 - blueAngle));

		worldState.setBallX(ball.getX());
		worldState.setBallY(ball.getY());

		worldState.setBlueX(blue.getX());
		worldState.setBlueY(blue.getY());

		worldState.setYellowX(yellow.getX());
		worldState.setYellowY(yellow.getY());

		worldState.updateCounter();
		worldState.setNewFrame(true);
	}

	public WorldState getWorldState() {
		return worldState;
	}

	// TODO: test these values!
	public int getMinX() { return this.minX; }
	public int getMaxX() { return this.maxX; }
	public int getMinY() { return this.minY; }
	public int getMaxY() { return this.maxY; }

	static String[] split(String str, String delim) {
		StringTokenizer tokenizer = new StringTokenizer(str, delim);
		ArrayList<String> tokens = new ArrayList<String>(str.length());
		while (tokenizer.hasMoreTokens()) {
			tokens.add(tokenizer.nextToken());
		}
		return tokens.toArray(new String[0]);
	}
}
