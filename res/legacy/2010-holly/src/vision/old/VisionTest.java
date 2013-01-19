package vision.old;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import baseSystem.Singleton;

/**
 * Class to be used for emulating vision on a system that doesn't have 
 * access to a video feed and V4L4J.
 */
public class VisionTest extends Thread {

	public final static int FRAME_WIDTH = Vision.FRAME_WIDTH;
	public final static int FRAME_HEIGHT = Vision.FRAME_HEIGHT;
	public final static int PITCH_START_X = Vision.PITCH_START_X;
	public final static int PITCH_START_Y = Vision.PITCH_START_Y;
	public final static int PITCH_END_X = Vision.PITCH_END_X;
	public final static int PITCH_END_Y = Vision.PITCH_END_Y;

	private int debug;
	private boolean finished;
	private WritableRaster raster;
	private WritableRaster raster1;
	private WritableRaster raster2;
	private ImageProcessor4 ip;
	private long start;
	private Singleton singleton;
	private VisionFrame vf;
	
	public VisionTest(int debug, boolean integrated) {
		super("Vision");
		this.debug = debug;
		finished = false;
		if (integrated) {
			singleton = Singleton.getSingleton();
		}
		
		BufferedImage image1 = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage image2 = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		try {
			//image1 = ImageIO.read(new File("src/img/shot0010.png"));
			image1 = ImageIO.read(new File("src/img/new/shot0003.png"));
			image1 = image1.getSubimage(PITCH_START_X, PITCH_START_Y, (PITCH_END_X - PITCH_START_X), (PITCH_END_Y - PITCH_START_Y));
			//image2 = ImageIO.read(new File("src/img/shot0014.png"));
			image2 = ImageIO.read(new File("src/img/new/shot0012.png"));
			image2 = image2.getSubimage(PITCH_START_X, PITCH_START_Y, (PITCH_END_X - PITCH_START_X), (PITCH_END_Y - PITCH_START_Y));
		} catch (IOException e) {
			e.printStackTrace();
		}
		raster1 = image1.getRaster();
		raster2 = image2.getRaster();
	}

	public static int getPitchHeight() {
		return PITCH_END_Y - PITCH_START_Y;
	}

	public static int getPitchWidth() {
		return PITCH_END_X - PITCH_START_X;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public int debugLevel() {
		return debug;
	}
	
	public void run() {
		int[] coords = new int[9];

		ip = new ImageProcessor4(getPitchWidth(), getPitchHeight(), this.debug);
		vf = new VisionFrame(ip);

		while (!finished) {
			start = System.currentTimeMillis();
			// swap rasters to simulate changing of images
			if (start % 2 == 0) {
				raster = raster1;
			} else {
				raster = raster2;
			}
			
			if (singleton != null) {
				if (singleton.getCoordinates() != null) {
					coords = ip.getPosition(raster, singleton.getCoordinates());
				} else {
					coords = ip.getPosition(raster, coords);
				}
				//singleton.setCoordinates(coords);
			} else {
				coords = ip.getPosition(raster, coords);
			}
			if (vf != null)
				vf.update(raster, coords);
			if (debug > 0) {
				System.out.println("VISION >> FPS: " + 1000 / (System.currentTimeMillis() - start));
			}
		}
	}

	public synchronized void exit() {
		finished = true;
	}
	
	public static void main(String[] args) {
		VisionTest vsys = new VisionTest(1, false);
		vsys.start();
	}
}
