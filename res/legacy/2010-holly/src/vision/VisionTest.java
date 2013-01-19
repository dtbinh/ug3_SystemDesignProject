package vision;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import baseSystem.Singleton;
import baseSystem.SystemOverview;

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
	private ImageProcessor ip;
	private long start;
	private Singleton singleton;
	private SystemOverview so;

	public VisionTest(int debug, boolean integrated) {
		super("Vision");
		this.debug = debug;
		finished = false;
		if (integrated) {
			Singleton.startSingleton(Singleton.BLUE,Singleton.LEFT,false);
                        singleton = Singleton.getSingleton();
		}
                System.out.println("LOL");
		BufferedImage image1 = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage image2 = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		try {
			//image1 = ImageIO.read(new File("src/img/shot0010.png"));
			image1 = ImageIO.read(new File("/home/woosh/Projects/athletic-intelligence/Robinho/live_imgs/shot0004.png"));
			image1 = image1.getSubimage(PITCH_START_X, PITCH_START_Y, (PITCH_END_X - PITCH_START_X), (PITCH_END_Y - PITCH_START_Y));
			//image2 = ImageIO.read(new File("src/img/shot0014.png"));
			image2 = ImageIO.read(new File("/home/woosh/Projects/athletic-intelligence/Robinho/live_imgs/shot0008.png"));
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
		int[][] coords = new int[9][2];

		ip = new ImageProcessor(getPitchWidth(), getPitchHeight(), this.debug);
		so = new SystemOverview(ip, singleton);
		(new Thread(so)).start();

		while (!finished) {
			start = System.currentTimeMillis();
			// swap rasters to simulate changing of images
			if (start % 2 == 0) {
				raster = raster1;
			} else {
				raster = raster2;
			}

			ip.setData(raster);
			coords = ip.getCoords();
                        if (singleton != null) {
					singleton.setCoordinates(coords);
                        }
			if (so != null)
				so.update(raster, ip.getDebugThreshRaster(), ip.getDebugPlates(), coords);
			if (!so.isVisible() && debug > 0) {
				System.out.println("VISION >> FPS: " + 1000 / (System.currentTimeMillis() - start));
			}
		}
	}

	public synchronized void exit() {
		finished = true;
	}

	public static void main(String[] args) {
		VisionTest vsys = new VisionTest(1, true);
		vsys.start();
	}
}
