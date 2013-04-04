package vision.old;

import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PImage;
import javax.imageio.ImageIO;

import baseSystem.Singleton;
import baseSystem.SystemOverview;

/**
 * Main class of the vision subsystem. Run this if you want vision to run on its own
 * @author Matt
 *
 */

public class Vision extends Thread {

	public final static int FRAME_WIDTH = 768;
	public final static int FRAME_HEIGHT = 576;
	public final static String VIDEO_CARD = "/dev/video0";
	public final static int VIDEO_CHANNEL = 2;
	public final static int PITCH_START_X = 28;
	public final static int PITCH_START_Y = 96;
	public final static int PITCH_END_X = 740;
	public final static int PITCH_END_Y = 478;
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
	private boolean waiting;
	private boolean finished;
	private WritableRaster raster;
	private BufferedImage image;
	private ImageProcessor4 ip;
	private VideoDevice vd;
	private FrameGrabber fg;
	private byte[] grabbedFrame;
	private ByteBuffer bb;
	private long start;
	private Singleton singleton;
	private VisionFrame vf;
	private SystemOverview so;
	private GrabFrame frameGrabber;

	/**
	 * This is the main Vision object which is running in the separate thread.
	 * Currently all the video card initialisation is done by startCapture().
	 * Vision is a facade for the vision system a coordinator component.
	 * Image processing and object recognition is done by the ImageProcessor.
	 * Tracker object is used to memorise objects' previous locations and feed
	 * coordinates back to the ImageProcessor which should improve object recognition.
	 *
	 * TODO: integration with the navigation and strategy.
	 */
	public Vision(int debug, boolean integrated) {
		super("Vision");
		this.debug = debug;
		waiting = false;
		finished = false;
		frameGrabber = new GrabFrame();
		if (integrated) {
			singleton = Singleton.getSingleton();
		}
	}

	public static int getPitchHeight() {
		return PITCH_END_Y - PITCH_START_Y;
	}

	public static int getPitchWidth() {
		return PITCH_END_X - PITCH_START_X;
	}

	/**
	 * Only {@link baseSystem.Singleton#getVisionDataAge() Singleton} should use this method to get the time when the frame was grabbed.
	 * @return Time of frame grab in milliseconds. 
	 */
	public long getFrameTime() {
		return start;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public int debugLevel() {
		return debug;
	}

	/**
	 * This the main function of the Vision thread
	 */
	@Override
	public void run() {
		
		int[] coords = new int[9];
		frameGrabber.start();
		frameGrabber.startCapture();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {
			Logger.getLogger(Vision.class.getName()).log(Level.SEVERE, null, ex);
		}

		ip = new ImageProcessor4(getPitchWidth(), getPitchHeight(), this.debug);
		vf = new VisionFrame(ip);
		//so = new SystemOverview(ip);

		while (!finished) {
			
			try {
				grabFrame(frameGrabber.getFrame());

				if (singleton != null) {
					if (singleton.getCoordinates() != null) {
						coords = ip.getPosition(raster, singleton.getCoordinates());
					} else {
						coords = ip.getPosition(raster, coords);
					}
					//singleton.setCoordinates(coords);
				} else {
					coords = ip.getPosition(raster, coords);
				}
				// update the vision frame
				if (vf != null)
					vf.update(raster, coords);
			} catch (V4L4JException ex) {
				System.out.println("Could not retrieve the image from the Video Card!");
				ex.printStackTrace();
			}
			if (debug > 0) {
				System.out.println("VISION >> FPS: " + 1000 / (System.currentTimeMillis() - start));
			}
			
		}
		frameGrabber.stopCapture();
		System.out.println("VISION: shuting down...");

		
	}

	public synchronized void exit() {
		finished = true;
		frameGrabber.exit();
	}

	/** This function transforms a byte array into a Raster
	 * which is used by the ImageProcessor
	 *
	 * @param frame byte frame grabbed from the video card
	 */
	private void frameToImage(byte[] frame) {
		/* TODO: enable when using grabPitch()

        raster = Raster.createInterleavedRaster(new DataBufferByte(new byte[getPitchHeight() * getPitchWidth() * 3],
        getPitchHeight() * getPitchWidth() * 3), getPitchWidth(),
        getPitchHeight(), 3 * getPitchWidth(), 3,
        new int[]{0, 1, 2}, null);
        raster.setDataElements(0, 0, getPitchWidth(), getPitchHeight(), frame);
		 */

		// TODO: should be removed if grabPitch() is used inside grabFrame()
		raster = Raster.createInterleavedRaster(new DataBufferByte(new byte[FRAME_HEIGHT * FRAME_WIDTH * 3],
				FRAME_HEIGHT * FRAME_WIDTH * 3), FRAME_WIDTH,
				FRAME_HEIGHT, 3 * FRAME_WIDTH, 3,
				new int[]{0, 1, 2}, null);
		// writing the frame to the raster
		raster.setDataElements(0, 0, FRAME_WIDTH, FRAME_HEIGHT, frame);

		// carving out the pitch from the image
		raster = (WritableRaster) raster.createChild(PITCH_START_X, PITCH_START_Y, getPitchWidth(), getPitchHeight(), 0, 0, null);
	}

	/**
	 * This function translates byte frame into PImage format used by OpenCV
	 * @param frame byte frame grabbed from the video card
	 * @return image translated into PImage format
	 */
	private PImage frameToPImage(byte[] frame) {
		raster = Raster.createInterleavedRaster(new DataBufferByte(new byte[FRAME_WIDTH * FRAME_HEIGHT * 3], FRAME_WIDTH * FRAME_HEIGHT * 3), FRAME_WIDTH, FRAME_HEIGHT, 3 * FRAME_WIDTH, 3, new int[]{0, 1, 2}, null);
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		//setting a buffered image which is going to be used by the ImageProcessor
		image = new BufferedImage(cm, raster, false, null);
		//writing the frame to the image
		raster.setDataElements(0, 0, FRAME_WIDTH, FRAME_HEIGHT, frame);
		PImage pImage = new PImage(image);
		return pImage;
	}

	/**
	 * This function translates byte frame into array of integers
	 * where each integer is a pixel with interleaved RGB values
	 * @param frame byte frame grabbed from the video card
	 * @return an int array of pixels
	 */
	@Deprecated
	private int[] frameToPixelArray(byte[] frame) {
		int[] pixels = new int[frame.length / 3];
		int R_MASK = 0x00ff0000;
		int G_MASK = 0x0000ff00;
		int B_MASK = 0x000000ff;
		for (int i = 0; i < pixels.length; i++) {

			// merging GBR consecutive bytes from the buffer into single int RGB value
			pixels[i] = (((int) frame[i]) & R_MASK) | (((int) frame[i + 1]) & G_MASK) | (((int) frame[i + 2]) & B_MASK);
		}
		return pixels;
	}

	/**
	 * Wrapper function for grabbing a frame from the FrameGrabber
	 * and translating it into an image format
	 * @throws V4L4JException
	 */
	private void grabFrame(byte[] frame) throws V4L4JException {
		//		bb = fg.getFrame();
		//		if (grabbedFrame == null || grabbedFrame.length == 0) {
		//			grabbedFrame = new byte[bb.limit()];
		//		}
		//		bb.get(grabbedFrame);
		//instead of bb.get(grabbedFrame):
		// frameToImage(grabPitch(grabbedFrame));
		//		long start = System.nanoTime();
		frameToImage(frame);
		//		System.out.println("Time to cut frame out " + (System.nanoTime() - start)/1000000);
	}

	/**
	 * Experimental function for carving out the pitch from the image
	 * TODO: TEST IT!
	 * TODO: check if faster than curving out the pitch from the raster!
	 * @param bb - byte buffer that stores the current frame
	 * @param frame - byte array which will be copied cropped frame into
	 */
	private byte[] grabPitch(byte[] frame) {
		byte[] temp = new byte[getPitchWidth() * getPitchHeight() * 3];
		long start = System.currentTimeMillis();
		int offsetX = PITCH_START_X * 3;
		int offsetY = PITCH_START_Y;
		int pitchWidth = getPitchWidth() * 3;
		int pitchHeight = getPitchHeight();
		int imWidth = FRAME_WIDTH * 3;
		int imHeight = FRAME_HEIGHT;
		int destinationIndex = 0;

		int endOfPitch = frame.length - (imHeight - (offsetY + pitchHeight)) * imWidth - (imWidth - (pitchWidth + offsetX));
		System.out.println(imWidth * imHeight == frame.length);
		System.out.println(endOfPitch - (pitchWidth * pitchHeight + offsetX * pitchHeight + imWidth * offsetY + (imWidth - offsetX - pitchWidth)*pitchHeight));
		for (int i = (offsetY * imWidth) + offsetX; i < endOfPitch; i += imWidth) {

			System.arraycopy(frame, i, temp, destinationIndex, pitchWidth);
			destinationIndex += pitchWidth;
		}
		System.out.println("grabPitch() runtime: "+(System.currentTimeMillis()-start)+"ms");
		return temp;
	}

	/**
	 * Wrapper function for setting up video capture
	 */
	@Deprecated
	private void startCapture() {
		try {
			vd = new VideoDevice(VIDEO_CARD);
			fg = vd.getRGBFrameGrabber(FRAME_WIDTH, FRAME_HEIGHT, VIDEO_CHANNEL, V4L4JConstants.STANDARD_PAL);

			fg.startCapture();
		} catch (V4L4JException ex) {
			System.out.println("Could not retrieve the image from the Video Card!");
			ex.printStackTrace();
			stopCapture();
		}
	}

	/**
	 * Wrapper function for finalizing video capture and releasing video card
	 */
	@Deprecated
	private void stopCapture() {
		fg.stopCapture();
		vd.releaseFrameGrabber();
		vd.release();
	}

	/** this function tests the ImageProcessor on the given image
	 * @param filepath path to the image file to be read and tested
	 */
	private static void imageTestFromFile(String filepath) throws V4L4JException, IOException {
		// load image into a BufferedImage
		BufferedImage image = new BufferedImage(768, 576,
				BufferedImage.TYPE_3BYTE_BGR);
		try {
			// use a static image for now
			image = ImageIO.read(new File(filepath));
			//ImageProcessor ip = new ImageProcessor(false);
			//ip.getPosition(image.getData(), new int[]{-1, -1, -1, -1, -1, -1});
		} catch (IOException e) {
			System.out.println("Could not open the file!");
			e.printStackTrace();
		}
	}

	/**
	 * This function is used only for testing vision system.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Vision vsys = new Vision(1, false);
		vsys.start();
		//  PImage openCVFrame = vsys.frameToPImage(vsys.grabbedFrame);

		// BlobDetection blob = new BlobDetection(openCVFrame);

	}
}
