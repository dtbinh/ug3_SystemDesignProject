package sdp.server.strategy;

import java.util.ArrayList;
import javax.swing.JButton;

import sdp.server.BallState;
import sdp.server.Control;
import sdp.server.PcServer;
import sdp.server.RobotState;
import sdp.server.World;
import sdp.server.gui.ControlPanel;
import sdp.server.gui.ImageViewer;
import sdp.server.math.Line;
import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;
import sdp.server.strategy.action.*;

public class Strategy implements StrategyInterface {
	protected PcServer		      server;
	protected World			      world;
	protected Control		      ctrl;
	protected boolean             enabled        = false;
	protected double              ourSide        = 1.0; // 1 for right, -1 for left
	//private   double              currentSpeed   = 0.0; // Used for incremental speed increase
	private   ArrayList< Action > actions        = new ArrayList< Action >();
	private   boolean             actionExecuted = false;
	private   boolean             drawDebugInfo  = false;
			
	public Strategy( PcServer server) {
		this.server      = server;
		this.world       = server.getWorld();
		this.ctrl        = server.getControl();
	}
	
	public Control getControl() {
		return ctrl;
	}
	
	public World getWorld() {
		return world;
	}
	
	public PcServer getServer() {
		return server;
	}
	
	public double getOurSide() {
		return ourSide;
	}
	
	@Override
	public void enable() {
		if( !enabled ) {
			enabled = true;
			ControlPanel panel = server.getGui().getStrategyPanel();
			panel.addButton("Strategy Deb Inf", new ControlPanel.ButtonCallback() {
				@Override public void buttonClicked(JButton button) {
					drawDebugInfo = !drawDebugInfo;
				}
			});
		}
	}
	
	@Override
	public void disable() {
		if( enabled ) {
			enabled = false;
			ControlPanel panel = server.getGui().getStrategyPanel();
			panel.removeButton("Strategy Deb Inf");
			ctrl.setFlags( Control.F_STATIONARY );
		}
	}
	
	public double getGoalSize() {
		return 0.55;
	}

	public boolean isEnabled () {
		return enabled;
	}
	
	public int getActionCount() {
		return actions.size();
	}
	
	public boolean hasActions() {
		return !actions.isEmpty();
	}
	
	public Action pollAction() {
		if( hasActions() )
			return actions.get( 0 );
		else
			return null;
	}
	
	public Action popAction() {
		if( hasActions() ) {
			Action action = actions.get( 0 );
			actions.remove( 0 );
			return action;
		} else {
			return null;
		}
	}
	
	public boolean isDrawingDebugInfo() {
		return drawDebugInfo;
	}
	
	public void pushAction( Action newAction ) {
		if( !hasActions() )
			actionExecuted = false;
		actions.add( newAction );
	}
	
	public void driveTo( Vector2 position ) {
		pushAction( new DriveAction( position ) );
	}
	
	public void driveTo( Vector2 position, double speed ) {
		changeSpeed( speed );
		pushAction( new DriveAction( position  ) );
	}
	
	public void driveTo( Vector2 position, long flags ) {
		resetFlags( flags );
		pushAction( new DriveAction( position  ) );
	}
	
	public void driveTo( Vector2 position, long flags, double speed ) {
		changeSpeed( speed );
		resetFlags( flags );
		pushAction( new DriveAction( position ) );
	}
	
	public void approach( Vector2 position, double atAngle ) {
		pushAction( new ApproachAction( position, atAngle ) );
	}
	
	public void approach( Vector2 position, double atAngle, double atSpeed ) {
		changeSpeed( atSpeed );
		pushAction( new ApproachAction( position, atAngle ) );
	}
	
	public void approach( Vector2 position, double atAngle, long flags ) {
		resetFlags( flags );
		pushAction( new ApproachAction( position, atAngle ) );
	}
	
	public void approach( Vector2 position, double atAngle, long flags, double atSpeed ) {
		resetFlags( flags );
		changeSpeed( atSpeed );
		pushAction( new ApproachAction( position, atAngle ) );
	}
	
	public void setFlags( long newFlags ) {
		pushAction( new SetFlagsAction( newFlags ) );
	}
	
	public void unsetFlags( long newFlags ) {
		pushAction( new UnsetFlagsAction( newFlags ) );
	}
	
	public void resetFlags( long newFlags ) {
		pushAction( new ResetFlagsAction( newFlags ) );
	}
		
	public void changeSpeed( double speed ) {
		pushAction( new SpeedChangeAction( speed ) );
	}
	
	public void stop( double timeoutInSeconds ) {
		pushAction( new StopAction() );
		pushAction( new WaitAction( timeoutInSeconds ) );
	}
	
	public void wait( double timeoutInSeconds ) {
		pushAction( new WaitAction( timeoutInSeconds ) );
	}
	
	public void setTag( int newTag ) {
		if( hasActions() )
			setTag( getActionCount() - 1, newTag );
	}
	
	public void setAbortCondition( Condition newAbortCondition ) {
		if( hasActions() )
			actions.get( getActionCount() - 1 ).setAbortCondition( newAbortCondition );
	}
	
	public void setTag( int actionIndex, int tag ) {
		assert actionIndex >= 0 && actionIndex < getActionCount();
		actions.get( actionIndex ).setTag( tag );
	}
	
	public int getTag() {
		if( hasActions() )
			return getTag( getActionCount() - 1 );
		else
			return 0;
	}
	
	public int getTag( int actionIndex ) {
		assert actionIndex >= 0 && actionIndex < getActionCount();
		return actions.get( actionIndex ).getTag();
		
	}
		
	public void update() {	
		if( !actionExecuted ) {
			if( !hasActions() ) {
				ctrl.setFlags( Control.F_STATIONARY );
				actionExecuted = true;
			} else {
				actionExecuted = pollAction().applyAction( this );
			}
		}
		
		if( actionExecuted && ctrl.hasReachedGoal() ) {
			if( hasActions() ) {
				popAction();
				actionExecuted = false;
			}
		}
		
		if(drawDebugInfo ) {
			ImageViewer viewer    = server.getGui().getImageViewer();
			int nActions = getActionCount();
			Vector2 a = getPositionAfterAction( -1 );
			double  r = getRotationAfterAction( -1 );
			
			for( int i = 0 ; i < nActions ; ++ i ) {
				Vector2 b = getPositionAfterAction( i );
				viewer.drawCircle( a, 0.02, 0, 255, 0 );
				viewer.drawLine( a, a.plus( Vector2.fromAngle( r, 0.15 ) ), 0, 255, 0 );
				viewer.drawLine( a, b, 255, 255, 0 );
				r = getRotationAfterAction( i );
				a = b;
			}
			
			viewer.drawCircle( a, 0.02, 0, 255, 0 );
			viewer.drawLine( a, a.plus( Vector2.fromAngle( r, 0.15 ) ), 0, 255, 0 );
		}
	}	
	
	/** Computes the point to kick towards so as to score a goal. Does not take opponent into account (see
	 * computeWithOpponentKickToPoint), if opponent avoidance is desired.
	 * 
	 * @param position The position to kick from.
	 * @return The point to kick towards so as to score a goal from the given position, ignoring the
	 * opponent.
	 */
	public Vector2 computeNoOpponentKickPoint( Vector2 ballPos ) {
		double kickTowards = -ourSide;
		double goalSize    = 0.55 * 0.5;
		double ballY       = ballPos.getY();
		Vector2 pitch      = world.getPitchTopRight(); 
		return new Vector2( kickTowards * pitch.getX(), Math.signum( ballY ) * Math.min( goalSize, Math.abs( ballY ) ) );
	}
	
	/** Computes the point to kick towards, avoiding the opponent. If the opponent cannot be avoided (e.g. it's too
	 * close) then a wall-shot is computed.
	 * 
	 * @param position The position to kick from.
	 * @return The point to kick towards so as to score a goal from the given position.
	 */
	public Vector2 computeWithOpponentKickPoint( Vector2 ballPos ) {
		RobotState opponent = world.getRobotState( ctrl.getRobotState().getTeam().getOtherTeam() );
		Vector2    pitch    = world.getPitchTopRight();
		Vector2    oppPos   = opponent.getPosition();
		double     oppRad   = Math.max( opponent.getSize().getX(), opponent.getSize().getY() ) / 2.5;
		Vector2    kickAt   = computeNoOpponentKickPoint( ballPos );
		
		double     straightIntersect = Line.fromTwoPoints( ballPos, kickAt ).intersectCircle( oppPos, oppRad );
		boolean    canKickStraight   = Double.isNaN( straightIntersect ) || straightIntersect < 0;
		
		if( !oppPos.isNaN() && Math.abs( ballPos.getX() ) < 0.95 * pitch.getX() && !canKickStraight ) {
			double     goalSize = 0.55;
			double[]   regionTops    = { 0.0, 0.0 };
			double[]   regionBots    = { 0.0, 0.0 };
			double[]   regionSize    = { 0.0, 0.0 };
			Vector2[]  regionCentres = { null, null };
			Line[]     regionLines   = { null, null };
			double[]   regionInt     = { Double.NaN, Double.NaN };
				
			regionTops[ 0 ] =   goalSize / 2.0;
			regionBots[ 1 ] = - goalSize / 2.0;
			
			regionBots[ 0 ] =  Math.min( oppPos.getY() + oppRad, regionTops[ 0 ] );
			regionTops[ 1 ] =  Math.max( oppPos.getY() - oppRad, regionBots[ 1 ] );
			
			for( int i = 0; i < 2 ; ++ i ) {
				regionCentres[ i ] = new Vector2( - ourSide * pitch.getX(), ( regionTops[ i ] + regionBots[ i ] ) / 2.0 );
				regionLines[ i ]   = Line.fromTwoPoints( ballPos, regionCentres[ i ] );				
				regionInt[ i ]     = regionLines[ i ].intersectCircle( oppPos, oppRad );
				
				if( regionInt[ i ] < 0.0 )
					regionInt[ i ] = Double.NaN;
				regionSize[ i ]    = regionTops[ i ] - regionBots[ i ];
			}
			
			if( Double.isNaN( regionInt[ 0 ] ) && Double.isNaN( regionInt[ 1 ] ) ) {
				if( regionSize[ 0 ] > regionSize[ 1 ] )
					kickAt = regionCentres[ 0 ];
				else
					kickAt = regionCentres[ 1 ];
			} else if( !Double.isNaN( regionInt[ 0 ] ) && Double.isNaN( regionInt[ 1 ] ) ) {
				kickAt = regionCentres[ 1 ];
			} else if( !Double.isNaN( regionInt[ 1 ] ) && Double.isNaN( regionInt[ 0 ] ) ) {
				kickAt = regionCentres[ 0 ];
			} else {
				kickAt = new Vector2( - ourSide * pitch.getX() * 0.5, pitch.getY() );
			}
		}
		
		return kickAt;
	}
	
	/** Compute whether kicking would result in a scored goal.
	 * 
	 * @return Whether the kick would be successful or not. 
	 */
	public boolean shouldKick() {	
		return shouldKick( new Vector2( - ourSide * world.getPitchTopRight().getX(), 0.0 ), getGoalSize() );
	}
	
	/** Compute whether kicking would result in the ball hitting the given kick point within a given tolerance.
	 * Ignores the position of the other opponent.
	 * 
	 * @param kickToPoint The point where kicking is desired.
	 * @param tolerance   The maximum distance allowed from kickToPoint. 
	 * 
	 * @return Whether the kick would be successful or not.
	 */
	public boolean shouldKick( Vector2 kickToPoint, double tolerance ) {
		RobotState robot        = ctrl.getRobotState();
		Vector2    robPos       = ctrl.getCurrentNavPoint();
		double     robRot       = robot.getRotation();
		BallState  ball         = world.getBallState();
		Vector2    ballPos      = ball.getPosition();
		Vector2    toBall       = ballPos.minus( ctrl.getCurrentNavPoint() );
		boolean    closeToBall  = toBall.computeNorm() < ctrl.positionTolerance*1.5;
		boolean    facingBall   = Math.abs( MathUtils.angleDiff( toBall.computeAngle(), robRot ) ) < Math.PI / 3.0;
		boolean    facingKickTo = Math.abs( MathUtils.angleDiff( kickToPoint.minus( robPos ).computeAngle(), robRot ) ) <= Math.PI * 0.45;
		
		return closeToBall && facingBall && facingKickTo;	
	}
	
	/** Clear all the actions in the current plan. An empty plan also causes the robot to stop. */
	protected void clearActions() {
		actions.clear();
		actionExecuted = false;
	}
	
	/** Estimate the position of the robot after the last action is executed. Equivalent to:
	 * 
	 * getPositionAfterAction( getActionsLeftCount() - 1 );
	 * 
	 * @return The estimated position of the robot after the last action's execution.
	 */
	public Vector2 getFinalPosition() {
		return getPositionAfterAction( getActionCount() - 1 );
	}
	
	/** Estimate the rotation of the robot after the last action is executed. Equivalent to:
	 * 
	 * getRotationAfterAction( getActionsLeftCount() - 1 );
	 * 
	 * @return The estimated rotation of the robot after the last action's execution.
	 */
	public double  getFinalRotation() {
		return getRotationAfterAction( getActionCount() - 1 );
	}
	
	/** Estimate the position of the robot after executing the action at a given index. If the index is negative, the 
	 * current position is returned. If the index is greater than the number of actions left, then the result is the
	 * same as the position after the last action.
	 * 
	 * @param actionIndex The index of the action after whose execution the position is required. Can be negative or
	 * greater than the maximum index.
	 * 
	 * @return The estimated position of the robot after the action's execution.
	 */
	public Vector2 getPositionAfterAction( int actionIndex ) {
		Vector2 currentPos;
		
		if( actionIndex < -1 )
			actionIndex = -1;
		else if( actionIndex > getActionCount() - 1 )
			actionIndex = getActionCount() - 1;
		
		if( actionIndex < 0 ) {
			currentPos = ctrl.getCurrentNavPoint();
		} else {
			Action lastMoveAction = actions.get( actionIndex );
			
			while( actionIndex >= 0 ) {
				lastMoveAction = actions.get( actionIndex );
				
				if( lastMoveAction instanceof MovementAction )
					break;
				
				-- actionIndex;
			}
			
			if( actionIndex < 0 )
				currentPos = ctrl.getCurrentNavPoint();
			else
				currentPos = ( (MovementAction) lastMoveAction ).getPosition( this );
		}
		
		return currentPos;
	}
	
	/** Estimate the rotation of the robot after executing the action at a given index. If the index is negative, the
	 * current rotation is returned. If the index is greater than the number of actions left, then the result is the
	 * same as the rotation after the last action.
	 * 
	 * @param actionIndex The index of the action after whose execution the rotation is required. Can be negative or
	 * greater than the maximum index.
	 * 
	 * @return The estimated rotation of the robot after the action's execution.
	 */
	public double getRotationAfterAction( int actionIndex ) {
		final double SAME_POSITION_TOLERANCE = 1e-4;
		
		double currentRot;
		
		if( actionIndex < -1 )
			actionIndex = -1;
		else if( actionIndex > getActionCount() - 1 )
			actionIndex = getActionCount() - 1;
		
		if( actionIndex < 0 ) {
			currentRot = ctrl.getCurrentRotation();
		} else {
			Action lastRotateMoveAction = actions.get( actionIndex );
			
			while( actionIndex >= 0 ) {
				lastRotateMoveAction = actions.get( actionIndex );
				
				if( lastRotateMoveAction instanceof MovementAction )
					break;
				
				if( lastRotateMoveAction instanceof RotationAction )
					break;
				
				-- actionIndex;
			}
			
			if( actionIndex < 0 ) {
				currentRot = ctrl.getCurrentRotation();
			} else if( lastRotateMoveAction instanceof RotationAction ) {
				currentRot = ((RotationAction) lastRotateMoveAction).getRotation( this );
			} else {
				Vector2 currentPos  = ( (MovementAction) lastRotateMoveAction ).getPosition( this );
				assert lastRotateMoveAction instanceof MovementAction;
				Vector2 previousPos;
				
				-- actionIndex;
				while( actionIndex >= 0 ) {
					lastRotateMoveAction = actions.get( actionIndex );
					
					if( lastRotateMoveAction instanceof MovementAction ) {
						MovementAction a = (MovementAction) lastRotateMoveAction;
						if( !a.getPosition( this ).approxEquals( currentPos, SAME_POSITION_TOLERANCE ) )
							break;
					}
					
					-- actionIndex;
				}
				
				if( actionIndex < 0 )
					previousPos = ctrl.getCurrentPosition();
				else
					previousPos = ( ( MovementAction ) lastRotateMoveAction ).getPosition( this );
				
				currentRot = currentPos.minus( previousPos ).computeAngle();
			}
		}
		
		return currentRot;
	}
	
	public boolean isInPitch( Vector2 p ) {
		Vector2 s = world.getPitchTopRight();
		double o = ctrl.getNavPointOffset()*0.5;
		return p.getX() >= -s.getX() + o  && p.getX() <= s.getX() - o &&
		       p.getY() >= -s.getY() + o && p.getY() <= s.getY() - o;
	}	
	/*
	public void driveAroundObstacles( DriveAction driveAction, Vector2[] obstaclePositions, double obstacleRadii[] ) {
		Vector2 finalPos   = driveAction.getPosition();
		boolean intersects = true;
		int     nObstacles = obstaclePositions.length;
		Vector2 curPos;
		double  curRot;
		int     iters = 0;
		int     detourIndex = -1;
		
		{
			PositionAndRotation pnr = getLastPositionAndRotation();
			curPos = pnr.position;
			curRot = pnr.rotation;
		}
		
		while( intersects && (++iters) < 100 ) {
			intersects = false;
			
			double[] obstacleDists = {
				obstaclePositions[ 0 ].minus(curPos).computeNorm() - obstacleRadii[ 0 ],
				obstaclePositions[ 1 ].minus(curPos).computeNorm() - obstacleRadii[ 1 ]
			};
			
			if( obstacleDists[ 0 ] > obstacleDists[ 1 ] ) {
				double auxd;
				Vector2 auxv;
				
				auxd = obstacleDists[ 0 ]; obstacleDists[ 0 ] = obstacleDists[ 1 ]; obstacleDists[ 1 ] = auxd;
				auxd = obstacleRadii[ 0 ]; obstacleRadii[ 0 ] = obstacleRadii[ 1 ]; obstacleRadii[ 1 ] = auxd;
				
				auxv = obstaclePositions[ 0 ];
				obstaclePositions[ 0 ] = obstaclePositions[ 1 ];
				obstaclePositions[ 1 ] = auxv;
			}
			
			for( int i = 0 ; i < nObstacles ; ++ i ) {
				Vector2 obsPos = obstaclePositions[ i ];
				double  obsRad = obstacleRadii[ i ];
			
				Line lineToFinal = Line.fromTwoPoints( curPos, finalPos );
				Line normal      = lineToFinal.getNormal( obsPos );
				
				double  inDist  = lineToFinal.intersect( normal );
				double  colDist = obsRad + ctrl.getGoalPointOffset();
				
				boolean test1 = curPos.minus( finalPos ).computeSquaredNorm()          >= inDist*inDist;
				boolean test2 = curPos.minus( finalPos ).dot( curPos.minus( obsPos ) ) >= 0.0;
				boolean test3 = lineToFinal.distanceTo( obsPos )                       <  colDist;
				 
				boolean test4 = finalPos.minus( obsPos ).computeNorm()                 >= colDist;
				
				if( !test4 ) {
					obsRad = finalPos.minus( obsPos ).computeNorm();
					colDist = obsRad + ctrl.getGoalPointOffset();
				}
				
				if( test1 && test2 && test3 ) {
					double detourDist  = obsRad + ctrl.getGoalPointOffset() + 0.06;
					double detourAngle = Math.PI / 6.0;
					
					intersects = true;
					
					Vector2 detour1 = obsPos.plus( Vector2.fromAngle(curPos.minus( obsPos ).computeAngle() + detourAngle, detourDist) );
					Vector2 detour2 = obsPos.plus( Vector2.fromAngle(curPos.minus( obsPos ).computeAngle() - detourAngle, detourDist) );
					
					double  detourAngles[] = { Double.NaN, Double.NaN };
					Vector2 detour[] = { detour1, detour2 };
					int     nGoodPoints = 0;
					
					for( int j = 0 ; j < 2 ; ++ j ) {
						if( isInPitch( detour[ j ] ) ) {
							detourAngles[ j ] = MathUtils.angleDiff( detour[i].minus( curPos ).computeAngle(), curRot );
							nGoodPoints ++;
						}
					}
					
					if( detourIndex == -1 ) {
						if( nGoodPoints == 2 ) {
							if( detour[ 0 ].minus( finalPos ).computeSquaredNorm() < detour[1].minus( finalPos ).computeSquaredNorm() )
								detourIndex = 0;
							else
								detourIndex = 1;
						} else if( nGoodPoints == 1 ) {
							if( Double.isNaN( detourAngles[ 0 ] ) ) {
								detourIndex = 1;
							} else {
								detourIndex = 0;
							}
						} else
							intersects = false;
					} else if( Double.isNaN( detourAngles[ detourIndex ] ) ) {
						detourIndex = ( detourIndex + 1 ) % 2;
						clearActions();
						PositionAndRotation pnr = getLastPositionAndRotation();
						curPos = pnr.position;
						curRot = pnr.rotation;
					}
					
					if( detourIndex != -1 ) {
						driveThrough( detour[ detourIndex ], 1.0 );
						PositionAndRotation pnr = getLastPositionAndRotation();
						curPos = pnr.position;
						curRot = pnr.rotation;
					}
				}
			}
		}
		
		if( intersects )
			System.err.println("Strategy: enqueueGoal(): Infinite loop in obstacle avoidance.");
		
		pushAction( driveAction );
	}*/
}
