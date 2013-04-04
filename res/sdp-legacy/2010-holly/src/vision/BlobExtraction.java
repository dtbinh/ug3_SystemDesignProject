package vision;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author Ben
 */
public class BlobExtraction {
	
	private int width;
	private int height;
	private boolean[][] binaryImg;
	private int[][] labels;
	private int labelNorth;
	private int labelWest;
	private int conn;
	private ArrayList<int[]> blobs[];
	private int[] goodBlobs;
	private int blobCount;
	private Hashtable<Integer, Integer> equivalence;
	private int gbc = 0;
	private int minSize, label, count, max, min;

	/**
	 * Creates a new instance of BlobExtraction for extracting blobs from a frame.
	 */
	@SuppressWarnings("unchecked")
	protected BlobExtraction() {
		equivalence = new Hashtable<Integer, Integer>();
		goodBlobs = new int[100];

		blobs = new ArrayList[10000];
		for (int i=0; i<10000; i++){
			blobs[i] = new ArrayList<int[]>();
		}

		labels = new int[768][576];
	}

	/**
	 * Given a frame this method will extract new blobs from a frame.
	 * @param data - binary frame on which to do the processing
	 */
	protected void extract(boolean[][] binaryImg) {
		// clear stuff before the run
		for(int i=0;i<768; i++){
			for(int j=0; j<576; j++){
				labels[i][j] = 0;
			}
		}

		blobCount = 0;
		gbc = 0;

		for(int i=0; i<10; i++){
			goodBlobs[i] = 0;
		}

		equivalence.clear();

		for (int i=0; i<10000; i++){
			blobs[i].clear();
		}

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

		// second pass to add blobs together
		for (int i = 1; i < height-1; i++) {
			for (int j = 1; j < width-1; j++) {
				label = labels[j][i];
				if(label>0){
					blobs[equivalent(label)].add(new int[]{j,i});
				}
			}
		}

		for (int i = 0; i < blobs.length; i++) {
			if (blobs[i].size() > minSize) {
				goodBlobs[gbc++] = i;
			}
		}
	}

	private int equivalent(int label) {
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
	
	/*
	 * ===============
	 * GETTERS/SETTERS
	 * ===============
	 */

	/**
	 * Checks how many blobs were found.
	 * @return Number of blobs.
	 */
	protected int getNumBlobs() {
		return gbc;
	}

	/**
	 * Returns a specified blob.
	 * @param i - index
	 * @return Blob at index.
	 */
	protected ArrayList<int[]> getBlob(int i) {
		return blobs[goodBlobs[i]];
	}
	
	/**
	 * Set the dimensions of the binary image to scan.
	 * @param width - frame height
	 * @param height - frame width
	 */
	protected void setDimensions(int width, int height){
		this.width = width;
		this.height = height;
	}

	/**
	 * Changes the level of connectivity for blobbing. Lower values
	 * work better if blobs get disconnected, higher values if too
	 * much noise is being picked up.
	 * @param value
	 */
	protected void setConnectivity(int value) {
		if (value > 0 && value < 9){
			conn = value;
		}
	}

	/**
	 * Set the minimum size of blobs to be filtered out.
	 * @param minSize - minimum size
	 */
	protected void setMinSize(int minSize){
		this.minSize = minSize;
	}
}
