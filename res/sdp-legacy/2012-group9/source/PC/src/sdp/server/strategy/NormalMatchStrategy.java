package sdp.server.strategy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;

import static com.googlecode.javacv.cpp.opencv_core.*;

import sdp.server.Control;
import sdp.server.PcServer;
import sdp.server.ScreenProjection;
import sdp.server.gui.ControlPanel;
import sdp.server.gui.ImageViewer;
import sdp.server.math.Vector2;
import sdp.server.strategy.action.Condition;
import sdp.server.strategy.action.KickBallAction;
import sdp.server.strategy.action.StopAction;
import sdp.server.strategy.action.TimeoutCondition;

public class NormalMatchStrategy extends Strategy {
	
	enum State {
		DO_NOTHING,
		FOLLOW_BALL
	}
	
	private UserInputListener  uil;
	private State              state = State.FOLLOW_BALL;
	private boolean            newState = true;
	private VectorFieldDisplay vfd;
	public  int                action = KickBallAction.IGNORE_OPPONENT;
	
	
	private class UserInputListener implements MouseListener, MouseMotionListener, KeyListener {
		Vector2 clickStart;
		Vector2 clickEnd;
		boolean clearActions;
		
		@Override public void keyPressed(KeyEvent e) {
			
		}

		@Override public void keyReleased(KeyEvent e) {
			
		}

		@Override public void keyTyped(KeyEvent e) {
			if( e.getKeyChar() == 'w' ) {
				ctrl.toggleFlags( Control.F_AVOID_WALLS );
				System.out.println("NormalMatchStrategy.UserInputListener.keyTyped(): Wall avoidance is now: " + ctrl.isAnyFlagSet( Control.F_AVOID_WALLS ) );
			}
		}

		@Override public void mouseDragged(MouseEvent e) {
			if( clickStart != null )
				clickEnd = new Vector2( e.getX(), e.getY() );
		}

		@Override public void mouseMoved(MouseEvent e) {
			
		}

		@Override public void mouseClicked(MouseEvent e) {
			
		}

		@Override public void mouseEntered(MouseEvent e) {
			
		}

		@Override public void mouseExited(MouseEvent e) {
			
		}

		@Override public void mousePressed(MouseEvent e) {
			if( e.getButton() == MouseEvent.BUTTON1 ) {
				if( ( e.getModifiers() & MouseEvent.SHIFT_MASK ) != 0 ) {
					clearActions = false;
				} else {
					clearActions = true;
				}
				
				clickStart = new Vector2( e.getX(), e.getY() );
			}
		}

		@Override public void mouseReleased(MouseEvent e) {
			if( e.getButton() == MouseEvent.BUTTON1 ) {
				ScreenProjection screenProj = world.getScreenProjection();
				clickEnd = new Vector2( e.getX(), e.getY() );
				Vector2 pos = screenProj.unprojectPosition( clickStart );
				
				if( clearActions )
					clearActions();
				
				if( clickEnd.minus( clickStart ).computeNorm() >= 5 ) {
					double angle = clickEnd.minus( clickStart ).computeAngle();
					angle = screenProj.unprojectAngle( angle );
					approach( pos, angle );
				} else {
					unsetFlags( Control.F_STOP_AT_GOAL );
					driveTo( pos );
				}
				
				clickStart = clickEnd = null;
			}
		}
		
		public void drawStuff() {
			if( clickStart != null ) {
				ScreenProjection screenProj = world.getScreenProjection();
				Vector2 startPos;
				if( clearActions )
					startPos = ctrl.getCurrentNavPoint();
				else
					startPos = getFinalPosition();
				server.getGui().getImageViewer().drawLine( startPos, screenProj.unprojectPosition(clickStart), 255, 255, 0);
				
				if( clickEnd != null && clickEnd.minus( clickStart ).computeNorm() >= 5 )	
					server.getGui().getImageViewer().drawLine( screenProj.unprojectPosition(clickStart), screenProj.unprojectPosition(clickEnd), 0, 255, 0);
			}			
		}
	}
	
	public NormalMatchStrategy( PcServer server, double ourSide ) {
		super( server );
		this.ourSide = ourSide;
		
	}
	
	public void enable() {
		super.enable();
		ControlPanel panel = server.getGui().getStrategyPanel();
		
		panel.addButton("Start/Stop Ball Following", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked(JButton button) {
				if( state == State.FOLLOW_BALL )
					state = State.DO_NOTHING;
				else
					state = State.FOLLOW_BALL;
				newState = true;
			}
		});
		
		uil = new UserInputListener();
		server.getGui().getImageViewer().getCanvas().addMouseListener( uil );
		server.getGui().getImageViewer().getCanvas().addMouseMotionListener( uil );
		server.getGui().getImageViewer().getCanvas().addKeyListener( uil );
		server.getGui().getImageViewer().addNewIplImageListener( vfd = new VectorFieldDisplay() );
	}
	
	public void disable() {
		super.disable();
		ControlPanel panel = server.getGui().getStrategyPanel();
		panel.removeButton( "Start/Stop Ball Following" );
		server.getGui().getImageViewer().getCanvas().removeMouseListener( uil );
		server.getGui().getImageViewer().getCanvas().removeMouseMotionListener( uil );
		server.getGui().getImageViewer().getCanvas().removeKeyListener( uil );
		server.getGui().getImageViewer().removeNewIplImageListener( vfd );
	}
	
	
	private class VectorFieldDisplay implements ImageViewer.NewIplImageListener {
		@Override
		public void newIplImage(IplImage image) {
			if( isDrawingDebugInfo() ) {
				Vector2 pitch = world.getPitchTopRight();
				double  resolution = 0.03;
				
				Vector2 saveRobPos = ctrl.getRobotState().getPosition();
				double  saveRobRot = ctrl.getRobotState().getRotation();
				
				ctrl.getRobotState().reset( Vector2.ZERO, 0.0 );
				
				ScreenProjection s = world.getScreenProjection();
				
				for( double x = - pitch.getX() ; x < pitch.getX() ; x += resolution ) {
					for( double y = - pitch.getY() ; y < pitch.getY() ; y += resolution ) {
						Vector2 nav = new Vector2( x, y );
						Vector2 pos = nav.minus( new Vector2( ctrl.getNavPointOffset(), 0.0 ) );
						ctrl.getRobotState().reset( pos, 0.0 );
						Vector2 dir = getFinalPosition().minus( nav );
						Vector2 p2 = dir.times(resolution * 0.75).plus( nav );
						Vector2 p3 = nav.plus( dir.computeNormal().times( 0.002 ) );
						
						CvPoint c1 = s.projectPosition( nav ).getCvPoint();
						CvPoint c2 = s.projectPosition( p2  ).getCvPoint();
						CvPoint c3 = s.projectPosition( p3  ).getCvPoint();
						
						cvLine( image, c1, c2, cvScalar(0,255,255,255), 1, 8, 0);
						cvLine( image, c1, c3, cvScalar(0,255,255,255), 1, 8, 0);
					}
				}
				
				ctrl.getRobotState().reset( saveRobPos, saveRobRot );
			}
		}
		
	}
	
	@Override
	public void update() {
		super.update();
		uil.drawStuff();
		
		if( newState ) {
			if( state == State.FOLLOW_BALL ) {
				clearActions();
				ctrl.resetFlags( Control.F_DEFAULT );
				ctrl.unsetFlags( Control.F_STATIONARY );
				ctrl.setCurrentSpeed( 1.0 );
				driveTo( new Vector2( ctrl.getCurrentPivot().getX(), 0.0 ) );
				setAbortCondition( new TimeoutCondition( 0.2 ) );
				pushAction( new KickBallAction( action ) );
				setAbortCondition( new Condition() {
					@Override public boolean test(Strategy strategy) {
						return state != State.FOLLOW_BALL;
					}
				});
				pushAction( new StopAction() );
				
			}
			
			newState = false;
		}
		
	}
	
	
}
