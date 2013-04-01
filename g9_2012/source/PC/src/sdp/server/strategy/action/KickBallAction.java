package sdp.server.strategy.action;

import sdp.server.Control;
import sdp.server.RobotState;
import sdp.server.World;
import sdp.server.math.Line;
import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;
import sdp.server.strategy.Strategy;

public class KickBallAction extends StoredTagAction implements MovementAction {
	public static final int IGNORE_OPPONENT = 0;
	public static final int AROUND_OPPONENT = 1;
	
	private static final double APPROACH_COEFF        = 0.7;
	public double  sigmoidK        = -32.0;
	public double  maxDist         = 0.2;
	public int     kickPointOption = IGNORE_OPPONENT;
		
	private Vector2 ballPos, robPos, robSz, pitch;
	private double  ballRad, ourSide;
	private Line    ballGoalLine;
	private double  ballGoalAngle, maxY, minApproachDist, approachDist;
	private Vector2 kickAt;
	
	private World   world;
	private Control ctrl;
		
	public KickBallAction() {
		
	}
		
	public KickBallAction( int kickPointOption ) {
		this.kickPointOption = kickPointOption;
	}
		
	private double misalignment( Vector2 point ) {
		assert point        != null;
		assert ballPos      != null;
		assert ballGoalLine != null;
		assert ctrl         != null;
		
		double dphi = goalAngleAbsDiff( point );
		double dist = ballGoalLine.distanceTo(point);
		
		return Math.min( dphi / Math.PI, 1.0 ) * ( Math.min( dist / minApproachDist, 1.0) + 0.5);
	}
	
	private double goalAngleAbsDiff( Vector2 point ) {
		return Math.abs( MathUtils.angleDiff( ballGoalLine.pointAt(0.02).minus( point ).computeAngle(), ballGoalAngle ) );
	}
		
	private void updateLocals( Strategy strategy ) {
		world           = strategy.getWorld();
		ctrl            = strategy.getControl();
		ballPos         = world.getBallState().getPosition();
		//ballVel         = world.getBallState().getLinearVelocity();
		ballRad         = world.getBallState().getRadius();
		robPos          = ctrl.getCurrentPivot();
		robSz           = ctrl.getRobotState().getSize();
		ourSide         = strategy.getOurSide();
		pitch           = world.getPitchTopRight();
		minApproachDist = ctrl.getTurnRadius() * APPROACH_COEFF;
		
		ballPos = ballPos.plus( world.getBallState().getLinearVelocity().times( world.getTimeStep() * 4.0 ) );
	}
	
	private void computeKickAt( Strategy strategy ) {
		maxY = pitch.getY() - robSz.getX() * .15;
		boolean stuckAgainstHorizWall = Math.abs( ballPos.getY() ) >= maxY;
		
		if( kickPointOption == IGNORE_OPPONENT )
			kickAt = strategy.computeNoOpponentKickPoint( ballPos );
		else
			kickAt = strategy.computeWithOpponentKickPoint( ballPos );
		
		if( !stuckAgainstHorizWall ) {
			
		} else {
			ballPos = new Vector2( ballPos.getX(), ballPos.getY() - Math.signum( ballPos.getY() ) * ( ballRad  + robSz.getX() * .15 ) * 0.5 );
			kickAt  = new Vector2( - ourSide * pitch.getX(), ballPos.getY() );
		}
		
		ballGoalLine  = Line.fromTwoPoints( ballPos, kickAt );
		ballGoalAngle = ballGoalLine.computeAngle();
	}
	
	@Override
	public Vector2 getPosition( Strategy strategy ) {
		
		updateLocals( strategy );
		computeKickAt( strategy );
		
		Vector2   approachPoint;
		double    robMisalignment    = misalignment( robPos );
		approachDist = minApproachDist * Math.pow( robMisalignment, 1.0 ) * 3.0;
		
		Vector2   robBallNormal      = ballPos.minus( robPos ).computeUnit().computeNormal().times( approachDist );
		Vector2[] potentialPoints    = { ballPos.plus( robBallNormal ), ballPos.minus( robBallNormal ) };
		double[]  potentialMisalign  = { goalAngleAbsDiff( potentialPoints[ 0 ] ), goalAngleAbsDiff( potentialPoints[ 1 ] ) };
		Vector2   idealApproachPoint = ballGoalLine.pointAt( 0.1 - approachDist  );
		int       pointIndex;
		
		//strategy.getServer().getGui().getImageViewer().drawCircle( potentialPoints[0], 0.02, 255, 255, 255);
		//strategy.getServer().getGui().getImageViewer().drawCircle( potentialPoints[1], 0.02, 255, 255, 255);
		
		if( potentialMisalign[ 0 ] < potentialMisalign[ 1 ] )
			pointIndex = 0;
		else
			pointIndex = 1;
				
		approachPoint = potentialPoints[ pointIndex ];
		
		if( potentialMisalign[ pointIndex ] > goalAngleAbsDiff( robPos ) || ballGoalLine.signedDistanceTo(robPos) * ballGoalLine.signedDistanceTo(approachPoint) < 0.0 ) {
			approachPoint = idealApproachPoint;
		} 
				 
		Line approachLine = Line.fromTwoPoints( approachPoint, ballPos );
		double t = approachLine.solveWithY( maxY );
		
		double ballOnLine = ballPos.minus( approachPoint ).computeNorm();
		if( !Double.isNaN( t ) && t > 0.0 && t < ballOnLine )
			approachPoint = approachLine.pointAt( t );
				
		t = approachLine.solveWithY( - maxY );
		if( !Double.isNaN( t ) && t > 0 && t < ballOnLine )
			approachPoint = approachLine.pointAt( t );
		
		Vector2 ballField = approachPoint.minus( robPos ).computeUnit();
		
		RobotState opp      = world.getRobotState( ctrl.getRobotState().getTeam().getOtherTeam() );		
		
		if( opp.isDetected() ) {
			Vector2 oppPos   = opp.getPosition();
			Vector2 edge     = oppPos.minus( robPos );
			double  dist     = edge.computeNorm() + 5e-3;
			double  exponent = 2.0;
			double  oneDist  = ctrl.getTurnRadius() * 0.9;
			double  strength = Math.pow( oneDist, exponent );
			Vector2 force;
			
			if( dist > 0.0 )
				edge = edge.times( 1.0 / dist );
			
			force = edge.times( - strength / Math.pow( dist, exponent ) );
			
			return force.plus( ballField ).plus( robPos );
		} else {
			return ballField.computeUnit().plus( robPos );
		}
	}
	
	@Override
	protected boolean applyActionNoAbort( Strategy strategy ) {
		Control ctrl = strategy.getControl();
		ctrl.setDestination( getPosition( strategy ) );
		ctrl.unsetFlags( Control.F_STATIONARY | Control.F_REVERSE | Control.F_STOP_AT_GOAL );
		
		//ctrl.setCurrentSpeed( Math.min( ballPos.minus(robPos).computeNorm() / 0.5 , 0.5) + 0.5 );
		
		if( strategy.shouldKick( kickAt, 0.55 ) )
			strategy.getControl().kick();
		
		strategy.getServer().getGui().getImageViewer().drawCircle( kickAt, 0.02, 0, 255, 255);
		strategy.getServer().getGui().getImageViewer().drawCircle( ballPos, 0.02, 0, 255, 255);
		
		return false;
	}
	
}
