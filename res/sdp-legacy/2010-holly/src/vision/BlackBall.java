/**
 * 
 */
package vision;

import java.awt.Color;
import java.awt.image.Raster;
import java.util.ArrayList;

/**
 * @author Laurentiu
 * 
 */
public class BlackBall {

	private ArrayList<int[]> midline; // store all points picked up by IP here
	private Raster data;

	public BlackBall(Raster data, ArrayList<int[]> midline) {
		this.midline = new ArrayList<int[]>(midline);
		this.data = data;

	}

	public int[] blackBallMid(Raster data, ArrayList<int[]> midline) {
		int[] bb ;
		int x = (midline.get(0)[0] + midline.get(1)[0])/2;
		int y = (midline.get(0)[1] + midline.get(1)[1])/2;
		ArrayList<Integer> bp1 = getBlackPoints(data, x, y);
		x = (midline.get(0)[0] + midline.get(2)[0])/2;
		y = (midline.get(0)[1] + midline.get(2)[1])/2;
		ArrayList<Integer> bp2 = getBlackPoints(data, x, y);
		//System.out.println("nr of black"+bp1.get(0) +" "+ bp2.get(0));
		if(bp1.get(0) > bp2.get(0))
		{
			bb = new int[]{bp1.get(1),bp1.get(2)};
		}
		else
		{
			bb = new int[]{bp2.get(1),bp2.get(2)};
		}
		return bb;

	}

	public ArrayList<Integer> getBlackPoints(Raster data, int x, int y) {
		// radii for black ball scanning
		//System.out.println(data.getWidth()+" " +data.getHeight());
		int radius = 15;
		int cutwidth = radius * 2;
		int cutheight = radius * 2;

		// just in case if we're near the edge of a screen
		if (x + cutwidth >= data.getWidth()) {
			cutwidth = data.getWidth() - x;
		}
		if (y + cutheight >= data.getHeight()) {
			cutheight = data.getHeight() - y;
		}

		// take out a rectangular section based on the radii
		//System.out.println((x - radius)+" "+(y - radius)+" "+cutwidth+" "+ cutheight+" "+( x - radius)+" "+( y - radius));
		Raster radiusdata = data.createChild(x - radius, y - radius,
				cutwidth, cutheight, x - radius, y - radius, null);

		// do same as before, but convert to HSB colour representation
		int[] pixeldata = new int[3];
		float[] hsbdata = new float[3];
		int hue = 0, sat = 0, bright = 0;
		ArrayList<int[]> points = new ArrayList<int[]>();
		for (int i = 0; i < radiusdata.getHeight(); i++) {
			for (int j = 0; j < radiusdata.getWidth(); j++) {
				radiusdata.getPixel(j + radiusdata.getMinX(), i
						+ radiusdata.getMinY(), pixeldata);
				Color.RGBtoHSB(pixeldata[0], pixeldata[1], pixeldata[2],
						hsbdata);

				hue = Math.round(hsbdata[0] * 360);
				sat = Math.round(hsbdata[1] * 100);
				bright = Math.round(hsbdata[2] * 100);
				// H:340-100, S:0-20, B:20-60
				if ((hue >= 340 || hue <= 100) && (sat >= 0 && sat <= 20)
						&& (bright >= 20 && bright <= 60)) {
					points.add(new int[] { j, i });
				}
			}
			// break out from the loop if we scanned too many lines and not
			// found anything for a while; much much faster
			if (points.size() != 0
					&& (points.get(points.size() - 1)[1] + 10) < i) {
				break;
			}
		}

		ArrayList<Integer> candidate = new ArrayList<Integer>();
		
		//get the nr of black points
		candidate.add(points.size());
		// get the midpoint
		int dot[] = position(points);
		int dotx = dot[0];
		int doty = dot[1];

		// failed to find them 
		if (dotx == -1 && doty == -1) {
			candidate.add(-1);
			candidate.add(-1);
			return candidate;
		}

		// re-translate the dot's coordinates
		dotx += radiusdata.getMinX();
		doty += radiusdata.getMinY();
		candidate.add(dotx);
		candidate.add(doty);
		return candidate;

	}

	public int[] position(ArrayList<int[]> points) {
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
			return new int[] { x, y, -1 };
		}

		return new int[] { x, y, -1 };
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
