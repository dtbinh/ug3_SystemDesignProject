package vision;

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
 * @author Ben
 * @author Laurentiu
 */
public class ImageProcessor {

	public static final String DEBUG_LOC_PIC = "debug_locations.jpg";
	public static final String DEBUG_THR_PIX_PIC = "debug_thr_pix.jpg";
	public static final String DEBUG_BLOBS_PIC = "debug_blobs.png";

	// defaults values
	private final int DEF_CONN_PLATE = 7;
	private final int DEF_CONN_BALL = 3;
	private final int DEF_CONN_BLACKDOT = 6;
	private final int DEF_MINSIZE_PLATE = 1000;
	private final int DEF_MINSIZE_BALL = 25;
	private final int DEF_MINSIZE_BLACKDOT = 60;
	private final int DEF_PLATE_TYPE = 1;
	private final int DEF_THRCOUNTMAX = 12000;

	private int width;
	private int height;
	// debug/control vars
	private int debug;
	private int mode;
	private int newMode;
	private boolean bgSub;
	// coordinate extraction vars
	private int[][] coords;
	private Raster data;
	private boolean[][] binaryImg;
	private boolean[][] binaryImgBall;
	private int[] ball;
	private boolean foundBlue;
	private boolean foundYell;
	// blob/angle extraction vars
	private BlobExtraction be;
	private int connPlateValue;
	private int connBallValue;
	private int connBlackdotValue;
	private int minPlateSize;
	private int minBallSize;
	private int minBlackdotSize;
	private int plateType;
	private ArrayList<ArrayList<int[]>> blobs;
	private ArrayList<int[]> ballBlob;
	// thresholding vars
	int[] bgvals;
	int[] rgbvals;
	float[] hsbvals;
	int offset;
	int red, green, blue;
	float hue, sat, val;
	int thrCountMax;
	int thrCount;
	// correction vars
	private int cpixel[];
	// bg sub vars
	private BufferedImage bgImage;
	private Raster bgRaster;
	// debug vars
	private ColorSpace cs;
	@SuppressWarnings("unused")
	private ColorModel cm;
	private WritableRaster threshWRaster;
	private Plate plate;

	/**
	 * Creates a new ImageProcessor4 instance for identifying objects from the pitch camera.
	 * @param width - width of the frame
	 * @param height - height of the frame
	 * @param debug - set whether to launch in debug mode
	 */
	protected ImageProcessor(int width, int height, int debug) {
		this.width = width;
		this.height = height;
		// debug/control vars
		this.debug = debug;
		bgSub = false;
		//coordinate extraction vars
		coords = new int[9][2];
		binaryImg = new boolean[this.width][this.height];
		binaryImgBall = new boolean[this.width][this.height];
		// blob/angle extraction vars
		be = new BlobExtraction();
		be.setDimensions(width, height);
		connPlateValue = DEF_CONN_PLATE;
		connBallValue = DEF_CONN_BALL;
		connBlackdotValue = DEF_CONN_BLACKDOT;
		minPlateSize = DEF_MINSIZE_PLATE;
		minBallSize = DEF_MINSIZE_BALL;
		minBlackdotSize = DEF_MINSIZE_BLACKDOT;
		plateType = DEF_PLATE_TYPE;
		// thresholding vars
		bgvals = new int[width * height * 3];
		rgbvals = new int[width * height * 3];
		hsbvals = new float[3];
		offset = 0;
		red = 0; green = 0; blue = 0;
		hue = 0; sat = 0; val = 0;
		thrCountMax = DEF_THRCOUNTMAX;
		thrCount = 0;
		//
		plate = new Plate();
		// correction vars
		cpixel = new int[3];
		// bg sub vars
		bgImage = null;
		bgRaster = null;
		// debug vars
		cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

		execBGSubtraction();
	}
	
	/**
	 * This method will instruct image processing to do object detection and return their coordinates.
	 * @return Array of coordinates and angles [ballX, ballY, ballAngle, blueX, blueY, blueAngle, yellowX, yellowY, yellowAngle][0] 
	 * for uncorrected and [...][1] for corrected.
	 */
	protected int[][] getCoords() {
		mode = newMode;
		// blobs from SO
		if (mode == 2) {
			blobs = new ArrayList<ArrayList<int[]>>();
		}

		imageToBinary();
		if (thrCount > thrCountMax) {
			// if too many pixels were thresholded the frame may be broken, so don't carry on
			return coords;
		}

		be.setConnectivity(connPlateValue);
		be.setMinSize(minPlateSize);
		be.extract(binaryImg);

		for (int i = 0; i < be.getNumBlobs(); i++) {
			// blobs from SO
			if (mode == 2) {
				ArrayList<int[]> tmp = new ArrayList<int[]>();
				ArrayList<int[]> curBlob = be.getBlob(i);

				for (int p = 0; p < curBlob.size(); p++) {
					for( int q = 0; q < curBlob.get(p).length; q++) {
						tmp.add(new int[] {curBlob.get(p)[0], curBlob.get(p)[1]});
					}
				}
				blobs.add(tmp);
			}

			plate.computeProperties(be.getBlob(i), data, plateType, connBlackdotValue, minBlackdotSize);

			if (plate.getClassification() == 1) {
				// set blue robot coords
				coords[3][0] = plate.getMidpoint()[0];
				coords[4][0] = plate.getMidpoint()[1];
				coords[5][0] = plate.getAngle();
				
				coords[3][1] = plate.getMidpointCorr()[0];
				coords[4][1] = plate.getMidpointCorr()[1];
				coords[5][1] = plate.getAngleCorr();
				
				foundBlue = true;
			} else if (plate.getClassification() == 2) {
				// set yellow robot coords
				coords[6][0] = plate.getMidpoint()[0];
				coords[7][0] = plate.getMidpoint()[1];
				coords[8][0] = plate.getAngle();

				coords[6][1] = plate.getMidpointCorr()[0];
				coords[7][1] = plate.getMidpointCorr()[1];
				coords[8][1] = plate.getAngleCorr();
				
				foundYell = true;
			}
			if (foundBlue && foundYell) {
				// found both so stop
				break;
			}
		}

		be.setConnectivity(connBallValue);
		be.setMinSize(minBallSize);
		be.extract(binaryImgBall);
		// blobs from SO
		if (mode == 2) {
			ArrayList<int[]> tmp = new ArrayList<int[]>();
			for (int i = 0; i < be.getNumBlobs(); i++) {
				ArrayList<int[]> curBlob = be.getBlob(i);
				for (int p = 0; p < curBlob.size(); p++) {
					for (int q = 0; q < curBlob.get(p).length; q++) {
						tmp.add(new int[] {curBlob.get(p)[0], curBlob.get(p)[1]});
					}
				}
				blobs.add(tmp);
			}
		}

		if (be.getNumBlobs() == 1) {
			if (debug > 2) {
				System.out.println("VISION >> Got ball");
			}
			ballBlob = be.getBlob(0);

			ball = VFunctions.getMidpoint(ballBlob);
			coords[0][0] = ball[0];
			coords[1][0] = ball[1];
			coords[2][0] = 1;
			
			cpixel = BarrelDistortionCorrection.convertPixel(ball[0], ball[1]);
			coords[0][1] = cpixel[0];
			coords[1][1] = cpixel[1];
			coords[2][1] = 1;
		} else if (be.getBlob(0).size() > 0) {
			if (debug > 1) {
				System.out.println("VISION >> Num balls found: " + be.getNumBlobs());
			}

			int size = be.getBlob(0).size();

			for (int i = 1; i < be.getNumBlobs(); i++) {
				if (be.getBlob(i).size() > size) {
					size = be.getBlob(i).size();
					ball = VFunctions.getMidpoint(be.getBlob(i));
					coords[0][0] = ball[0];
					coords[1][0] = ball[1];
					coords[2][0] = 0;
					
					cpixel = BarrelDistortionCorrection.convertPixel( ball[0],  ball[1]);
					coords[0][1] = cpixel[0];
					coords[1][1] = cpixel[1];
					coords[2][1] = 0;
				}
			}
		} else {
			coords[0][0] = -1;
			coords[1][0] = -1;
			coords[2][0] = -1;
			
			coords[0][1] = -1;
			coords[1][1] = -1;
			coords[2][1] = -1;
		}
		
		// set '-1' values if we haven't found anything
		if (!foundBlue) {
			coords[3][0] = -1;
			coords[4][0] = -1;
			coords[5][0] = -1;
			
			coords[3][1] = -1;
			coords[4][1] = -1;
			coords[5][1] = -1;
		}
		if (!foundYell) {
			coords[6][0] = -1;
			coords[7][0] = -1;
			coords[8][0] = -1;
			
			coords[6][1] = -1;
			coords[7][1] = -1;
			coords[8][1] = -1;
		}

		if (debug > 2) {
			System.out.println("VISION >> Ball: " + coords[0][0] + " " + coords[1][0]);
			System.out.println("VISION >> Blue: " + coords[3][0] + " " + coords[4][0] + " " + coords[5][0]);
			System.out.println("VISION >> Yell: " + coords[6][0] + " " + coords[7][0] + " " + coords[8][0]);
		}
		
		// clear out the pointers and flags to get ready for the next run
		foundBlue = false;
		foundYell = false;

		return coords;
	}
	
	/**
	 * Converts a frame to a binary image based on pixel thresholds.
	 */
	private void imageToBinary() {
		// reset vars
		thrCount = 0;
		
		// thresholding from SO
		if (mode == 3) {
			threshWRaster = data.createCompatibleWritableRaster();
		}

		data.getPixels(0, 0, width, height, rgbvals);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				offset = (y * 3 * width) + (x * 3);
				red = rgbvals[offset];
				green = rgbvals[offset + 1];
				blue = rgbvals[offset + 2];

				if (bgSub) {
					// WITH BACKGROUND SUBTRACTION
					if ((Math.abs(red - bgvals[offset]) > 40 || Math.abs(green - bgvals[offset + 1]) > 40 || Math.abs(blue - bgvals[offset + 2]) > 20)) {
						// PLATE
						if (green >= 160 && !(red >= 245 && green >= 245 && blue >= 235)) {
							Color.RGBtoHSB(red, green, blue, hsbvals);
							hue = hsbvals[0];
							
							// HUE 50-220
							if ((hue >= 0.13888 && hue <= 0.61111)) {
								binaryImg[x][y] = true;
								thrCount++;
							} else {
								binaryImg[x][y] = false;
							}
						} else {
							binaryImg[x][y] = false;
						}

						// BALL
						if (red >= 140 && red <= 255 && green >= 0 && green <= 130 && blue >= 0 && blue <= 130) {
							binaryImgBall[x][y] = true;
							thrCount++;
						} else {
							binaryImgBall[x][y] = false;
						}
					} else {
						// NO BACKGROUND SUBTRACTION
						Color.RGBtoHSB(red, green, blue, hsbvals);
						hue = hsbvals[0];
						sat = hsbvals[1];
						val = hsbvals[2];

						// TODO thresholds will need changing
						// PLATE
						if (val >= 0.5 && red >= 80 && red <= 230 && green >= 180 && (hue >= 0.22222 && hue <= 0.5)) {
							binaryImg[x][y] = true;
						} else {
							binaryImg[x][y] = false;
						}

						// BALL
						if (red >= 240 && (hue >= 0.94444 || hue <= 0.05555) && sat >= 0.5 && val >= 0.7) {
							binaryImgBall[x][y] = true;
						} else {
							binaryImgBall[x][y] = false;
						}
					}
					// thresholding from SO
					if (mode == 3) {
						if ((binaryImg[x][y]) || (binaryImg[x][y] && binaryImgBall[x][y])) {
							threshWRaster.setPixel(x, y, new int[] {255, 255, 255});
						} else if (binaryImgBall[x][y]) {
							threshWRaster.setPixel(x, y, new int[] {255, 0, 0});
						} else {
							threshWRaster.setPixel(x, y, new int[] {0, 0, 0});
						}
					}
				}
			}
		}
	}

	/**
	 * Execute background subtraction. The image used should be named as "bgImage.png"
	 * and stored in the root directory of the project. It should be taken with mPlayer,
	 * as cropping is done by the method, and there should be no objects on the pitch.
	 * This method can be used on the run as well. Use with care.
	 */
	public void execBGSubtraction() {
		try {
			bgImage = ImageIO.read(new File("bgImage.png"));
			// cut the image
			bgImage = bgImage.getSubimage(Vision.PITCH_START_X, Vision.PITCH_START_Y, (Vision.PITCH_END_X - Vision.PITCH_START_X), (Vision.PITCH_END_Y - Vision.PITCH_START_Y));
			bgRaster = bgImage.getRaster();
			bgRaster.getPixels(0, 0, bgImage.getWidth(), bgImage.getHeight(), bgvals);
			bgSub = true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("VISION ERROR >> Could not load background subtraction image!");
			bgSub = false;
		}
	}
	
	/*
	 * ======================
	 * GETTERS/SETTERS/RESETS
	 * ======================
	 */
	
	/**
	 * Updates the frame data on which processing will take place.
	 * @param data
	 */
	protected void setData(Raster data) {
		this.data = data;
	}

	/**
	 * Check what debug level is set.
	 * @return debug status
	 */
	public int debugLevel() {
		return debug;
	}

	/**
	 * Change the debug level.
	 * @param debug
	 */
	public void setDebug(int debug) {
		if (debug >= 0)
			this.debug = debug;
	}

	/**
	 * Only System Overview should call this to tell IP what kind of 
	 * drawing needs to take place.
	 * @param mode
	 */
	public void setMode(int mode) {
		this.newMode = mode;
	}
	
	/**
	 * @return The maximum number of thresholded pixels for discarding frames.
	 */
	public int getThrCountMax() {
		return thrCountMax;
	}
	
	/**
	 * Set the maximum number of pixels to be thresholded for discarding a frame 
	 * to prevent freeze-ups on extreme conditions (ie camera flash).
	 * @param value
	 */
	public void setThrCountMax(int value) {
		if (value > 0)
			thrCountMax = value;
	}
	
	/**
	 * Reset to the default value.
	 */
	public void resetThrCountMax() {
		thrCountMax = DEF_THRCOUNTMAX;
	}

	/**
	 * Check if background subtraction is on.
	 * @return Background subtraction status.
	 */
	public boolean isBGSub() {
		return bgSub;
	}

	/**
	 * Turn background subtraction on or off.
	 * @param bgSub - value to change to
	 */
	public void setBGSub(boolean bgSub) {
		if (bgImage != null)
			this.bgSub = bgSub;
	}

	/**
	 * @return Level of connectivity for blobbing.
	 */
	public int getConnectivity(int type) {
		if (type == 0) {
			return connPlateValue;
		} else if (type == 1) {
			return connBallValue;
		} else {
			return connBlackdotValue;
		}
	}

	/**
	 * Changes the level of connectivity for blobbing. Lower values
	 * work better if blobs get disconnected, higher values if too
	 * much noise is being picked up.
	 * @param value
	 */
	public void setConnectivity(int type, int value) {
		if (value < 1 || value > 8) {
			return;
		}
		
		if (type == 0) {
			connPlateValue = value;
		} else if (type == 1) {
			connBallValue = value;
		} else if (type == 2) {
			connBlackdotValue = value;
		}
	}

	/**
	 * Resets the level of blobbing connectivity to the default value.
	 */
	public void resetConnectivity(int type) {
		if (type == 0) {
			connPlateValue = DEF_CONN_PLATE;
		} else if (type == 1) {
			connBallValue = DEF_CONN_BALL;
		} else if (type == 2) {
			connBlackdotValue = DEF_CONN_BLACKDOT;
		}
	}

	/**
	 * @return Minimum size for blob filtering.
	 */
	public int getMinSize(int type) {
		if (type == 0) {
			return minPlateSize;
		} else if (type == 1) {
			return minBallSize;
		} else {
			return minBlackdotSize;
		}
	}

	/**
	 * Changes the size for blob filtering.
	 * @param value
	 */
	public void setMinSize(int type, int value) {
		if (value <= 0) {
			return;
		}
		
		if (type == 0) {
			minPlateSize = value;
		} else if (type == 1) {
			minBallSize = value;
		} else if (type == 2) {
			minBlackdotSize = value;
		}
	}

	/**
	 * Resets the size for blob filtering.
	 */
	public void resetMinSize(int type) {
		if (type == 0) {
			minPlateSize = DEF_MINSIZE_PLATE;
		} else if (type == 1) {
			minBallSize = DEF_MINSIZE_BALL;
		} else if (type == 2) {
			minBlackdotSize = DEF_MINSIZE_BLACKDOT;
		}
	}
	
	/**
	 * @return The Plate type currently in use.
	 */
	public int getPlateType() {
		return plateType;
	}
	
	/**
	 * Changes which type if Plate to use for angle calculation. '0' for hybrid, '1' for Ben's, and 
	 * '2' for Martin's.
	 * @param type
	 */
	public void setPlateType(int type) {
		if (type >= 0 && type <= 2) {
			plateType = type;
		}
	}
	
	/**
	 * Changes the type of Plate to the default value.
	 */
	public void resetPlateType() {
		plateType = DEF_PLATE_TYPE;
	}

	/**
	 * @return Raster where thresholded pixels have been marked with white.
	 */
	public WritableRaster getDebugThreshRaster() {
		if (mode == 3) {
			return threshWRaster;
		} else {
			return null;
		}
	}

	/**
	 * @return Points contained within the blobs.
	 */
	public ArrayList<ArrayList<int[]>> getDebugPlates(){
		if (mode == 2 && blobs.size() > 0) {
			return blobs;
		} else {
			return null;
		}
	}
	
	/*
	 * ===============
	 * THRESHOLD NOTES
	 * ===============
	 * PITCH (R, G, B | H, S, V)
	 * green (40-90, 70-120, 50-110 | 120-150, 35-50, 30-60)
	 * white (250-255, 250-255, 200-255 | 340-80, 0-30, 90-100)
	 * PLATE (R, G, B | H, S, V)
	 * green (90-280, 200-255, 140-220 | 120-160, 20-70, 80-100)
	 * yellow (230-255, 200-255, 80-240 | 40-80, 10-80, 80-100)
	 * blue (100-180, 180-255, 200-255 | 150-220, 30-70, 70-100)
	 */
}
