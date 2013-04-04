/**
 * 
 */
package vision.old;

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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;

/**
 * @author laurentiu
 * 
 */
public class ConvexHull {

	public static ArrayList<int[]> getConvexHull(ArrayList<int[]> blob) {
		if (blob.size() < 3) {
			System.out.println("You need more than 3 points");
			return null;
		} else {
			ArrayList<int[]> sorted = getSortedFromLowestY(blob);
			print(sorted);
			removeColinearPoints(sorted);
			print(sorted);

			if (sorted.size() < 3) {
				System.out
				.println("Cannot create a convex hull. All the points are collinear");
				return null;
			}

			else {
				Stack<int[]> stack = new Stack<int[]>();
				stack.push(sorted.get(0));
				stack.push(sorted.get(1));
				stack.push(sorted.get(2));

				for (int i = 3; i < sorted.size(); i++) {
					System.out.println("stack size" + stack.size());
					int[] a;
					if(stack.size() >= 2)
					{
						a = sorted.get(i);
						int headx = a[0];
						int heady = a[1];
						a = stack.pop();
						int middlex = a[0];
						int middley = a[1];
						a = stack.pop();
						int tailx = a[0];
						int taily = a[1];
						if (formsLeftTurn(tailx, taily, middlex, middley, headx,
								heady)) {
							stack.push(new int[] { tailx, taily });
							stack.push(new int[] { middlex, middley });
							stack.push(new int[] { headx, heady });
						} else {
							//System.out.println("stack size" + stack.size());
							stack.push(new int[] { tailx, taily });
							//System.out.println("stack size" + stack.size());
							i--;
							//if(stack.size()<2)
								//stack.push(new int[] { middlex, middley });
						}
					}
				}
				stack.push(sorted.get(0));
				return new ArrayList<int[]>(stack);
			}
		}

	}
	public static void print (ArrayList<int[]> points){
		for (int i = 0; i < points.size(); i++) {
			int[] a = points.get(i);
			System.out.print("(" + a[0]+" " + a[1] + ")");
		}
		System.out.println();
	}
	
	public static int getAngle(ArrayList<int[]> points)
	{
		return 0;
	}

	private static boolean formsLeftTurn(int tailx, int taily, int middlex,
			int middley, int headx, int heady) {
		return crossProduct(tailx, taily, middlex, middley, headx,
				heady) > 0;

	}

	public static float crossProduct(int xa, int ya, int xb, int yb, int xc, int yc)
	{
		float res = ((xb-xa)*(yc-ya))-((xc-xa)*(yb-ya));
		System.out.println("cross product result" + res);
		return res;
	}

	private static int[] getLowestY(ArrayList<int[]> points) {
		int lowestx = -1, lowesty = -1;
		Iterator<int[]> iter = points.iterator();
		while (iter.hasNext()) {
			int[] local = iter.next();
			int localx = local[0], localy = local[1];
			if ((lowestx == (-1) && lowesty == (-1)) || (localx < lowesty)
					|| ((localy == lowesty) && (localx < lowestx))) {
				lowestx = localx;
				lowesty = localy;
			}
		}
		return new int[] { lowestx, lowesty };

	}


	private static ArrayList<int[]> getSortedFromLowestY(ArrayList<int[]> points) {
		ArrayList<int[]> sorted = new ArrayList<int[]>(points);
		Collections.sort(sorted, new LowestYComparator(getLowestY(points)));
		return sorted;
	}


	private static void removeColinearPoints(ArrayList<int[]> sorted) {
		int[] lowestY = sorted.get(0);
		for (int i = sorted.size() - 1; i > 1; i--) {
			float slopeB = getSlope(lowestY, sorted.get(i));
			float slopeA = getSlope(lowestY, sorted.get(i - 1));
			if (slopeB == slopeA) {
				sorted.remove(i - 1);
			}
		}
	}

	public static float getSlope(int[] a, int[] b) {
		return ((float) (b[1] - a[1])) / (b[0] - a[0]);
	}
	public static float distance(int[] a, int[] b){
		return Math.abs(b[0]-a[0])+Math.abs(b[1]-a[1])+1;

	}

	private static class LowestYComparator implements Comparator<int[]> {

		final int[] lowestY;

		LowestYComparator(int[] lowestY) {
			this.lowestY = lowestY;
		}

		public boolean equalsV(int[] a, int[] b) {
			if (a[0] == b[0] && a[1] == b[1])
				return true;
			else
				return false;
		}


		public int compare(int[] a, int[] b) {
			if (equalsV(a, lowestY))
				return -1;
			if (equalsV(b, lowestY))
				return 1;

			float slopeA = getSlope(lowestY, a);
			float slopeB = getSlope(lowestY, b);

			if (slopeA == slopeB) {
				float distanceAY = distance(a, lowestY);
				float distanceBY = distance(b, lowestY);
				if (distanceAY < distanceBY)
					return (int) distanceAY;
				else
					return (int) -distanceBY;
			}

			// can i do >=?
			if (slopeA > 0 && slopeB < 0) {
				return -1;
			} else if (slopeA < 0 && slopeB > 0) {
				return 1;
			} else {
				if (slopeA < slopeB) {
					return (int) slopeA;
				} else {
					return (int) -slopeB;
				}
			}

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<int[]> a= new ArrayList<int[]>();

		a.add(new int[]{0,0});
		a.add(new int[]{5,1});
		a.add(new int[]{3,2});
		a.add(new int[]{2,16});
		a.add(new int[]{8,2});
		a.add(new int[]{4,3});
		a.add(new int[]{13,7});
		a.add(new int[]{2,5});
		a.add(new int[]{7,11});
		a.add(new int[]{5,1});
		a.add(new int[]{10,4});
		long start = System.currentTimeMillis();
		ArrayList<int[]> b = ConvexHull.getConvexHull(a);
		System.out.println("FPS: " + 1000
				/ (System.currentTimeMillis() - start)+" total time " + (System.currentTimeMillis() - start));
		System.out.println("Convex Hull: ");
		print(b);

		/*
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		try {
			//image = ImageIO.read(new File("src/img/extremes/shot0014.png"));
			image = ImageIO.read(new File("src/img/shot0012.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Raster data = image.getData();

		ImageProcessor2 ip = new ImageProcessor2(true);
		ip.findTBlob(data, 1, true);
		a = ip.blobs;
		ArrayList<int[]> b = ConvexHull.getConvexHull(a);
		print(b);
		// for further blob algorithm debugging
		if (true) {
			WritableRaster wraster2 = null;
			BufferedImage image2 = null;
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
			wraster2 = data.createCompatibleWritableRaster();
			wraster2.setDataElements(0, 0, data);
			image2 = new BufferedImage(cm, wraster2, false, null);

			for (int i = 0; i < b.size(); i++) {
				wraster2.setPixel(b.get(i)[0], b.get(i)[1], new int[] {255, 0, 0});
			}

			try {
				ImageIO.write(image2, "png", new File("hull.png"));
			} catch (IOException e) {
				System.out.println("Could not save debug picture!");
				e.printStackTrace();
			}
		}*/
	} //[(0,0), (5,1), (8,2), (13,7), (2,16), (0,0)]


}
