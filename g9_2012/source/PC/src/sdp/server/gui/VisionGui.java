package sdp.server.gui;

import javax.swing.JButton;
import javax.swing.JSlider;

import sdp.server.gui.ControlPanel.SliderCallback;
import sdp.server.vision.Vision;

/** GUI class that provides controls for the vision subsystem (threshold sliders, save/load values etc.).
 * 
 * @author Cristian Cobzarenco
 *
 */
public class VisionGui {
	private Vision       vision;
	private ControlPanel panel;
	private ImageViewer  viewer;
	private int          adjustedChannel = 0;
	
	public VisionGui( Vision vision ) {
		this( vision, null, null );
	}
	
	public VisionGui( Vision vision, ImageViewer viewer ) {
		this( vision, viewer, null );
	}
	
	public VisionGui( Vision vision, ImageViewer viewer, ControlPanel panel ) {
		if( panel == null ) {
			panel = new ControlPanel();
			panel.setTitle( "Vision Controls" );
		}
		
		if( viewer == null ) {
			viewer = new ImageViewer( vision.getScreenProjection() );
			viewer.setTitle( "Vision Output" );
		}
			
		this.vision = vision;
		this.viewer = viewer;
		this.panel  = panel;
	}
	
	public void initialise() {
		addControls();
		addListeners();
		
		update();
	}
	
	public ControlPanel getPanel() {
		return panel;
	}
	
	public ImageViewer getViewer() {
		return viewer;
	}
	
	public void update() {
		viewer.showImage( vision.getDebugImage() );
	}
	
	public void dispose() {
		viewer.dispose();
		panel.dispose();
	}
	
	private void addControls() {
		panel.addSlider("Channel (Ball, Yellow, Blue, Y. Circle, B. Circle)", 0, 0, 4, new SliderCallback() {
			public void valueChanged( JSlider s, int newValue ) {
				adjustedChannel = newValue;
				updatePropertySliders();
				if( vision.isShowingChannel() )
					vision.showEntityChannel( adjustedChannel );
			}
		});
		
		// Add vision property sliders
		for( Vision.Property p : Vision.Property.values() ) {
			for( Vision.Value v : Vision.Value.values() )
				panel.addSlider( p.toString() + " " + v.toString(), 0, 0, 255, new PropertyCallback(p,v) );
		}
		
		updatePropertySliders();
		
		panel.addButton("Show/Hide Channel", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton b) {
				if( vision.isShowingChannel() )
					vision.showNormal();
				else
					vision.showEntityChannel( adjustedChannel );
			}
		});
				
		panel.addButton("Auto Crop", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton b) {
				vision.autoCrop();
			}
		});
		
		panel.addButton("Save Properties", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton b) {
				vision.saveProperties();
			}
		});
		
		panel.addButton("Load Properties", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton b) {
				vision.loadProperties();
				updatePropertySliders();
			}
		});
	}
	
	private void addListeners() {}
	
	private void updatePropertySliders() {
		for( Vision.Property p : Vision.Property.values() ) {
			for( Vision.Value v : Vision.Value.values() )
				panel.getSlider(p.toString() + " " + v.toString()).setValue(
					vision.getProperty( adjustedChannel, p, v )
				);
		}
	}
	
	private class PropertyCallback implements SliderCallback {
		PropertyCallback( Vision.Property property, Vision.Value valueType ) {
			this.property  = property;
			this.valueType = valueType;
		}
		
		@Override
		public void valueChanged(JSlider s, int newValue) {
			vision.setProperty( adjustedChannel, property, valueType, newValue );
			System.out.println("VisionGui: " +  adjustedChannel + "." + property + "." + valueType + " = " + newValue);
		}
		
		private Vision.Property property;
		private Vision.Value    valueType;
	}
}
