package vision;

import java.awt.Color;

public class RGBHSVMap {

	private float[][][] h;
	private float[][][] s;
	private float[][][] v;

	public RGBHSVMap() {
		h = new float[256][256][256];
		s = new float[256][256][256];
		v = new float[256][256][256];
		float[] hsv = new float[3];

		for (int r = 0; r < 256; r++) {
			for (int g = 0; g < 256; g++) {
				for (int b = 0; b < 256; b++) {
					Color.RGBtoHSB(r, g, b, hsv);
					h[r][g][b] = hsv[0];
					s[r][g][b] = hsv[1];
					v[r][g][b] = hsv[2];
				}
			}
		}
	}

	public float[] RGBtoHSV(int r, int g, int b, float[] fArray) {
		float h = this.h[r][g][b];
		float s = this.s[r][g][b];
		float v = this.v[r][g][b];

		if (fArray != null) {
			fArray[0] = h;
			fArray[1] = s;
			fArray[2] = v;
		}
		return new float[] {h, s, v};
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		RGBHSVMap map = new RGBHSVMap();
		System.out.println(System.currentTimeMillis() - start);

		float[] result = new float[3];

		start = System.nanoTime();
		map.RGBtoHSV(25, 127, 90, result);
		System.out.println(System.nanoTime() - start);

		for (int i = 0; i < result.length; i++) {
			System.out.println(result[i]);
		}

		start = System.nanoTime();
		Color.RGBtoHSB(25, 127, 90, result);
		System.out.println(System.nanoTime() - start);

		for (int i = 0; i < result.length; i++) {
			System.out.println(result[i]);
		}
	}
}
