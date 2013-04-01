package sdp.server.gui;

import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;

import sdp.server.PcServer;
import sdp.server.nxt.Communication;
import sdp.server.nxt.NxtWorld;
import sdp.server.strategy.DefendPenaltyStrategy;
import sdp.server.strategy.NormalMatchStrategy;
import sdp.server.strategy.PenaltyKickStrategy;
import sdp.server.strategy.Strategy;
import sdp.server.strategy.StrategyInterface;
import sdp.server.strategy.action.KickBallAction;

/** Simple class that wraps CameraGui and VisionGui as well as adding a few other controls, to provide
 * a user interface for NxtWorld's parameters.
 * @author s0906176
 *
 */
public class NxtGui implements Gui {
	private PcServer          server;
	private NxtWorld          world;
	private CameraGui         cam;
	private VisionGui         vis;
	private ControlGui        ctrl;
	private ControlPanel      generalPanel;
	private int               fpsFrames = 0;
	private double            fpsTime   = 0.0;
	private StrategyInterface newStrategy = null;
	private boolean           changeStrategy = false;
	
	public NxtGui( PcServer iserver, NxtWorld iworld ) {
		this.server  = iserver;
		this.world   = iworld;
		
		cam          = new CameraGui( world.getCamera() );
		vis          = new VisionGui( world.getVision() );
		ctrl         = new ControlGui( world, server.getControl(), vis.getViewer() );
		generalPanel = new ControlPanel();
	}
	
	@Override
	public void initialise() {
		cam.initialise();
		vis.initialise();
		ctrl.initialise();
		
		generalPanel.setTitle("General");
		
		Communication comm = world.getCommunication(); 
		String connectButtonCaption;
		if( comm.getDummyMode() ) {
			connectButtonCaption = "Connect (Not Connected)";
		} else {
			connectButtonCaption = "Disconnect (Connected)";
		}
		
		generalPanel.addButton( connectButtonCaption, new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton button) {
				Communication comm = NxtGui.this.world.getCommunication();
				comm.setDummyMode( !comm.getDummyMode() );
				
				if( comm.getDummyMode() ) {
					button.setText( "Connect (Not Connected)" );
				} else {
					button.setText( "Disconnect (Connected)" );
				}
			}
		});
		
		generalPanel.addButton( "Force Kick", new ControlPanel.ButtonCallback() {
			public void buttonClicked( JButton button ) {
				NxtGui.this.world.getCommunication().kick();
			}
		});
		
		generalPanel.addButton( "Shutdown", new ControlPanel.ButtonCallback() {
			public void buttonClicked( JButton button ) {
				shutdown();
			}
		});
		
		generalPanel.addButton( "Remove Strategy", new ControlPanel.ButtonCallback() {
			public void buttonClicked( JButton button ) {
				newStrategy = null;
				changeStrategy = true;
			}
		});
		
		generalPanel.addButton( "Defend Penalty", new ControlPanel.ButtonCallback() {			
			@Override public void buttonClicked(JButton button) {
				newStrategy = new DefendPenaltyStrategy( server );
				changeStrategy = true;
			}
		});
		
		generalPanel.addButton( "Penalty Kick", new ControlPanel.ButtonCallback() {			
			@Override public void buttonClicked(JButton button) {
				newStrategy = new PenaltyKickStrategy( server );
				changeStrategy = true;
			}
		});
		
		generalPanel.addButton( "Normal Match", new ControlPanel.ButtonCallback() {			
			@Override public void buttonClicked(JButton button) {
				newStrategy = new NormalMatchStrategy( server, Math.signum( server.getControl().getRobotState().getPosition().getX() ) );
				changeStrategy = true;
			}
		});
		
		WindowListener winListener = new WindowListener() {
			@Override public void windowClosed(WindowEvent arg0) {}
			@Override public void windowActivated(WindowEvent arg0) {}
			@Override public void windowOpened(WindowEvent arg0) {}
			@Override public void windowIconified(WindowEvent arg0) {}
			@Override public void windowDeiconified(WindowEvent arg0) {}
			@Override public void windowDeactivated(WindowEvent arg0) {}
			@Override public void windowClosing(WindowEvent arg0) {
				shutdown();
			}
		};
		
		vis.getViewer().getContentPane().addHierarchyBoundsListener( new HierarchyBoundsListener() {
			@Override public void ancestorResized(HierarchyEvent arg0) {}
			@Override public void ancestorMoved(HierarchyEvent arg0) {
				arrangeWindows();
			}
		});
			 
		generalPanel.addWindowListener( winListener );
		vis.getPanel().addWindowListener( winListener );
		ctrl.getPanel().addWindowListener( winListener );
		vis.getViewer().addWindowListener( winListener );
		cam.getPanel().addWindowListener( winListener );
		
		generalPanel.pack();
		vis.getPanel().pack();
		cam.getPanel().pack();
		arrangeWindows();
	}
	
	public ControlPanel getGeneralPanel() {
		return generalPanel;
	}
	
	@Override
	public void update() {
		if( changeStrategy ) {
			server.setStrategy( newStrategy );
			changeStrategy = false;
		}
		
		if( fpsTime >= 1.0 ) {
			double fps = fpsFrames / fpsTime;
			vis.getViewer().setTitle("Vision Output [FPS: " + String.format("%2.1f", fps) + " / " + String.format("%3.1f ms", 1000.0/fps ) + "]");
			fpsTime = 0.0;
			fpsFrames = 0;
		}
		fpsTime += world.getTimeStep();
		fpsFrames ++;
		
		vis.update();
		cam.update();
	}
	
	@Override
	public void dispose() {
		vis.dispose();
		cam.dispose();
		ctrl.dispose();
		generalPanel.dispose();
	}
	
	public ImageViewer getImageViewer() {
		return vis.getViewer();
	}
	
	private void arrangeWindows() {
		int viewerHeight = vis.getViewer().getHeight();
		int viewerX      = vis.getViewer().getX();
		int visHeight    = cam.getPanel().getHeight() + viewerHeight;
		int camWidth     = vis.getViewer().getWidth() - generalPanel.getWidth();
		int x            = viewerX - vis.getPanel().getWidth();
		if( x < 0 ) {
			viewerX += -x;
			x = 0;
			vis.getViewer().setLocation( viewerX, vis.getViewer().getY() );
		}
		
		
		int y = vis.getViewer().getY();
		
		vis.getPanel().setBounds( x, y, vis.getPanel().getWidth(), visHeight );
		ctrl.getPanel().setBounds( x + vis.getViewer().getWidth() + vis.getPanel().getWidth(), y, ctrl.getPanel().getWidth(),visHeight );
		cam.getPanel().setBounds(viewerX, y + viewerHeight, camWidth, cam.getPanel().getHeight() );
		generalPanel.setBounds( viewerX + camWidth, y + viewerHeight, generalPanel.getWidth(), cam.getPanel().getHeight() );
	}
	
	private void shutdown() {
		server.shutdown();
	}

	@Override public ControlPanel getStrategyPanel() {
		return generalPanel;
	}
}
