package vision;

import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Applies median filtering to a raster. Works by taking 8 neighbours of a pixel 
 * and selecting a median value from 9 samples for each channel in RGB which 
 * replaces the values of the pixels.
 * @author Martin
 */
public class MedianFilter {

	WritableRaster data;

	public MedianFilter(WritableRaster data) {
		this.data = data;
	}
	
	/**
	 * Applies the filtering.
	 */
	public void filter() {
		for (int y = 0; y < data.getHeight(); y++) {
			for (int x = 0; x < data.getWidth(); x++) {
				setMedian(x, y);
			}
		}
	}

	/**
	 * Collects 9 values (current pixels and 8 neighbours) and sets its RGB 
	 * values to the median of the 9.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 */
	private void setMedian(int x, int y) {
		int[] pixel = new int[3];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> g = new ArrayList<Integer>();
		ArrayList<Integer> b = new ArrayList<Integer>();
		// 3x3
		for (int j = -1; j < 2; j++) {
			for (int i = -1; i < 2; i++) {
				try {
					data.getPixel(x + i, y + j, pixel);
					r.add(pixel[0]);
					g.add(pixel[1]);
					b.add(pixel[2]);
				} catch (ArrayIndexOutOfBoundsException e) {
					// do nothing when out of bounds
				}
			}
		}

		data.setPixel(x, y, new int[] {median(r), median(g), median(b)});
	}

	/**
	 * @param vals - values from which to compute the median
	 * @return Median.
	 */
	private int median(ArrayList<Integer> vals) {
		Collections.sort(vals);

		if (vals.size() % 2 == 1) {
			return vals.get((vals.size() + 1) / 2 - 1);
		} else {
			double lower = vals.get(vals.size() / 2 - 1);
			double upper = vals.get(vals.size() / 2);

			return (int) ((lower + upper) / 2.0);
		}
	}
	
	/*
	public static void main(String args[]) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		try {
			image = ImageIO.read(new File("src/img/shot0010.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Raster data = image.getData();
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ComponentColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		WritableRaster wRaster = data.createCompatibleWritableRaster();
		wRaster.setDataElements(0, 0, data);

		MedianFilter test = new MedianFilter(wRaster);
		long start = System.currentTimeMillis();
		test.filter();
		System.out.println(System.currentTimeMillis() - start);

		BufferedImage image2 = new BufferedImage(cm, wRaster, false, null);

		try {
			ImageIO.write(image2, "png", new File("src/img/shot0010_f.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
