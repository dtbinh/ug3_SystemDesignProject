package vision.old;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author Ben
 */
public class BlobExtraction {
	
	private final int DEF_CONN;

	private int width;
	private int height;
	private int minSize;
	private int conn;
	private boolean[][] binaryImg;
	private int[][] labels;
	private int label;
	private int labelNorth;
	private int labelWest;
	private int count;
	private int min, max;
	private ArrayList<int[]> blobs[];
	private int blobCount;
	private Hashtable<Integer, Integer> equivalence;
	private ArrayList<Integer> goodBlobs;
	
	/**
	 * Creates a new instance of BlobExtraction for extracting blobs from a frame.
	 * @param width - width of the frame
	 * @param height - height of the frame
	 * @param minSize - minimum size of the blobs for filtering
	 * @param conn - connectivity for the blobbing
	 */
	protected BlobExtraction(int width, int height, int minSize, int conn) {
		this.minSize = minSize;
		this.width = width;
		this.height = height;
		this.conn = conn;
		
		this.DEF_CONN = conn;
		this.labels = new int[width][height];
		this.equivalence = new Hashtable<Integer, Integer>();
		this.goodBlobs = new ArrayList<Integer>();
	}

	/**
	 * Given a frame this method will extract new blobs from a frame.
	 * @param data - binary frame on which to do the processing
	 */
	protected void extract(boolean[][] binaryImg) {
		// clear stuff before the run
		labels = new int[width][height];
		blobCount = 0;
		goodBlobs.clear();
		equivalence.clear();
		
		// update the binary image
		this.binaryImg = binaryImg;

		// execute the core stuff
		getBlobs();
	}
	
	/**
	 * Saves blobs from a binary image and filters them out as well.
	 */
	private void getBlobs() {
		
		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if (binaryImg[i][j]) {
					checkNeighbours(i, j);
				}
			}
		}
		
		blobs = new ArrayList[blobCount];
		for (int i=0; i<blobCount; i++){
			blobs[i] = new ArrayList<int[]>();
		}
		
		// second pass to add blobs together
		for (int i = 1; i < height-1; i++) {
			for (int j = 1; j < width-1; j++) {
				label = labels[j][i];
				if (label > 0) {
					blobs[equivalent(label)].add(new int[]{j,i});
				}
			}
		}
		
		for (int i = 0; i < blobs.length; i++) {
			if (blobs[i].size() > minSize) {
				goodBlobs.add(i);
			}
		}
	}
	
	private int equivalent (int label){
		if (equivalence.containsKey(label)) {
			return equivalent(equivalence.get(label));
		} else {
			return label;
		}
	}

	/**
	 * Checks the neighbours of a pixel for blobbing.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 */
	private void checkNeighbours(int x, int y) {
		labelNorth = labels[x][y - 1];
		labelWest = labels[x - 1][y];
		
		count = 0;
		if (binaryImg[x][y - 1]) {
			count++;
			labelNorth = labels[x][y - 1];
		}
		if (binaryImg[x - 1][y]) {
			count++;
			labelWest = labels[x - 1][y];
		}
		if (binaryImg[x + 1][y]) {
			count++;
		}
		if (binaryImg[x][y + 1]) {
			count++;
		}
		if (binaryImg[x + 1][y + 1]) {
			count++;
		}
		if (binaryImg[x - 1][y - 1]) {
			count++;
		}
		if (binaryImg[x - 1][y + 1]) {
			count++;
		}
		if (binaryImg[x + 1][y - 1]) {
			count++;
		}

		// if not enough connected pixels then ignore (directly affects speed!)
		if (count < conn) {
			return;
		}

		// if two matches
		if (labelWest > 0 && labelNorth > 0) {
			if (labelWest == labelNorth) {
				
				// if the labels are the same
				labels[x][y] = labelWest;
				return;
			} else {
				// if they differ choose the minimum and remember that they are equivalent
				max = Math.max(labelWest, labelNorth);
				min = Math.min(labelWest, labelNorth);

				labels[x][y] = min;
				equivalence.put(max, min);
				
				return;
			}
		}

		// if one match then add to that region
		if (labelWest > 0 || labelNorth > 0) {
			label = Math.max(labelWest, labelNorth);
			labels[x][y] = label;
			return;
		}

		// if no matches create a new region
		labels[x][y] = blobCount++;
	}

	/**
	 * Checks how many blobs were found.
	 * @return Number of blobs.
	 */
	protected int getNumBlobs() {
		return goodBlobs.size();
	}

	/**
	 * Returns a specified blob.
	 * @param i - index
	 * @return Blob at index.
	 */
	protected ArrayList<int[]> getBlob(int i) {
		return blobs[goodBlobs.get(i)];
	}
	
	/**
	 * @return Level of connectivity for blobbing.
	 */
	protected int getConnectivity() {
		return conn;
	}
	
	/**
	 * Changes the level of connectivity for blobbing. Lower values 
	 * work better if blobs get disconnected, higher values if too 
	 * much noise is being picked up.
	 * @param value
	 */
	protected void setConnectivity(int value) {
		if (value > 0 && value < 9)
			conn = value;
	}
	
	/**
	 * Resets the level of blobbing connectivity to the default value. 
	 */
	protected void resetConnectivity() {
		conn = DEF_CONN;
	}
}
