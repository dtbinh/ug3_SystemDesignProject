package vision.old;

import java.awt.Color;
import java.awt.image.Raster;
import java.util.ArrayList;

public class PlateB {

	private ArrayList<int[]> points;
	private Raster data;
	private boolean[][] plateInverted;
	private int width;
	private int height;
	private int midpoint[];
	private int classification = -1;	// 0 for not a plate, 1 for blue, 2 for yellow
	private int angle = -1;
	private int startX, startY, endX, endY;
	private BlobExtraction be;
	
	public PlateB(ArrayList<int[]> points, Raster data, int debug) {
		this.points = new ArrayList<int[]>(points);
		this.data = data;

		getBoundingBox();
		calcPlateColor();
		invertPlate();
	}

	/**
	 * Checks the pixel in the midpoint of this Plate for its hue 
	 * properties in order to classify it as blue or yellow.
	 */
	private void calcPlateColor() {
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
	 * Inverts the points of the blob to find the ones which are not contained 
	 * within its boundaries.
	 */
	private void invertPlate() {
		plateInverted = new boolean[width][height];
		int[] curPoint;
		int[] nextPoint;
		int y;

		for (int i = 0; i < points.size() - 1; i++){
			curPoint = points.get(i);
			nextPoint = points.get(i+1);

			if (nextPoint[1] == curPoint[1]) {
				y = curPoint[1] + 1;
				for (int j = (curPoint[0] + 1); j < nextPoint[0]; j++) {
					if ((j - startX > 0) && (j - startX < width) && (y - startY > 0) && (y - startY < height))
						plateInverted[j-startX][y-startY] = true;
				}
			}
		}

		be = new BlobExtraction(width, height, 75, 2);
		be.extract(plateInverted);

		if (be.getNumBlobs() != 1) {
			//System.out.println("Found " + b.getNumBlobs() + " blobs");
		} else {
			ArrayList<int[]> blackDotPoints = be.getBlob(0);
			int[] dotMidpoint = getMidpoint(blackDotPoints);
			dotMidpoint[0] += startX;
			dotMidpoint[1] += startY;

			//System.out.println("Dot midpoint: ( " + dotMidpoint[0] + " , " + dotMidpoint[1] + ")");
			//System.out.println("Plate midpoint: ( " + midpoint[0] + " , " + midpoint[1] + ")");

			angle = (int) Math.toDegrees(Math.atan2(dotMidpoint[1] - midpoint[1], midpoint[0] - dotMidpoint[0]));
			if (angle < 0) {
				angle= 360 + angle;
			}
		}
	}

	private int[] getMidpoint(ArrayList<int[]> points) {
		int sumX = 0, sumY = 0;
		
		for(int[] i : points) {
			sumX += i[0];
			sumY += i[1];
		}
		int size = points.size();

		return new int[] {(int) sumX/size, (int) sumY/size};
	}

	private void getBoundingBox() {
		int[] p = points.get(0);

		startX = p[0];
		endX = p[0];
		startY = p[1];
		endY = p[1];

		for (int i = 1; i < points.size(); i++) {
			p = points.get(i);

			if (p[0] < startX) {
				startX = p[0];
			}
			if (p[0] > endX) {
				endX = p[0];
			}
			if (p[1] < startY) {
				startY = p[1];
			}
			if (p[1] > endY) {
				endY = p[1];
			}
		}
		width = endX - startX;
		height = endY - startY;

		midpoint = new int[]{(startX + endX)/2, (startY + endY)/2};
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
