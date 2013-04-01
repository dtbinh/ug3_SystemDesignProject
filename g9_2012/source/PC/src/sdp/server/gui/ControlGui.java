package sdp.server.gui;

import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;

import static com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import sdp.server.Control;
import sdp.server.RobotState;
import sdp.server.ScreenProjection;
import sdp.server.World;
import sdp.server.gui.ImageViewer.NewIplImageListener;
import sdp.server.math.Vector2;

public class ControlGui {
	private World        world;
	private Control      control;
	private ImageViewer  viewer;
	private ControlPanel panel;
	private boolean      showDebugGraphics = false;
	private GraphDisplay graphDisplay = null;
	
	private class DebugGraphics implements NewIplImageListener {
		@Override
		public void newIplImage( IplImage img ) {
			if( showDebugGraphics ) {
				graphDisplay.addPoint( control.getRotationError() );
				graphDisplay.newIplImage( img );
				
				ScreenProjection s = world.getScreenProjection();
				RobotState rob  = control.getRobotState();
				Vector2    vps  = control.getWorld().getPitchTopRight();
				Vector2    pos  = rob.getPosition();
				double     trad = control.getTurnRadius();
				
				Vector2 pivot   = control.getCurrentPivot();
				Vector2 gpoint  = control.getCurrentNavPoint();
				Vector2 limited = vps.minus( new Vector2( trad, trad ) ); 
				
				cvRectangle( img, s.projectPosition( vps ).getCvPoint(),     s.projectPosition( vps.times(-1.0) ).getCvPoint(),     cvScalarAll(255),    1, 8, 0 );
				cvRectangle( img, s.projectPosition( limited ).getCvPoint(), s.projectPosition( limited.times(-1.0) ).getCvPoint(), cvScalar(0,0,255,0), 1, 8, 0 );
				
				cvCircle( img, s.projectPosition( pivot  ).getCvPoint(), (int) Math.round(trad / s.getScale()), cvScalarAll(255),       1, 8, 0 );
				cvCircle( img, s.projectPosition( pivot  ).getCvPoint(), 3,                                     cvScalar(255,0,0,0),   -1, 8, 0 );
				cvCircle( img, s.projectPosition( gpoint ).getCvPoint(), 3,                                     cvScalar(255,255,0,0), -1, 8, 0 );
				
				Vector2 v = control.getDestination();
				cvLine( img, s.projectPosition(pos).getCvPoint(), s.projectPosition(v).getCvPoint(), cvScalar(0,255,0,255), 1, 8, 0 );
				
				cvCircle( img, s.projectPosition( Vector2.ZERO ).getCvPoint(), 3, cvScalarAll( 255 ), 1, 8, 0 );
				
				Vector2 ws = control.getRobotInterface().getWheelSpeeds();
				
				ws = ws.times( -1.0 / control.getProperty("maxWheelSpeed").getValue() );
				
				int wheelDisplayHeight = 50, wheelDisplayWidth = 10, wheelDisplaySpacing = 5;
				int wheelDisplayX = 20, wheelDisplayY = wheelDisplayHeight + 10;
				CvScalar minusColour = cvScalar(0,0,255,0), plusColour = cvScalar(0,255,0,0);
				
				cvRectangle(
						img,
						cvPoint( wheelDisplayX, wheelDisplayY ),
						cvPoint(
							wheelDisplayX + wheelDisplayWidth,
							wheelDisplayY + (int)(wheelDisplayHeight*ws.getX())
						),
						ws.getX() > 0.0 ? minusColour : plusColour,
						-1, 8, 0
				);
				
				
				cvRectangle(
						img,
						cvPoint(
							wheelDisplayX + wheelDisplayWidth + wheelDisplaySpacing,
							wheelDisplayY
						),
						cvPoint(
							wheelDisplayX + wheelDisplaySpacing + wheelDisplayWidth*2,
							wheelDisplayY + (int)(wheelDisplayHeight*ws.getY())
						),
						ws.getY() > 0.0 ? minusColour : plusColour,
						-1, 8, 0
				);
					
				
				
			}
		}
	}
	
	public ControlGui( World iworld, Control control, ImageViewer viewer ) {
		this( iworld, control, viewer, null );
	}
	
	public ControlGui( World iworld, Control ctrl, ImageViewer viewer, ControlPanel panel ) {
		if( panel == null ) {
			panel = new ControlPanel();
			panel.setTitle("Robot Control");
		} 
		
		this.world   = iworld;
		this.control = ctrl;
		this.viewer  = viewer;		
		this.panel = panel;
	}
	
	public void initialise() {
		this.viewer.addNewIplImageListener( new DebugGraphics() );
		
		for( String key : control.getAllKeys() )
			new PropertySpinnerCallback( control.getProperty( key ), panel );
		
		panel.addButton("Show Debug Info", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked( JButton button ) {
				showDebugGraphics = !showDebugGraphics;
				if( showDebugGraphics )
					button.setText( "Hide Debug Info" );
				else
					button.setText( "Show Debug Info" );
			}
		});
				
		panel.addButton("Switch from " + control.getRobotState().getTeam().toString(), new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked( JButton button ) {
				RobotState.Team newTeam = control.getRobotState().getTeam().getOtherTeam();
				
				control.setTeam( newTeam );
				button.setText( "Switch from " + newTeam.toString() );
			}
		});
		
		this.graphDisplay = new GraphDisplay("Delta Phi", new Vector2(20,viewer.getHeight() - 140), new Vector2( viewer.getWidth() - 40, 100 ), 680, -Math.PI, Math.PI );
	}
	
	public static class PropertySpinnerCallback implements ControlPanel.SpinnerCallback {
		private Control.Property prop;
		
		PropertySpinnerCallback( Control.Property property, ControlPanel panel ) {
			this.prop = property;
			panel.addSpinner( property.getKey(), prop.getValue(), prop.getMin(), prop.getMax(), prop.getStep(), this );
		}
		
		@Override
		public void valueChanged( SpinnerNumberModel spinner, double newValue ) {
			prop.setValue( newValue );
		}
	}
	
	public void dispose() {
		panel.dispose();
	}
	
	public ControlPanel getPanel() {
		return panel;
	}
}
