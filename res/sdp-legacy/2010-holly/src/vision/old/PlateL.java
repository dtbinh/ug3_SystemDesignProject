/**
 * 
 */
package vision.old;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
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
import java.util.Collections;

import javax.imageio.ImageIO;

/**
 * @author Laurentiu
 */
public class PlateL {

		private ArrayList<int[]> points;
		private Raster data;
		private int debug;
		private Polygon polygon;
		private int width;
		private int height;
		private boolean[][] area;
		private int midpoint[];
		private int classification = -1;	// 0 for not a plate, 1 for blue, 2 for yellow
		private int angle = -1;

		// debug stuff
		private ColorSpace cs;
		private ColorModel cm;
		private WritableRaster plateWRaster;
		private WritableRaster threshWRaster;
		private BufferedImage debugPlateImage;
		private BufferedImage debugThreshImage;
		private int[] pointsX;
		private int[] pointsY;

		/**
		 * Creates a new Plate object given a set of points and a reference
		 * to a frame. The location (aka midpoint) of this plate is calculated
		 * on the object's creation.
		 * @param points - array of points which belong to this Plate's blob
		 * @param data - reference to the frame for computation purposes
		 * @param debug - set whether to launch in debug mode
		 */
		public PlateL(ArrayList<int[]> points, Raster data, int debug) {
			this.points = new ArrayList<int[]>(points);
			this.data = data;
			this.debug = debug;

			// create a polygon out of the points
			pointsX = new int[points.size()];
			pointsY = new int[points.size()];
			for (int i = 0; i < points.size(); i++) {
				pointsX[i] = points.get(i)[0];
				pointsY[i] = points.get(i)[1];
			}
			this.polygon = new Polygon(pointsX, pointsY, points.size());

			this.width = (int) polygon.getBounds().getWidth();
			this.height = (int) polygon.getBounds().getHeight();
			this.area = new boolean[width][height];

			calcMidpoint(points);
			calcPlateColor();
			polygonArea();
			connectNeighbours();
			calcAngle2();

			if (debug > 1) {
				cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
				cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
				threshWRaster = data.createCompatibleWritableRaster();
				threshWRaster.setDataElements(0, 0, data);
				debugThreshImage = new BufferedImage(cm, threshWRaster, false, null);
				plateWRaster = data.createCompatibleWritableRaster();
				plateWRaster.setDataElements(0, 0, data);
				debugPlateImage = new BufferedImage(cm, plateWRaster, false, null);
			}
			if (debug > 1) {
				try {
					ImageIO.write(debugThreshImage, "png", new File("angle_thresh_" + this.toString() + ".png"));
					ImageIO.write(debugPlateImage, "png", new File("angle_plate_" + this.toString() + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Calculates the midpoint of a Plate. Midpoint gets recalculated in 
		 * connectNeighbours() called from computeProperties() which will provide 
		 * a more precise midpoint.
		 */
		private void calcMidpoint(ArrayList<int[]> points) {
			int sumx = 0, sumy = 0, i = 0;
			for (i = 0; i < points.size(); i ++) {
				sumx += points.get(i)[0];
				sumy += points.get(i)[1];
			}

			if (i == 0) {
				midpoint =  new int[] {-1, -1};
			} else {
				midpoint =  new int[] {sumx / i, sumy / i};
			}
		}

		/**
		 * Checks the pixel in the midpoint of this Plate for its hue 
		 * properties in order to classify it as blue or yellow.
		 */
		private void calcPlateColor(){
			int[] pix = new int[3];
			float[] hsv = new float[3];
			data.getPixel(midpoint[0], midpoint[1], pix);
			Color.RGBtoHSB(pix[0], pix[1], pix[2], hsv);
			float hue = hsv[0];

			if (hue >= 0.11111 && hue <= 0.22222) {
				//yellow (40 - 80)
				this.classification = 2;
			} else if (hue >= 0.38888 && hue <= 0.61111) {
				//blue (140 - 220)
				this.classification = 1;
			} else {
				// no plate
				this.classification = 0;
			}
		}

		/**
		 * Saves the area of the polygon into a 2D array of booleans 
		 * for faster checking since AWT's intersect() is slow.
		 */
		private void polygonArea() {
			Rectangle bounds = polygon.getBounds();

			for (int x = bounds.x; x < bounds.getWidth()+bounds.x; x++) { 
				for (int y = bounds.y; y < bounds.getHeight()+bounds.y; y++) {
					//if (polygon.intersects(x, y, 1, 1)) {
					  if(polygon_intersection(4, pointsX, pointsY, x, y)){	
						area[x-bounds.x][y-bounds.y] = true;
					}
				}
			}
		}
		public boolean polygon_intersection(int nr_vert, int[] pointsX, int[] pointsY, int x, int y)
		{
			  int i, j;
			  boolean c = false;
			  for (i = 0, j = nr_vert-1; i < nr_vert; j = i++) {
			    if ( ((pointsY[i]>y) != (pointsY[j]>y)) &&
				 (x < (pointsX[j]-pointsX[i]) * (y-pointsY[i]) / (pointsY[j]-pointsY[i]) + pointsX[i]) )
			       c = !c;
			  }
			  return c;
		}

		/**
		 * Scan through the area and connect pixels. Will also recalculate 
		 * the midpoint more precisely.
		 */
		private void connectNeighbours() {
			Rectangle bounds = polygon.getBounds(); // used for midpoint

			int midx = 0, midy = 0;
			for (int x = 0; x < width; x++) {
				midy = 0;
				for (int y = 0; y <  height; y++) {
					if (!area[x][y]) {
						area[x][y] = checkNeighbours(x, y);
					}
					midy += y;
				}
				midx += x;
			}

			midx /= width;
			midy /= height;
			midx += bounds.x;
			midy += bounds.y;

			midpoint = new int[] {midx, midy};
		}

		/**
		 * Check neighbours of a pixel for connectedness.
		 * @param x - x coordinate
		 * @param y - y coordinate
		 * @return True when the pixel at the coordinates has enough neighbours to be included.
		 */
		private boolean checkNeighbours(int x, int y) {
			int count = 0;
			int myx = x, myy = y;

			if (((myx - 1) > 0 && (myx + 1) < width) && ((myy - 1) > 0 && (myy + 1) < height)) {
				// S
				if (area[x][y - 1]) {
					count++;
				}
				// W
				if (area[x-1][y]) {
					count++;
				}
				// E
				if (area[x + 1][y]) {
					count++;
				}
				// N
				if (area[x][y + 1]) {
					count++;
				}
				// NE
				if (area[x+1][y + 1]) {
					count++;
				}
				// SW
				if (area[x-1][y - 1]) {
					count++;
				}
				// NW
				if (area[x-1][y + 1]) {
					count++;
				}
				// SE
				if (area[x+1][y - 1]) {
					count++;
				}
			}
			if (count > 4) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Computes the angle of this Plate only by thresholding pixels which 
		 * intersect the polygon representing the points of the Plate.
		 */
		private void calcAngle2() {
			// get the bounding rectangle
			Rectangle bounds = polygon.getBounds();
			// to hold thresholded pixels
			ArrayList<Integer> xs = new ArrayList<Integer>();
			ArrayList<Integer> ys = new ArrayList<Integer>();
			// for the black dot midpoint
			int[] dot = new int[2];
			int size = 0;
			// pixel values
			int[] pixeldata = new int[3];
			float[] hsbdata = new float[3];
			int hue = 0, sat = 0, bright = 0;

			for (int x = bounds.x; x < bounds.getWidth()+bounds.x; x++) {
				for (int y = bounds.y; y < bounds.getHeight()+bounds.y; y++) {
					// check if this pixel is inside the polygon (not the whole bounding box!)		
					if (area[x-bounds.x][y-bounds.y]) {
						if (debug > 1)
							plateWRaster.setPixel(x, y, new int[] {255, 0, 0});
						// extract and save pixel values
						data.getPixel(x, y, pixeldata);
						Color.RGBtoHSB(pixeldata[0], pixeldata[1], pixeldata[2], hsbdata);
						hue = Math.round(hsbdata[0] * 360);
						sat = Math.round(hsbdata[1] * 100);
						bright = Math.round(hsbdata[2] * 100);

						// do the thresholding here (maybe do more reliable blobbing later)
						if ((hue >= 340 || hue <= 180) && (sat >= 0 && sat <= 40) && (bright >= 20 && bright <= 60)) {
							if (debug > 1)
								threshWRaster.setPixel(x, y, new int[] {255, 0, 0});
							xs.add(x);
							ys.add(y);
							//dot[0] += x;
							//dot[1] += y;
							size++;
						}
					}
				}
			}

			// calculate midpoint of the black dot
			if (size == 0) {
				angle = -1;
				return;
			} else {
				//dot[0] = dot[0] / size;
				//dot[1] = dot[1] / size;
				dot[0] = median(xs);
				dot[1] = median(ys);
			}

			angle = Math.round((float) Math.toDegrees(Math.atan2(midpoint[1] - dot[1], midpoint[0] - dot[0])));
			angle = Math.abs(360 - angle) % 360;
		}

		private int median(ArrayList<Integer> values) {
			Collections.sort(values);

			if (values.size() % 2 == 1) {
				return values.get((values.size()+1)/2-1);
			} else {
				int lower = values.get(values.size()/2-1);
				int upper = values.get(values.size()/2);
				return (int) ((lower + upper) / 2.0);
			}	
		}

		/**
		 * Returns the midpoint of the plate.
		 * @return Midpoint in an integer {x, y} array.
		 */
		public int[] getMidpoint() {
			return midpoint;
		}

		/**
		 * For accessing the list of points.
		 * @return A reference to the list of points.
		 */
		public ArrayList<int[]> getPoints() {
			return points;
		}

		/**
		 * To check the number of points that this Plate instance contains.
		 * @return Integer number of points.
		 */
		public int getPointsSize() {
			return points.size();
		}

		/**
		 * Return 0 for not a valid plate, 1 for blue, and 2 for yellow. 
		 * @return Classification integer. 
		 */
		public int getClassification() {
			return classification;
		}

		/**
		 * For accessing the angle of this plate which has been computed 
		 * during the object's creation.
		 * @return Angle in degree integer format.
		 */
		public int getAngle() {
			return angle;
		}

		/**
		 * @return Integer width of the bounding box representing this Plate.
		 */
		public int getWidth() {
			return (int) polygon.getBounds().getWidth();
		}

		/**
		 * @return Integer height of the bounding box representing this Plate.
		 */
		public int getHeight() {
			return (int) polygon.getBounds().getHeight();
		}
	}
