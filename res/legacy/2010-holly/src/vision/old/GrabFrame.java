package vision.old;

import au.edu.jcu.v4l4j.Control;
import java.nio.ByteBuffer;

import au.edu.jcu.v4l4j.RGBFrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import java.util.List;

public class GrabFrame extends Thread {


	private VideoDevice vd;
	private RGBFrameGrabber fg;
	private ByteBuffer camFeed;
	private byte[] currentFrame;
	private boolean finished = false;

	private boolean waiting = true;

	public GrabFrame() {
		try {
			vd = new VideoDevice(Vision.VIDEO_CARD);
			fg = vd.getRGBFrameGrabber(Vision.FRAME_WIDTH, Vision.FRAME_HEIGHT,
					Vision.VIDEO_CHANNEL, V4L4JConstants.STANDARD_PAL);

                        List<Control> controls =  vd.getControlList().getList();
                        for(Control c: controls){
                                System.out.println("control name: "+c.getName()+" - min: "+c.getMinValue()+" - max: "+c.getMaxValue()+" - step: "+c.getStepValue()+" - value: "+c.getValue());
                                if(c.getName().equals("Contrast"))	c.setValue(Vision.CONTRAST);
                                if(c.getName().equals("Brightness"))	c.setValue(Vision.BRIGHTNESS);
                                if(c.getName().equals("full luma range"))	c.setValue(Vision.FULL_LUMA_RANGE);
                                if(c.getName().equals("Hue"))	c.setValue(Vision.HUE);
                                if(c.getName().equals("Saturation"))	c.setValue(Vision.SATURATION);
                                if(c.getName().equals("uv ratio"))	c.setValue(Vision.UV_RATIO);
                        }
                        vd.releaseControlList();


		} catch (V4L4JException ex) {
			System.out.println(ex);
		}

		currentFrame = new byte[Vision.FRAME_WIDTH*Vision.FRAME_HEIGHT*3];
	}

	public synchronized void stopCapture() {
		waiting = true;
		fg.stopCapture();
		// Free capture resources and release the FrameGrabber
		vd.releaseFrameGrabber();

		// release VideoDevice
		vd.release();
	}

	public synchronized void startCapture() {
		try {
			fg.startCapture();
			waiting = false;
		}
		catch (V4L4JException ex) {
			System.out.println("Could not retrieve the image from the Video Card!");
			ex.printStackTrace();
			stopCapture();
		}
	}

	@Override
	public void run() {
		while(!finished) {
			while (!waiting) {
				try {
					camFeed = fg.getFrame();
				}
				catch (V4L4JException ex) {
					System.out.println("Could not retrieve the frame from the FrameGrabber!");
					ex.printStackTrace();
				}
			}

		}
	}

	public synchronized byte[] getFrame(){
		while(camFeed==null){
			System.out.println("camFeed is null!");
		}
		camFeed.get(currentFrame);
		return currentFrame;
	}

	public synchronized void exit() {
		finished = true;
	}
}
