package vision.old;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Collections;

public class PlateM {

	private ArrayList<int[]> points;
	private Raster data;
	private Polygon polygon;
	private int width;
	private int height;
	private boolean[][] area;
	private int midpoint[];
	private int classification = -1;	// 0 for not a plate, 1 for blue, 2 for yellow
	private int angle = -1;
	
	public PlateM(ArrayList<int[]> points, Raster data, int debug) {
		this.points = new ArrayList<int[]>(points);
		this.data = data;

		// create a polygon out of the points
		int[] pointsX = new int[points.size()];
		int[] pointsY = new int[points.size()];
		for (int i = 0; i < points.size(); i++) {
			pointsX[i] = points.get(i)[0];
			pointsY[i] = points.get(i)[1];
		}
		this.polygon = new Polygon(pointsX, pointsY, points.size());

		this.width = (int) polygon.getBounds().getWidth();
		this.height = (int) polygon.getBounds().getHeight();
		this.area = new boolean[width][height];
		
		//calcMidpoint(points);
		polygonArea();
		connectNeighbours();
		calcPlateColor();
		calcAngle();
	}

	/**
	 * Calculates the midpoint of a Plate.
	 * @deprecated
	 */
	@SuppressWarnings("unused")
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
				if (polygon.intersects(x, y, 1, 1)) {
					area[x-bounds.x][y-bounds.y] = true;
				}
			}
		}
	}

	/**
	 * Scan through the area and connects disconnected pixels. 
	 * Will also recalculate the midpoint more precisely.
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
	 * @return True when the pixel at the coordinates has enough neighbours.
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
	 * Computes the angle of this Plate by only thresholding pixels which 
	 * intersect the polygon representing the points of the Plate.
	 */
	private void calcAngle() {
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

		for (int x = bounds.x; x < bounds.getWidth()+bounds.x; x++) {
			for (int y = bounds.y; y < bounds.getHeight()+bounds.y; y++) {
				// check if this pixel is inside the polygon (not the whole bounding box!)		
				if (area[x-bounds.x][y-bounds.y]) {
					// extract and save pixel values
					data.getPixel(x, y, pixeldata);
					Color.RGBtoHSB(pixeldata[0], pixeldata[1], pixeldata[2], hsbdata);

					// do the thresholding here
					// HUE: 340-180; SAT: 0-40; VAL: 20-60
					if ((hsbdata[0] >= 0.94444 || hsbdata[0] <= 0.5) && (hsbdata[1] >= 0 && hsbdata[1] <= 0.4) && (hsbdata[2] >= 0.2 && hsbdata[2] <= 0.6)) {
						xs.add(x);
						ys.add(y);
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
			dot[0] = median(xs);
			dot[1] = median(ys);
		}

		angle = Math.round((float) Math.toDegrees(Math.atan2(midpoint[1] - dot[1], midpoint[0] - dot[0])));
		angle = Math.abs(360 - angle) % 360;
	}

	/**
	 * Calculates the median from an array of integer values.
	 * @param values
	 * @return Median value.
	 */
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
	
	public int getClassification() {
		return classification;
	}
	
	public int[] getMidpoint() {
		return midpoint;
	}
	
	public int getAngle() {
		return angle;
	}
	
	public ArrayList<int[]> getPoints() {
		return points;
	}
}
