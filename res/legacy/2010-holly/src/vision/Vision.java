package vision;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.logging.Level;
import java.util.logging.Logger;
import baseSystem.Singleton;
import baseSystem.SystemOverview;

/**
 * Main class of the vision subsystem. Run this if you want vision to run on its own
 * @author Matt
 * @author Ben
 */
public class Vision extends Thread {

	public final static String VIDEO_CARD = "/dev/video0";
	public final static int FRAME_WIDTH = 768;
	public final static int FRAME_HEIGHT = 576;
	public final static int VIDEO_CHANNEL = 2;
	public final static int PITCH_START_X = 20;
	public final static int PITCH_START_Y = 80;
	public final static int PITCH_END_X = 758;
	public final static int PITCH_END_Y = 490;

	/*
	 * Temporary Video Controls for Brightness, Contrast, etc.
	 *
	 * DEFAULT VALUES FOR CONTROLS:
	 * BRIGHTNESS: 32768
	 * CONTRAST: 32768
	 * FULL LUMA RANGE: 1
	 * HUE: 32768
	 * SATURATION: 32768
	 * UV RATIO: 49
	 * ***************************
	 * VINNIE VALUES FOR CONTROLS:
	 * BRIGHTNESS: 32768
	 * CONTRAST: 22272
	 * FULL LUMA RANGE: 1
	 * HUE: 32768
	 * SATURATION: 65535
	 * UV RATIO: 49
	 */
	public final static int BRIGHTNESS = 32768;
	public final static int CONTRAST = 32768;
	public final static int HUE = 32768;
	public final static int SATURATION = 32768;
	public final static int FULL_LUMA_RANGE = 1;
	public final static int UV_RATIO = 49;

	private int debug;
	private boolean finished;
	private WritableRaster raster;
	private WritableRaster tmpRaster;
	private ImageProcessor ip;
	private long start;
	private long frameSum;
	private int count;
	private Singleton singleton;
	private SystemOverview so;
	private GrabFrame frameGrabber;

	/**
	 * This is the main Vision object which is running in the separate thread.
	 * Currently all the video card initialisation is done by startCapture().
	 * Vision is a facade for the vision system, a coordinator component.
	 * Image processing and object recognition is done by the ImageProcessor.
	 * Tracker object is used to memorise objects' previous locations and feed
	 * coordinates back to the ImageProcessor which should improve object recognition.
	 */
	public Vision(int debug, boolean integrated) {
		super("Vision");
		this.debug = debug;
		finished = false;
		frameGrabber = new GrabFrame();

		if (integrated) {
			singleton = Singleton.getSingleton();
		}

		tmpRaster = Raster.createInterleavedRaster(new DataBufferByte(new byte[FRAME_HEIGHT * FRAME_WIDTH * 3],
				FRAME_HEIGHT * FRAME_WIDTH * 3), FRAME_WIDTH,
				FRAME_HEIGHT, 3 * FRAME_WIDTH, 3,
				new int[] {0, 1, 2}, null);
	}

	/**
	 * This is the main function of the Vision thread
	 */
	@Override
	public void run() {
		int[][] coords = new int[9][2];
		frameGrabber.start();
		frameGrabber.startCapture();

		// wait for a while
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {
			Logger.getLogger(Vision.class.getName()).log(Level.SEVERE, null, ex);
		}

		ip = new ImageProcessor(getPitchWidth(), getPitchHeight(), this.debug);

		so = new SystemOverview(ip, singleton);
		(new Thread(so)).start();

		while (!finished) {
			start = System.nanoTime();
			try {
				grabFrame(frameGrabber.getFrame());

				ip.setData(raster);
				coords = ip.getCoords();

				if (singleton != null) {
					singleton.setCoordinates(coords);
				}

				// update system overview
				if (so != null){
					so.update(raster, ip.getDebugThreshRaster(), ip.getDebugPlates(), coords);
				}
			} catch (V4L4JException ex) {
				System.out.println("VISION ERROR >> Could not retrieve the image from the Video Card!");
				ex.printStackTrace();
			}

			count++;
			if (count > 50) {
				count = 1;
				frameSum = 0;
			}
			frameSum += System.nanoTime() - start;

			if (!so.isVisible() && debug > 0) {
				System.out.println("VISION >> FPS: " + 1000 / (frameSum / (count * 1000000)));
			}
		}
		frameGrabber.stopCapture();
		System.out.println("VISION >> Shuting down...");
	}

	public synchronized void exit() {
		finished = true;
		frameGrabber.exit();
	}

	/**
	 * Wrapper function for grabbing a frame from the FrameGrabber and translating it 
	 * into an image format.
	 * @throws V4L4JException
	 */
	private void grabFrame(byte[] frame) throws V4L4JException {
		frameToImage(frame);
	}

	/** 
	 * This function transforms a byte array into a Raster which is used by the ImageProcessor.
	 * @param frame - byte frame grabbed from the video card
	 */
	private void frameToImage(byte[] frame) {
		long startFrameToImage = System.nanoTime();

		// writing the frame to the raster
		tmpRaster.setDataElements(0, 0, FRAME_WIDTH, FRAME_HEIGHT, frame);
		// carving out the pitch from the image
		raster = (WritableRaster) tmpRaster.createChild(PITCH_START_X, PITCH_START_Y, getPitchWidth(), getPitchHeight(), 0, 0, null);

		if (debug == 2) {
			System.out.println("VISION >> Time to carve out the pitch: " + (System.nanoTime() - startFrameToImage)/1000000.0);
		}
	}

	/*
	 * ===============
	 * GETTERS/SETTERS
	 * ===============
	 */

	/**
	 * @return Debug level.
	 */
	public int debugLevel() {
		return debug;
	}

	/**
	 * Sets the debug level.
	 * @param debug - new debug value
	 */
	public void setDebug(int debug) {
		this.debug = debug;
	}

	/**
	 * @return Height of the pitch (video frame) in pixels.
	 */
	public static int getPitchHeight() {
		return PITCH_END_Y - PITCH_START_Y;
	}

	/**
	 * @return Width of the pitch (video frame) in pixels.
	 */
	public static int getPitchWidth() {
		return PITCH_END_X - PITCH_START_X;
	}

	/**
	 * Only {@link baseSystem.Singleton#getVisionDataAge() Singleton} should use this method to get the 
	 * time when the frame was grabbed.
	 * @return Time of frame grab in milliseconds.
	 */
	public long getFrameTime() {
		return start;
	}

	/**
	 * Used only for testing the vision system.
	 * @param args
	 */
	public static void main(String[] args) {
		Vision vsys = new Vision(1, false);
		vsys.start();
	}
}
