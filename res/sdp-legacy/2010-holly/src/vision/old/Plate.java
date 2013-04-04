package vision.old;

import java.awt.image.Raster;
import java.util.ArrayList;

public class Plate {
	
	private int type;
	private PlateM pm;
	private PlateB pb;
	private PlateL pl;
	
	/**
	 * Creates a new Plate object given a set of points and a reference
	 * to a frame. All properties are calculated during the object's creation.
	 * @param type - which type of Plate to use (0 - Martin, 1 - Ben, 2 - Laurentiu)
	 * @param points - array of points which belong to this Plate's blob
	 * @param data - reference to the frame for computation purposes
	 * @param debug - set whether to run in debug mode
	 */
	public Plate(int type, ArrayList<int[]> points, Raster data, int debug) {
		this.type = type;
		
		if (type == 0) {
			pm = new PlateM(points, data, debug);
		} else if (type == 1) {
			pb = new PlateB(points, data, debug);
		} else if (type == 2) {
			pl = new PlateL(points, data, debug);
		}
	}
	
	/**
	 * Return 0 for not a valid plate, 1 for blue, and 2 for yellow.
	 * @return Classification integer.
	 */
	public int getClassification() {
		if (type == 0) {
			return pm.getClassification();
		} else if (type == 1) {
			return pb.getClassification();
		} else if (type ==2) {
			return pl.getClassification();
		}
		return 0;
	}
	
	/**
	 * @return Midpoint in an integer {x, y} array.
	 */
	public int[] getMidpoint() {
		if (type == 0) {
			return pm.getMidpoint();
		} else if (type == 1) {
			return pb.getMidpoint();
		} else if (type ==2) {
			return pl.getMidpoint();
		}
		return new int[] {-1, -1};
	}
	
	/**
	 * @return Angle in degree integer format.
	 */
	public int getAngle() {
		if (type == 0) {
			return pm.getAngle();
		} else if (type == 1) {
			return pb.getAngle();
		} else if (type ==2) {
			return pl.getAngle();
		}
		return -1;
	}
	
	/**
	 * @return Reference to the list of points belonging to the Plate's blob.
	 */
	public ArrayList<int[]> getPoints() {
		if (type == 0) {
			return pm.getPoints();
		} else if (type == 1) {
			return pb.getPoints();
		} else if (type ==2) {
			return pl.getPoints();
		}
		return new ArrayList<int[]>();
	}
}
