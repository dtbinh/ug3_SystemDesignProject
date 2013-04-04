package vision.old;

import java.awt.image.Raster;
import java.util.ArrayList;

/**
 * Returns the position of the ball. Refactored out of big IP class as its structure is rather different from the rest.
 * @author Martin
 * @author Laurentiu
 */
public class ImageProcessorBallLab {

	/**
	 * Calculates the position of the ball. If the ball hasn't been found it will return '-1' values in the array.
	 * @param data - rectangular array of pixels
	 * @param bgData - rectangular array of pixels for background frame
	 * @param bgSub - whether to use background subtraction
	 * @return Coordinate of the ball in an array augmented with angle '-1'.
	 */
	public static int[] ballPos(Raster data, Raster bgData, boolean bgSub) {
		ArrayList<int[]> points = new ArrayList<int[]>();
		int[] pixeldata = new int[3];
		int red = 0, green = 0, blue = 0;
		int[] bgvals = new int[3];

		// vertical scanning
		for (int y = 0; y < data.getHeight(); y++) {
			// horizontal
			for (int x = 0; x < data.getWidth(); x++) {
				// save colour information of the pixel to an array
				data.getPixel(x, y, pixeldata);
				int[] lab = new int[3];
				lab = Lab.rgb2lab(pixeldata[0], pixeldata[1], pixeldata[2]);
				red = lab[0];
				green = lab[1];
				blue = lab[2];

					if (red >= 137 && red <= 159 && green >= 50 && green <= 71 && blue >= 37 && blue <= 56) {
						points.add(new int[] {x, y});
					}
			}
			// if we scanned too many lines (and not found anything for a while) and the array is too small (could be just noise) then break out
			if (points.size() > 10 && (points.get(points.size() - 1)[1] + 100) < y) {
				break;
			}
		}

		// remove stray points
		removePoints(points, 4);

		// if we don't have enough points, let's assume it's noise and discard it
		if (points.size() < 4) {
			return new int[] {-1, -1, -1};
		} else {
			// get position of object from all the points
			return position(points);
		}
	}

	/**
	 * Remove points based on a threshold using Euclidean distances.
	 * @param points - points to check
	 * @param threshold - distance threshold for removal
	 */
	private static void removePoints(ArrayList<int[]> points, int threshold) {
		int avgx = 0, avgy = 0;
		int dist = 0;

		// find averages
		for (int i = 0; i < points.size(); i++) {
			avgx += points.get(i)[0];
			avgy += points.get(i)[1];
		}
		try {
			avgx = avgx / points.size();
			avgy = avgy / points.size();
		} catch (ArithmeticException e) {
			return;
		}

		// compute distances and remove if higher than the threshold
		for (int i = 0; i < points.size(); i++) {
			dist = Math.round((float) Math.sqrt((points.get(i)[0] - avgx) ^ 2 + (points.get(i)[1] - avgy) ^ 2));
			if (dist > threshold) {
				points.remove(i);
				i--;
			}
		}
	}

	/**
	 * Returns the position of an object given a list of points that belong to this object.
	 * @param points - ArrayList of points
	 * @return the position as an {x, y} array
	 */
	private static int[] position(ArrayList<int[]> points) {
		// calculate midpoint for location
		int sumx = 0, sumy = 0, x = -1, y = -1;
		int i = 0;

		for (i = 0; i < points.size(); i++) {
			sumx += points.get(i)[0];
			sumy += points.get(i)[1];
		}

		try {
			x = sumx / i;
			y = sumy / i;
		} catch (ArithmeticException e) {
			return new int[] {x, y, -1};
		}

		return new int[] {x, y, -1};
	}
}
