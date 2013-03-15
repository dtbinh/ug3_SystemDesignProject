package PathSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GridPointComparator implements Comparator<GridPoint> {
	public int compare(GridPoint p1, GridPoint p2) {
		if (p1.getTotalCost() < p2.getTotalCost()) {
			return -1;
		}
		return 1;
	}

	public ArrayList<GridPoint> sortGridPoints(ArrayList<GridPoint> grids) {
		Collections.sort(grids, this);
		return grids;
	}
}