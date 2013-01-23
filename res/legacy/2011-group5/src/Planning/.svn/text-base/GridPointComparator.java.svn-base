package Planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("unchecked")
public class GridPointComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		if (((GridPoint) o1).getTotalCost() < ((GridPoint) o2).getTotalCost())
			return -1;
		else
			return 1;
	}
	
	public ArrayList<GridPoint> sortGridPoints(ArrayList<GridPoint> grids) {
		Collections.sort(grids, this);
		return grids;
	}
}