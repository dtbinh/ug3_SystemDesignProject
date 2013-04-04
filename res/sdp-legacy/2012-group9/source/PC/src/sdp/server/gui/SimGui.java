package sdp.server.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;

import sdp.server.PcServer;
import sdp.server.RobotInterface;
import sdp.server.RobotState;
import sdp.server.math.Vector2;
import sdp.server.simulator.SimWorld;
import sdp.server.strategy.DefendPenaltyStrategy;
import sdp.server.strategy.NormalMatchStrategy;
import sdp.server.strategy.PenaltyKickStrategy;

public class SimGui implements Gui {
	private PcServer        server;
	private final SimWorld  world;
	private ControlPanel    generalPanel;
	private ImageViewer     viewer;
	private ControlGui      controlGui;
	
	public RobotState.Team getUserControlledTeam() {
		RobotState.Team t = server.getControl().getRobotState().getTeam();
		if( t == RobotState.Team.YELLOW )
			return RobotState.Team.BLUE;
		else
			return RobotState.Team.YELLOW;
	}
	
	public SimGui( PcServer iserver, SimWorld _world ) {
		this.server = iserver;
		this.world = _world;
		this.generalPanel = new ControlPanel();
		this.viewer       = new ImageViewer( world.getScreenProjection() );
		this.controlGui   = new ControlGui(world, server.getControl(), viewer );
	}
	
	@Override
	public void initialise() {
		controlGui.initialise();
		
		generalPanel.setTitle("General");
		
		generalPanel.addButton( "Shutdown", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton button) {
				shutdown();
			}
		});
		
		generalPanel.addButton( "Pause", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton button) {
				world.setPaused(!world.isPaused());
				
				// Change the text to reflect the action it will do.
				button.setText(world.isPaused() ? "Resume" : "Pause");
			}
		});
		
		generalPanel.addButton( "Reset Simulation", new ControlPanel.ButtonCallback() {
			public void buttonClicked(JButton button) {
				world.reset();
				
			}
		});
		
		generalPanel.addButton( "Mode: Friendly", new ControlPanel.ButtonCallback() {
			public void buttonClicked( JButton button ) {
				((SimWorld) server.getWorld()).setMode("friendly");
			}
		});
		
		generalPanel.addButton( "Mode: Penalty", new ControlPanel.ButtonCallback() {
			public void buttonClicked( JButton button ) {
				((SimWorld) server.getWorld()).setMode("penalty");;
			}
		});
		
		
		generalPanel.addButton( "Strategy: Remove Strategy", new ControlPanel.ButtonCallback() {
			public void buttonClicked( JButton button ) {
				server.setStrategy( null );
			}
		});
		
		generalPanel.addButton( "Strategy: Defend Penalty", new ControlPanel.ButtonCallback() {			
			@Override public void buttonClicked(JButton button) {
				server.setStrategy( new DefendPenaltyStrategy( server ) );
			}
		});
		
		generalPanel.addButton( "Strategy: Penalty Kick", new ControlPanel.ButtonCallback() {			
			@Override public void buttonClicked(JButton button) {
				server.setStrategy( new PenaltyKickStrategy( server ) );
			}
		});
		
		generalPanel.addButton( "Strategy: Normal Match", new ControlPanel.ButtonCallback() {			
			@Override public void buttonClicked(JButton button) {
				server.setStrategy( new NormalMatchStrategy( server, Math.signum( server.getControl().getRobotState().getPosition().getX() ) ) );
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
		
		generalPanel.addWindowListener( winListener );
		controlGui.getPanel().addWindowListener( winListener );
		viewer.addWindowListener( winListener );
		generalPanel.pack();
		
		
		UserInputListener uil = new UserInputListener(); 
		viewer.getCanvas().addMouseListener( uil );
		viewer.getCanvas().addMouseMotionListener( uil );
		viewer.getCanvas().addKeyListener( uil );
	}
	
	private class UserInputListener implements KeyListener, MouseListener, MouseMotionListener {
		//private boolean mouseFix;
		
		void handleMovement( KeyEvent e, float d ) {
			RobotInterface intf = world.getRobotInterface( getUserControlledTeam() );
			Vector2 ws = intf.getWheelSpeeds();
			if( e.getKeyCode() == KeyEvent.VK_UP ) {
				ws = ws.plus( new Vector2( d, d ) );
			} else if( e.getKeyCode() == KeyEvent.VK_DOWN ) {
				ws = ws.minus( new Vector2( d, d ) );
			} else if( e.getKeyCode() == KeyEvent.VK_LEFT ) {
				ws = ws.plus( new Vector2( -d, d ) );
			} else if( e.getKeyCode() == KeyEvent.VK_RIGHT) {
				ws = ws.plus( new Vector2( d, -d ) );
			} else if( e.getKeyCode() == KeyEvent.VK_SPACE) {
				intf.kick();			
			}
			intf.setWheelSpeeds( ws );
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			handleMovement( e, 11.0f );
		}

		@Override
		public void keyReleased(KeyEvent e) {
			handleMovement( e, -11.0f );
		}
		
		@Override
		public void mouseDragged( MouseEvent e ) {
			world.dragMouseTarget( e.getX(), e.getY() );
		}
		
		@Override
		public void mousePressed( MouseEvent e ) {
			world.findMouseTarget( e.getX(), e.getY() );
		}
		
		@Override
		public void mouseReleased(MouseEvent arg0) {
			world.removeMouseTarget();
		}
	
		@Override public void keyTyped(KeyEvent e)          {}
		@Override public void mouseMoved(MouseEvent arg0)   {}
		@Override public void mouseClicked(MouseEvent arg0) {}
		@Override public void mouseEntered(MouseEvent arg0) {}
		@Override public void mouseExited(MouseEvent arg0)  {}
	}
	
	private void shutdown() {
		server.shutdown();
	}
	
	public ControlPanel getStrategyPanel() {
		return generalPanel;
	}
	
	public ImageViewer getImageViewer() {
		return viewer;
	}

	@Override
	public void update() {
		viewer.showImage( world.getImage() );
	}

	@Override
	public void dispose() {
		generalPanel.dispose();
		viewer.dispose();
		world.dispose();
		controlGui.dispose();
	}
}
