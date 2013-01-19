package vision;

import java.awt.Color;
import java.awt.image.Raster;

public class HSVStructure {
	
	private float[][] h;
	private float[][] s;
	private float[][] v;
	private int width;
	private int height;
	
	public HSVStructure(Raster raster) {
		this.width = raster.getWidth();
		this.height = raster.getHeight();
		
		h = new float[width][height];
		s = new float[width][height];
		v = new float[width][height];
		int[] rgbvals = new int[3];
		float[] hsvvals = new float[3];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				raster.getPixel(x, y, rgbvals);
				Color.RGBtoHSB(rgbvals[0], rgbvals[1], rgbvals[2], hsvvals);
				h[x][y] = hsvvals[0];
				s[x][y] = hsvvals[1];
				v[x][y] = hsvvals[2];
			}
		}
	}
	
	public void getPixel(int x, int y, float[] fArray) {
		if (fArray != null) {
			fArray[0] = h[x][y];
			fArray[1] = s[x][y];
			fArray[2] = v[x][y];
		}
		//return new float[] {h[y][x], s[y][x], v[y][x]};
	}
}
