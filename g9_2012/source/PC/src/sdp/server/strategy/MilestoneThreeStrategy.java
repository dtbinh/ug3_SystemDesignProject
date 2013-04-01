package sdp.server.strategy;

/*
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;

import sdp.server.PcServer;
import sdp.server.RobotState.Team;
import sdp.server.gui.ControlPanel;
import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;
*/
public class MilestoneThreeStrategy {
	/*
	public Vector2[]    obstaclePositions = { null, null };
	public double[]     obstacleRadii     = { 0.0, 0.0 };
	public ControlPanel panel;
	public double       ourSide = -1.0;
	public boolean      avoidRobot   = false;
	private State       state = State.DO_NOTHING;
	private Vector2     gotoPos;
	private boolean     newState = false;
	private Vector2     oldBallPos;
	
	enum State {
		DO_NOTHING,
		GOTO,
		KICK_BALL
	}
	
	
	@Override
	public void disable() {
		super.disable();
		panel.dispose();
	}
		
	public MilestoneThreeStrategy(PcServer server) {
		super(server);
		
		panel = new ControlPanel();
		panel.setTitle( "Strategy Controls" );
		
		panel.addSpinner( "Our Side", 0.0, -1.0, 1.0, 1.0, new ControlPanel.SpinnerCallback() {
			@Override public void valueChanged( SpinnerNumberModel number, double newValue) {
				if( newValue <= 0.5 )
					ourSide = -1.0;
				else if( newValue >= 0.5 )
					ourSide = 1.0;
				else
					ourSide = 0.0;
			}
		});
		
		panel.addButton("Kick ball (w/ robot)", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked( JButton button ) {
				avoidRobot = true;
				state      = State.KICK_BALL;
				newState   = true;
			}
		});
		
		panel.addButton("Kick ball (w/o robot)", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked( JButton button ) {
				avoidRobot = false;
				state      = State.KICK_BALL;
				newState   = true;
			}
		});
		
		panel.addButton("Go to other side (w/ robot)", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked( JButton button ) {
				gotoOtherSide();
			}
		});
		
		panel.addButton("Go to ball (w/ robot)", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked( JButton button ) {
				gotoBall();
			}
		});
		
		panel.addButton("Do nothing", new ControlPanel.ButtonCallback() {
			@Override public void buttonClicked(JButton button) {
				state    = State.DO_NOTHING;
				newState = true;
			}
		});
	}
	
	public void gotoOtherSide() {
		double x = - Math.signum( ctrl.getCurrentPosition().getX() ) * world.getPitchTopRight().getX() * 0.8;
		double y = ctrl.getCurrentPosition().getY();
		
		gotoPos    = new Vector2( x, y );
		avoidRobot = true;
		newState   = true;
		state      = State.GOTO;
	}
	
	public void gotoBall() {
		gotoPos    = world.getBallState().getPosition();
		avoidRobot = true;
		newState   = true;
		state      = State.GOTO;
	}
	
	public void kickBall() {
		double kickTowards;
		Vector2 ballPos = world.getBallState().getPosition();
		double  ballRad = world.getBallState().getRadius();
		Vector2 robPos  = ctrl.getRobotGoalPoint();
		Vector2 pitch   = world.getPitchTopRight();
		
		if( ourSide == 0.0 )
			kickTowards = Math.signum(ballPos.getX() - robPos.getX());
		else
			kickTowards = - ourSide;
		
		Vector2 oppCenter = new Vector2( kickTowards * pitch.getX(), 0 );
		Vector2 kickLine  = ballPos.minus( oppCenter ).computeUnit();
		Vector2 toBall    = ballPos.minus( robPos ).computeUnit();
		double  kickFromDistance = 0.2 + ctrl.getGoalPointOffset();
		Vector2 kickFrom         = ballPos.plus( kickLine.times( ballRad  + kickFromDistance ) ).plus(toBall.times(ctrl.getGoalPointOffset() - ctrl.getPivotOffset()));
		double  kickAngle        = kickLine.times( -1.0 ).computeAngle();
		
		double  distToBall       = ballPos.minus( robPos ).computeNorm();
		if( distToBall >= 0.015 + ctrl.getGoalPointOffset() || MathUtils.angleDiff( toBall.computeAngle(), kickAngle ) >= Math.PI/16.0  ) {
			enqueueGoal(
				new StrategyGoal( StrategyGoal.Type.GOTO, kickFrom, kickAngle, 1.0 ),
				obstaclePositions,
				obstacleRadii
			);
		}
		enqueueGoal( new StrategyGoal( StrategyGoal.Type.GOTO, ballPos.minus(kickLine.times(0.01)), 1.0, false ) );
		enqueueGoal( new StrategyGoal( StrategyGoal.Type.KICK ) );
	}
	
	public void update() {
		if( this.enabled ) {
			Vector2 ballPos = world.getBallState().getPosition();
			double  ballChange;
			if( oldBallPos != null )
				ballChange = oldBallPos.minus(ballPos).computeNorm();
			else
				ballChange = 0.0;
			
					
			
			obstaclePositions[ 0 ] = world.getBallState().getPosition();
			obstacleRadii    [ 0 ] = world.getBallState().getRadius() + 0.05;
			
			if( avoidRobot ) {
				if( ctrl.getRobotState().getTeam() == Team.YELLOW )
					obstaclePositions[ 1 ] = world.getRobotState( Team.BLUE ).getPosition();
				else
					obstaclePositions[ 1 ] = world.getRobotState( Team.YELLOW ).getPosition();
				
				obstacleRadii[ 1 ] = ctrl.getTurnRadius();
			} else {
				obstaclePositions[ 1 ] = world.getPitchTopRight().times( 2.0 );
				obstacleRadii    [ 1 ] = 0.01 - ctrl.getGoalPointOffset();
			}
			
			if( state == State.GOTO  ) {
				//if( gotoPos != null ) {
					clearQueue();
					enqueueGoal( new StrategyGoal( StrategyGoal.Type.GOTO, gotoPos, 1.0 ), obstaclePositions, obstacleRadii );
					runCurrentGoal();
				//}
			} else if( state == State.DO_NOTHING ) {
				if( newState ) {
					clearQueue();
					runCurrentGoal();
					newState = false;
				}
			} else if( state == State.KICK_BALL ) {
				//if( newState ) {
				if( getGoalCount() > 3 || getGoalCount() == 0 || ballChange > 0.05 ) {
					oldBallPos = ballPos;
					clearQueue();
					kickBall();
					runCurrentGoal();
				}
				//}
			}
			super.update();
		}
	}
	 */
}
