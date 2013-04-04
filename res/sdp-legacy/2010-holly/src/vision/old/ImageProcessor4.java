package vision.old;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * @author Martin
 * @author Laurentiu
 */
public class ImageProcessor4 {

	public static final String DEBUG_LOC_PIC = "debug_locations.jpg";
	public static final String DEBUG_THR_PIX_PIC = "debug_thr_pix.jpg";
	public static final String DEBUG_BLOBS_PIC = "debug_blobs.png";
	private final int DEF_PLATE = 1;

	private int width;
	private int height;
	private int debug;
	private boolean thrDrawing;
	private boolean blobDrawing;
	private boolean bgSub;
	private int plateMode;
	private int[] coords;
	private Raster data;
	private boolean[][] binaryImg;
	private boolean[][] binaryImgBall;
	private BlobExtraction bePlate;
	private BlobExtraction beBall;
	private ArrayList<Plate> plates;
	private ArrayList<Plate> plates_;
	private boolean foundBlue;
	private boolean foundYell;
	private int blueOldAngle;
	private int yellowOldAngle;
	// thresholding vars
	private boolean matches;
	private boolean matchesBall;
	private int[] rgbvals;
	private int red, green, blue;
	private float[] hsbvals;
	private float hue, sat, val;
	private float[] bgvals;
	// ball vars
	private ArrayList<int[]> ball;
	private ArrayList<int[]> ball_;
	private int sumXBall;
	private int sumYBall;

	// bg sub stuff
	BufferedImage bgImage;
	//HSVStructure bgRaster;
	Raster bgRaster;

	// debug stuff
	private ColorSpace cs;
	private ColorModel cm;
	private WritableRaster threshWRaster;
	private BufferedImage debugThreshImage;
	
	/**
	 * Creates a new ImageProcessor4 instance for identifying objects from the pitch camera.
	 * @param width - width of the frame
	 * @param height - height of the frame
	 * @param debug - set whether to launch in debug mode
	 */
	public ImageProcessor4(int width, int height, int debug) {
		this.width = width;
		this.height = height;
		this.debug = debug;
		this.thrDrawing = false;
		this.blobDrawing = false;
		this.bgSub = false;
		this.plateMode = DEF_PLATE;
		this.coords = new int[9];
		//this.result = new int[3];
		this.data = null;
		this.binaryImg = new boolean[this.width][this.height];
		this.binaryImgBall = new boolean[this.width][this.height];
		this.bePlate = new BlobExtraction(this.width, this.height, 800, 7);
		this.beBall = new BlobExtraction(this.width, this.height, 10, 5);
		this.plates = new ArrayList<Plate>();
		this.plates_ = null;
		this.foundBlue = false;
		this.foundYell = false;
		this.blueOldAngle = -100;
		this.yellowOldAngle = -100;
		// thresholding vars
		matches = false;
		matchesBall = false;
		rgbvals = new int[3];
		red = 0; green = 0; blue = 0;
		hsbvals = new float[3];
		hue = 0; sat = 0; val = 0;
		bgvals = new float[3];
		// ball vars
		ball = new ArrayList<int[]>();
		ball_ = null;
		sumXBall = 0;
		sumYBall = 0;
		
		this.bgImage = null;
		this.bgRaster = null;
		execBGSubtraction();

		// debug stuff
		cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	}
	
	/**
	 * Wrapper function; provides data to the Vision.
	 * @param data - raster containing the frame
	 * @param coords - coordinates of the ball, blue robot, and yellow robot (x,y)
	 * @return Current position of the recognised objects.
	 */
	protected int[] getPosition(Raster data, int[] oldcoords) {
		// update the pointer to the current frame
		this.data = data;

		// get the plates
		collectPlates(data);
		Plate plate;
		for (int i = 0; i < plates.size(); i++) {
			plate = plates.get(i);
			if (plate.getClassification() == 1) {
				// set blue robot coords
				coords[3] = plate.getMidpoint()[0];
				coords[4] = plate.getMidpoint()[1];
				if (blueOldAngle == -100) {
					coords[5] = plate.getAngle();
					blueOldAngle = coords[5];
				} else {
					if (plate.getAngle() != -1) {
						coords[5] = plate.getAngle();
					} else {
						coords[5] = blueOldAngle;
					}
				}
				foundBlue = true;
			} else if (plate.getClassification() == 2) {
				// set yellow robot coords
				coords[6] = plate.getMidpoint()[0];
				coords[7] = plate.getMidpoint()[1];
				if (yellowOldAngle == -100) {
					coords[8] = plate.getAngle();
					yellowOldAngle = coords[8];
				} else {
					if (plate.getAngle() != -1) {
						coords[8] = plate.getAngle();
					} else {
						coords[8] = yellowOldAngle;
					}
				}
				foundYell = true;
			}
			if (foundBlue && foundYell) {
				// found both so stop
				break;
			}
		}
		// set '-1' values if we haven't found anything
		if (!foundBlue) {
			coords[3] = -1;
			coords[4] = -1;
			coords[5] = -1;
		}
		if (!foundYell) {
			coords[6] = -1;
			coords[7] = -1;
			coords[8] = -1;
		}

		if (debug > 0) {
			System.out.println("VISION >> Ball: " + coords[0] + " " + coords[1]);
			System.out.println("VISION >> Blue: " + coords[3] + " " + coords[4] + " " + coords[5]);
			System.out.println("VISION >> Yell: " + coords[6] + " " + coords[7] + " " + coords[8]);
		}

		// clear out the pointers and flags to get ready for the next run
		plates.clear();
		foundBlue = false;
		foundYell = false;

		return coords;
	}
	
	/**
	 * Main method for finding the plates in a frame.
	 * If debug is set to 1 it will draw thresholded pixels to {@literal debugThreshImage}. 
	 * If debug is set to 2 the image will be saved to {@literal DEBUG_THR_PIX_PIC}.
	 * @param raster - raster containing the frame
	 */
	private void collectPlates(Raster raster) {
		// bugfix when being run from outside of the wrapper method
		if (data == null) {
			this.data = raster;
		}
		
		// convert the frame to a binary image
		imageToBinary();

		// get the blobs
		bePlate.extract(binaryImg);
		// extract the blobs to Plates
		for (int i = 0; i < bePlate.getNumBlobs(); i++) {
			plates.add(new Plate(plateMode, bePlate.getBlob(i), data, debug));
		}
		
		// do the ball
		beBall.extract(binaryImgBall);
		for (int i = 0; i < beBall.getNumBlobs(); i++) {
			// choose the biggest blob (hopefully the ball)
			if (ball.size() > beBall.getBlob(i).size()) {
				continue;
			} else {
				ball = beBall.getBlob(i);
			}
		}	
		
		// calculate the midpoint
		for (int i = 0; i < ball.size(); i++) {
			sumXBall += ball.get(i)[0];
			sumYBall += ball.get(i)[1];
		}
		if (sumXBall != 0 && sumYBall != 0) {
			coords[0] = sumXBall / ball.size();
			coords[1] = sumYBall / ball.size();
			coords[2] = -1;
		} else {
			coords[0] = -1;
			coords[1] = -1;
			coords[2] = -1;
		}
		
		if (blobDrawing) {
			ball_ = new ArrayList<int[]>(ball);
			plates_ = new ArrayList<Plate>(plates);
		}
		
		ball.clear();
		sumXBall = 0;
		sumYBall = 0;
	}

	/**
	 * Converts a frame to a binary image based on pixel thresholds.
	 */
	private void imageToBinary() {
		if (debug > 0) {
			threshWRaster = data.createCompatibleWritableRaster();
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data.getPixel(x, y, rgbvals);
				red = rgbvals[0];
				green = rgbvals[1];
				blue = rgbvals[2];
				Color.RGBtoHSB(red, green, blue, hsbvals);
				hue = hsbvals[0];
				sat = hsbvals[1];
				val = hsbvals[2];
				
				if (bgSub) {
					// if doing background subtraction
					bgRaster.getPixel(x, y, bgvals);
					if (!(Math.abs(red - bgvals[0]) < 10 && Math.abs(blue - bgvals[1]) < 10 && Math.abs(green - bgvals[2]) < 10)) {
						// difference is big enough for the pixel to be in the foreground
						// PLATES == HUE: 50-220 (add 'hsbvals[1] >= 0.05' to remove yellow noise)
						if (green >= 180 && (hue >= 0.13888 && hue <= 0.61111) && !(red >= 240 && green >= 240 && blue >= 240)) {
							matches = true;
						// BALL == HUE: 340-20; SAT: 50-100; VAL: 50-100
						} else if (red >= 240 && (hue >= 0.94444 || hue <= 0.05555) && sat >= 0.5 && val >= 0.7) {
							matchesBall = true;
						}
					}
				} else {
					// stricter thresholds when not using subtraction
					// PLATES == VAL: 50-100; HUE: 80-180
					if (val >= 0.5 && red >= 80 && red <= 230 && green >= 180 && (hue >= 0.22222 && hue <= 0.5)) {
						matches = true;
					// BALL == HUE: 340-20; SAT: 50-100; VAL: 50-100
					} else if (red >= 240 && (hue >= 0.94444 || hue <= 0.05555) && sat >= 0.5 && val >= 0.7) {
						matchesBall = true;
					}
				}
				
				if (matches && !matchesBall) {
					binaryImg[x][y] = true;
					binaryImgBall[x][y] = false;
					if (thrDrawing)
						threshWRaster.setPixel(x, y, new int[] {255, 255, 255});
				}
				if (matchesBall && !matches) {
					binaryImgBall[x][y] = true;
					binaryImg[x][y] = false;
					if (thrDrawing)
						threshWRaster.setPixel(x, y, new int[] {255, 0, 0});
				} else if (!matches && !matchesBall) {
					binaryImg[x][y] = false;
					binaryImgBall[x][y] = false;
					if (thrDrawing)
						threshWRaster.setPixel(x, y, new int[] {0, 0, 0});
				}
				
				// reset flags
				matches = false;
				matchesBall = false;
			}
		}
		
		if (debug > 1) {
			try {
				debugThreshImage = new BufferedImage(cm, threshWRaster, false, null);
				ImageIO.write(debugThreshImage, "jpg", new File(DEBUG_THR_PIX_PIC));
			} catch (IOException e) {
				System.out.println("VISION ERROR >> Could not save " + DEBUG_THR_PIX_PIC);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Execute background subtraction. The image used should be named as "bgImage.png" 
	 * and stored in the root directory of the project. It should be taken with mPlayer, 
	 * as cropping is done by the method, and there should be no objects on the pitch. 
	 * This method can be used on the run as well. Use with care.
	 */
	protected void execBGSubtraction() {
		try {
			bgImage = ImageIO.read(new File("bgImage.png"));
			// cut the image
			bgImage = bgImage.getSubimage(Vision.PITCH_START_X, Vision.PITCH_START_Y, (Vision.PITCH_END_X - Vision.PITCH_START_X), (Vision.PITCH_END_Y - Vision.PITCH_START_Y));
			//bgRaster = new HSVStructure(bgImage.getRaster());
			bgRaster = bgImage.getRaster();
			bgSub = true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("VISION ERROR >> Could not load background subtraction image!");
			bgSub = false;
		}
	}

	/**
	 * Check what debug level is set.
	 * @return debug status
	 */
	protected int debugLevel() {
		return debug;
	}

	/**
	 * Change the debug level.
	 * @param debug
	 */
	protected void setDebug(int debug) {
		if (debug == 0)
			thrDrawing = false;
		this.debug = debug;
	}
	
	/**
	 * Set if threshold drawing should take place
	 * @param value
	 */
	protected void setThrDrawing(boolean value) {
		this.thrDrawing = value;
	}
	
	/**
	 * Set if objects with blobs should be exported for drawing out
	 * @param value
	 */
	protected void setBlobDrawing(boolean value) {
		this.blobDrawing = value;
	}
	
	/**
	 * Check if background subtraction is on.
	 * @return Background subtraction status.
	 */
	protected boolean isBGSub() {
		return bgSub;
	}
	
	/**
	 * Turn background subtraction on or off.
	 * @param bgSub - value to change to
	 */
	protected void setBGSub(boolean bgSub) {
		if (bgImage != null)
			this.bgSub = bgSub;
	}
	
	/**
	 * @return Which Plate mode is being used.
	 */
	protected int getPlateMode() {
		return plateMode;
	}
	
	/**
	 * Change the plate mode.
	 * @param plate - value to change to
	 */
	protected void setPlateMode(int mode) {
		if (mode >= 0 && mode <= 2)
			this.plateMode = mode;
	}
	
	/**
	 * @param type - 0 for plates, 1 for ball
	 * @return Level of connectivity for blobbing.
	 */
	protected int getConnectivity(int type) {
		if (type == 0) {
			return bePlate.getConnectivity();
		} else {
			return beBall.getConnectivity();
		}
	}
	
	/**
	 * Changes the level of connectivity for blobbing. Lower values 
	 * work better if blobs get disconnected, higher values if too 
	 * much noise is being picked up.
	 * @param type - 0 for plates, 1 for ball
	 * @param value
	 */
	protected void setConnectivity(int type, int value) {
		if (type == 0) {
			bePlate.setConnectivity(value);
		} else {
			beBall.setConnectivity(value);
		}
	}
	
	/**
	 * Resets the level of blobbing connectivity to the default value. 
	 * @param type - 0 for plates, 1 for ball
	 */
	protected void resetConnectivity(int type) {
		if (type == 0) {
			bePlate.resetConnectivity();
		} else {
			beBall.resetConnectivity();
		}
	}

	/**
	 * @return Raster where thresholded pixels have been marked with white.
	 */
	protected WritableRaster getDebugThreshRaster() {
		return threshWRaster;
	}
	
	/**
	 * @return ArrayList of Plates.
	 */
	protected ArrayList<Plate> getDebugPlates() {
		return plates_;
	}
	
	/**
	 * @return ArrayList of the ball points.
	 */
	protected ArrayList<int[]> getDebugBall() {
		return ball_;
	}

	public static void main(String[] args) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		try {
			//image = ImageIO.read(new File("src/img/extremes/shot0008.png"));
			image = ImageIO.read(new File("src/img/shot0010.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Raster data = image.getData();

		ImageProcessor4 ip = new ImageProcessor4(data.getWidth(), data.getHeight(), 0);

		while (true) {
			long start = System.currentTimeMillis();
			ip.getPosition(data, null);
			System.out.println((System.currentTimeMillis() - start) + " ms");
			System.out.println("FPS: " + 1000 / (System.currentTimeMillis() - start));
		}
	}
}
