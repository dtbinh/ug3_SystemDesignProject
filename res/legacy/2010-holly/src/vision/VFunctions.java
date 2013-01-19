package vision;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Martin
 */
public class VFunctions {
	
	/**
	 * Given a list of [x, y] points this method returns the midpoint of these 
	 * points by summing up the values and dividing them by the number of points.
	 * @param points
	 * @return Midpoint as an [x, y] array.
	 */
	public static int[] getMidpoint(ArrayList<int[]> points) {
		int sumX = 0;
		int sumY = 0;
		int size = points.size();
		
		for (int[] i : points) {
			sumX += i[0];
			sumY += i[1];
		}
		
		if (sumX != 0 && sumY != 0) {
			return new int[] {sumX / size, sumY / size};
		} else {
			return new int[] {-1, -1};
		}
	}
	
	/**
	 * Calculates the median from an array of integer values.
	 * @param values
	 * @return Median value.
	 */
	public static int median(ArrayList<Integer> values) {
		Collections.sort(values);

		if (values.size() % 2 == 1) {
			return values.get((values.size()+1)/2-1);
		} else {
			int lower = values.get(values.size()/2-1);
			int upper = values.get(values.size()/2);
			
			return (int) ((lower + upper) / 2.0);
		}	
	}
}
