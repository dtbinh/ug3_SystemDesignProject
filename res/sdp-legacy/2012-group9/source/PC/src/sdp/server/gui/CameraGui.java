package sdp.server.gui;

import java.io.IOException;
import javax.swing.JSlider;

import sdp.server.nxt.Camera;

/** GUI class that provides controls for the camera: brightness, contrast etc. This wraps a ControlPanel, adding the
 * widgets on initialise() and destroying them on dispose(). The update() method simply makes sure the sliders are
 * set to the right values when the camera is connected. This can also be done manually by calling refreshValues().
 *  
 * @author Cristian Cobzarenco
 *
 */
public class CameraGui {
	private Camera       cam;
	private ControlPanel panel;
	private boolean      connected = false;
	
	/** Build a CameraGui object controlling the properties of a given Camera. Creates a new panel which can then be
	 * accessed using getPanel().
	 * 
	 * @param camera The Camera object to control.
	 */
	public CameraGui( Camera camera ) {
		this( camera, null );
	}
	
	/** Build a CameraGui object controlling the properties of a given Camera, using a given panel. The CameraGui takes
	 * ownership of the panel, destroying it on dispose().
	 * 
	 * @param camera The Camera object to control.
	 * @param panel The ControlPanel object to add the sliders to.
	 */
	public CameraGui( Camera camera, ControlPanel panel ) {
		if( panel == null ) {
			panel = new ControlPanel();
			panel.setTitle("Camera Controls");
		}
		
		this.cam   = camera;
		this.panel = panel;
	}
	
	/** This needs to be called after */
	public void initialise() {
		panel.addSlider("Brightness", 128, 0, 255, new ControlPanel.SliderCallback() {
			@Override
			public void valueChanged(JSlider s, int newValue) {
				cam.setBrightness(newValue);
				System.out.println("CameraGui: Brightness = "+newValue);
			}
		});
		
		panel.addSlider("Contrast", 63, 0, 127, new ControlPanel.SliderCallback() {
			@Override
			public void valueChanged(JSlider s, int newValue) {
				cam.setContrast(newValue);
				System.out.println("CameraGui: Contrast = "+newValue);
			}
		});
		
		panel.addSlider("Saturation", 63, 0, 127, new ControlPanel.SliderCallback() {
			@Override
			public void valueChanged(JSlider s, int newValue) {
				cam.setSaturation(newValue);
				System.out.println("CameraGui: Saturation = "+newValue);
			}
		});
			
		panel.addSlider("Hue", 0, -128, 127, new ControlPanel.SliderCallback() {
			@Override
			public void valueChanged(JSlider s, int newValue) {
				cam.setHue( newValue );
				System.out.println("CameraGui: Hue = "+newValue);
			}
		});
	}
	
	public ControlPanel getPanel() {
		return panel;
	}
	
	public void refreshValues() {
		String[] props = { "Brightness", "Contrast", "Saturation", "Hue" };
		
		for( String p : props ) {
			try {
				JSlider s     = panel.getSlider( p );
				int[]   range = cam.getPropertyRange( p );
				
				s.setMinimum( range[ 0 ] );
				s.setMaximum( range[ 1 ] );
				s.setValue( cam.getPropertyValue( p ) );
			} catch( IOException e ) {
				System.err.println("CameraGui: Could not read value of '" + p + "'");
			}
		}
		
		panel.getSlider( "Brightness" ).setValue( cam.getBrightness() );
		panel.getSlider( "Contrast" ).setValue( cam.getContrast() );
		panel.getSlider( "Saturation" ).setValue( cam.getContrast() );
		panel.getSlider( "Hue" ).setValue( cam.getHue() );
	}
	
	public void update() {
		boolean newConnected = cam.isConnected();
		
		if( connected != newConnected ) {
			connected = newConnected;
			if( connected )
				refreshValues();
		}
	}
	
	public void dispose() {
		panel.dispose();
	}
}
