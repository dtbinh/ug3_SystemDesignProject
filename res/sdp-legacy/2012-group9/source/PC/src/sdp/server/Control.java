package sdp.server;

import java.lang.Math;

import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;

/** This class provides an abstraction layer over the raw wheel speeds, by allowing a Strategy to simply specify a goal
 *  position and rotation, a direction of movement (forwards or backwards) and simple kick() method to perform kicks.
 *  It also allows the goal position to be overriden by calling setStationary() which makes the robot stop pursuing its
 *  goal temporarily.
 *  
 *  @author Cristian Cobzarenco
 */
public class Control extends PropertyProvider {
	public static final long F_AVOID_ROBOT  =   1;
	public static final long F_AVOID_BALL   =   2;
	public static final long F_AVOID_WALLS  =   4;
	public static final long F_REVERSE      =   8;
	public static final long F_STOP_AT_GOAL =  16;
	public static final long F_MATCH_ANGLE  =  32;
	public static final long F_KICK         =  64;
	public static final long F_STATIONARY   = 128;
	public static final long F_DEFAULT      = F_AVOID_BALL | F_AVOID_WALLS | F_AVOID_ROBOT | F_STOP_AT_GOAL | F_STATIONARY;
		
	private RobotState     robState;
	private RobotInterface robIntf;
	private World          world;
	private long           flags                = F_DEFAULT;
	private GoalState      goalState            = GoalState.DONE;
	private Vector2 	   destination          = Vector2.ZERO;
	private double  	   targetAngle          = 0.0;
	private double         previousRotationDiff = Double.NaN;
	private double         rotationIntegral     = 0.0;
	private double         rotationError        = 0.0;
	
	public double          positionTolerance    = 0.05;
	public double          rotationTolerance    = 32.0;
	public double          maxWheelSpeed        = 12.0;
	public double          currentSpeed         = 1.0;
	public double          movementSlowDown     = 1.1;
	public double          movementSlowStart    = 0.2;
	public double          arcExponent          = 1.0;
	public double          rotationP            = 0.400;
	public double          rotationI            = 0.020;
	public double          rotationD            = 0.060;
	public double          aimingSlowDown       = 0.9;
	public double          axleLength           = 0.015;
	public double          wheelRadius          = 0.2448;
	public double          turnRadius           = 0.21;
	public double          pivotOffset          = 0.05;
	public double          navPointOffset       = 0.124;
	public double          perspectiveCorr      = 0.042;	
	
	private enum GoalState {
		NOT_THERE,
		AT_POSITION,
		DONE
	}
	
	/** Construct giving a reference to the world and the team the robot is playing on. getRobotInterface() and
	 * getRobotState() are used to control the robot.
	 * 
	 * @param world The world object from PcServer.
	 * @param team  The team the robot plays on.
	 */
	public Control( World world, RobotState.Team team ) {
		this.world = world;
		robIntf    = world.getRobotInterface( team );
		robState   = world.getRobotState( team );
		populateProperties();
	}
	 
	/** Test whether all flags in a combination are set.
	 * 
	 * @param flags The combination of flags to test for.
	 * @return Whether all of the given flags are set.
	 */
	public boolean areFlagsSet( long flags ) {
		return (this.flags & flags) == flags; 
	}
	
	/** Test whether any flag in a combination is set.
	 * 
	 * @param flags The combination of flags to test for.
	 * @return Whether any of the given flags is set.
	 */
	public boolean isAnyFlagSet( long flags ) {
		return (this.flags & flags) != 0; 
	}
	
	/** Toggle (sets to true if false and vice-versa) the value of a
	 * combination of flags.
	 * 
	 *  @param flags The combination of flags to toggle
	 */
	public void toggleFlags( long flags ) {
		this.flags = this.flags ^ flags;
	}
	
	/** Set (to true) a combination of flags.
	 * 
	 * @param flags The combination of flags to set to true.
	 */
	public void setFlags( long flags ) {
		this.flags |= flags;
	}
	
	/** Set a combination of flags, to a given value (true or false).
	 * 
	 * @param set The value to set the combination of flags.
	 * @param flags The combination of flags to change.
	 */
	public void setFlags( boolean set, long flags ) {
		if( set )
			setFlags( flags );
		else
			unsetFlags( flags );
	}
	
	/** Unset (set to false) a given combination of flags.
	 * 
	 * @param flags Flag combination to unset.
	 */
	public void unsetFlags( long flags ){
		this.flags &= ( ~ flags );
	}
	
	/** Reset all of the Control flags to a given combination.
	 * 
	 * @param flags The new flag values.
	 */
	public void resetFlags( long flags ) {
		this.flags = flags;
	}
		
	/** Change the team the robot is playing on.
	 * 
	 * @param newTeam The new team the robot should be playing on.
	 */
	public void setTeam( RobotState.Team newTeam ) {
		if( newTeam != robState.getTeam() ) {
			robState = world.getRobotState( newTeam );
			robIntf  = world.getRobotInterface( newTeam );
		}
	}
		
	/** Get the robot state used by this Control object.
	 * 
	 * @return The used robot state.
	 */
	public RobotState getRobotState() {
		return robState;
	}
	
	/** Get the robot interface used by this Control object.
	 * 
	 * @return The used robot interface.
	 */
	public RobotInterface getRobotInterface() {
		return robIntf;
	}
	
	/** Get the current error in the robot's orientation, the difference between the desired angle and
	 * the current angle.
	 *  
	 * @return The current rotation error.
	 */
	public double getRotationError() {
		return rotationError;
	}
	
	/** Get the current rotation of the robot (the angle along which it's facing, 0 - right, PI/2 - up).
	 * 
	 * @return The current rotation of the robot.
	 */
	public double getCurrentRotation() {
		if( isAnyFlagSet( F_REVERSE ) )
			return MathUtils.capAngle( Math.PI + getRobotState().getRotation() );
		else
			return getRobotState().getRotation();
	}
	
	/** Get the current position of the robot's centre.
	 * 
	 * @return The current position of the robot's centre.
	 */
	public Vector2 getCurrentPosition() {
		return getRobotState().getPosition();
	}
	
	/** Get the position of the robot's 'nav-point' - this the point which is moved towards the destination.
	 *  
	 * @return The position of the nav-point.
	 */
	public Vector2 getCurrentNavPoint() {
		double  offset = navPointOffset - Math.max( Math.abs( destination.getY() ), Math.abs( destination.getX() ) ) * perspectiveCorr;
		return robState.getPosition().plus(
			Vector2.fromAngle(
					robState.getRotation(),
				offset * ( isAnyFlagSet( F_REVERSE ) ? -1.0 : 1.0)
			)
		);
	}
	
	/** Get the position of the robot's pivot - the point around which it turns.
	 * 
	 * @return The current position of the robot's pivot.
	 */
	public Vector2 getCurrentPivot() {
		double  r = robState.getRotation();
		Vector2 p = robState.getPosition();
		return p.plus( Vector2.fromAngle( r, pivotOffset ) );
	}
	
	/** Get the current destination of the robot.
	 * 
	 * @return The position the robot is attempting to arrive to.
	 */
	public Vector2 getDestination() {
		return destination;
	}
		
	/** Get the axle length (the distance between the two wheels) used.
	 * 
	 *  @return The axle length in metres.
	 */
	public double getAxleLength() {
		return axleLength;
	}
	
	/** Get the radius of the drive wheels used.
	 * 
	 * @return The radius of the drive wheels in metres.
	 */
	public double getWheelRadius() {
		return wheelRadius;
	}
	
	/** Get the turn radius of the robot. This should be set such that if the robot performs a 360 turn it
	 * always stay within the circle centred in the pivot (see getPivotOffset) with the radius equal to the
	 * turn radius.
	 * 
	 * @return The turn radius of the robot in metres.
	 */
	public double getTurnRadius() {
		return turnRadius;
	}
	
	/** Get the pivot offset of the robot: the distance between the robot's centre of mass and the pivot
	 * around which it rotates (the midpoint of the drive wheels). The pivot is assumed to lie on the
	 * direction line of the robot, so only the distance from the centre is required.
	 * 
	 * @return The pivot offset of the robot in metres.
	 */
	public double getPivotOffset() {
		return pivotOffset;
	}
	
	/** The nav-point of the robot is the point relative to the robot which the Control module tries
	 * to get as close as possible to the goal position. The nav-point offset is the distance between
	 * the nav-point and the robot's centre. If the nav-point offset is zero, then Control tries to
	 * get the centre of the robot on top of the goal position.
	 * 
	 * @return The desired nav-point offset of the robot in metres.
	 */
	public double getNavPointOffset() {
		return navPointOffset;
	}
	
	/** Get the world this Control object acts in.
	 * 
	 * @return The world object used by Control.
	 */
	public World getWorld() {
		return world;
	}
	
	/** Sets the axle length to a new value. See getAxleLength().
	 * 
	 * @param newAxleLength The new value in metres.
	 */
	public void setAxleLength( double newAxleLength ) {
		axleLength = newAxleLength;
	}
	
	/** Sets the drive wheel's radius to a new value.
	 * 
	 * @param newWheelRadius The new value in metres.
	 */
	public void setWheelRadius( double newWheelRadius ) {
		wheelRadius = newWheelRadius;
	}
	
	/** Sets a new turn radius. See getTurnRadius().
	 * 
	 * @param newTurnRadius The new turn radius in metres.
	 */
	public void setTurnRadius( double newTurnRadius ) {
		turnRadius = newTurnRadius;
	}
	
	/** Set a new pivot offset. See getPivotOffset().
	 * 
	 * @param newPivotOffset The new pivot offset in metres.
	 */
	public void setPivotOffset( double newPivotOffset ) {
		pivotOffset = newPivotOffset;
	}
	
	/** Set a new nav-point offset. See getNavPointOffset().
	 * 
	 * @param newNavPointOffset The new nav-point offset in metres.
	 */
	public void setNavPointOffset( double newNavPointOffset ) {
		navPointOffset = newNavPointOffset;
	}
	
	/** Set the destination of the robot. The robot will turn and move appropriately to get the nav-point
	 * as close to the destination as possible.
	 * 
	 * @param newDestination The new destination of the robot.
	 */
	public void setDestination( Vector2 newDestination ) {
		if( newDestination.isNaN() )
			System.out.println("Control: New goal position is NaN, ignoring.");
		else {
			destination = newDestination;
			
			Vector2 limits = world.getPitchTopRight().times( 0.98 );
			if( destination.getX() > limits.getX() )
				destination = new Vector2( limits.getX(), destination.getY() );
			
			if( destination.getX() < - limits.getX() )
				destination = new Vector2( - limits.getX(), destination.getY() );
			
			if( destination.getY() > limits.getY() )
				destination = new Vector2( destination.getX(), limits.getY() );
			
			if( destination.getY() < - limits.getY() )
				destination = new Vector2( destination.getX(), - limits.getY() );
			
			targetAngle          = destination.minus( robState.getPosition() ).computeAngle();
			previousRotationDiff = Double.NaN;
			rotationIntegral     = 0.0;
			unsetFlags( F_MATCH_ANGLE );
			
			if( isAtPosition() )
				goalState = GoalState.DONE;
			else
				goalState = GoalState.NOT_THERE;
		}
	}
	
	/** Set the desired robot orientation. After getting to the destination, it will rotate to be
	 * facing this way.
	 * 
	 * @param newTargetAngle The desired angle. Should be in [0, 2pi) range.
	 */
	public void setTargetAngle( double newTargetAngle ) {
		if( Double.isNaN( newTargetAngle ) )
			System.out.println("Control: New goal rotation is NaN, ignoring.");
		else {
			targetAngle          = MathUtils.capAngle( newTargetAngle );
			previousRotationDiff = Double.NaN;
			rotationIntegral     = 0.0;
			setFlags( F_MATCH_ANGLE );
						
			if( hasReachedGoal() || isAtPosition() ) {
				if( isAtRotation() )
					goalState = GoalState.DONE;
				else
					goalState = GoalState.AT_POSITION;
			}
		}
	}
	

	public void setMaximumSpeed ( double maxSpeed ) {
		assert maxSpeed >= 0.0 && maxSpeed <= 1.0;
		this.maxWheelSpeed = maxSpeed;
	}
	
	public void setCurrentSpeed( double speed ) {
		assert speed >= 0.0 && speed <= 1.0;
		this.currentSpeed = speed;
	}
	
	/** Check if a kicking action was already ordered this time step.
	 * 
	 * @return true if the robot is already kicking, false otherwise.
	 */
	public boolean isKicking() {
		return isAnyFlagSet( F_KICK );
	}
	
	/** Tells the robot to kick in the next time step. This works even in stationary mode. */
	public void kick() {
		setFlags( F_KICK );
	}
	
	/** Check whether the robot is at its goal position and rotation. If the robot is
	 * stationary this always returns true.
	 * 
	 * @return true if the goal has been achieved, false otherwise.
	 */
	public boolean hasReachedGoal() {
		return goalState == GoalState.DONE || isAnyFlagSet( F_STATIONARY );
	}
	
	/** Check if the robot is currently at position. */
	public boolean isAtPosition() {
		return getCurrentNavPoint().approxEquals( destination, positionTolerance );
	}
	
	/** Check if the robot is at rotation. */
	public boolean isAtRotation() {
		if( !isAnyFlagSet( F_MATCH_ANGLE ) )
			return true;
		
		double rot = robState.getRotation();
		if( isAnyFlagSet( F_REVERSE ) );
			rot = MathUtils.capAngle( rot + Math.PI );
		return Math.abs(MathUtils.angleDiff( targetAngle, rot)) <= 2.0 * Math.PI / rotationTolerance;
	}
	
	/** This method needs to be called every time step. It feeds the required wheel speeds to the
	 * World object (provided at construction) to get to the goal (unless stationary, in which case
	 * it only takes care of the kicking).
	 */
	public void update( double dt ) {
		Vector2 position        = getCurrentNavPoint();
		double  rotation        = getCurrentRotation();
		Vector2 wheelSpeeds     = robIntf.getWheelSpeeds();
		Vector2 pitch           = world.getPitchTopRight();
		double  forwardSpeed    = 1.0;
		double  rotationSpeed   = 1.0;
		boolean limitRotation   = false;
		double  allowedRotation = Double.NaN;
		boolean avoidWalls      = isAnyFlagSet( F_AVOID_WALLS );
		boolean reverse         = isAnyFlagSet( F_REVERSE );
		
		if( isAnyFlagSet( F_KICK ) ) {
			robIntf.kick();
			unsetFlags( F_KICK );
		}
		
		if( Double.isNaN( rotation ) || position.isNaN() ||
			Double.isNaN( targetAngle ) || isAnyFlagSet( F_STATIONARY ) ||
			hasReachedGoal()
		) {
			robIntf.setWheelSpeeds( Vector2.ZERO );
			return;	
		}
		
		rotationError = MathUtils.angleDiff( targetAngle, rotation );
		
		// If we're not at our goal position yet, we need a forward speed to go in that direction.
		// We don't care that we may not be pointing the right direction yet, the rotation code
		// that follows will take care of that.
		if( goalState == GoalState.NOT_THERE && !isAtPosition() ) {
			// WALL AVOIDANCE
			if( avoidWalls ) {
				Vector2 pivot          = getCurrentPivot();
				double  collisionAngle = Double.NaN;
				double  yDiff          = 0.0;
				double  xDiff          = 0.0;
				
				if( position.getY() < 0.0 )
					yDiff = - pitch.getY() - pivot.getY() ;
				else
					yDiff = pitch.getY() - pivot.getY(); 
				
				if( Math.abs( yDiff ) <= turnRadius ) {
					collisionAngle = MathUtils.capAngle( Math.asin( yDiff / turnRadius ) );
					
					// If we're facing right
					if( Math.abs(rotation - Math.PI) > Math.PI/2  )
						collisionAngle = MathUtils.capAngle( Math.PI - collisionAngle );
				}
								
				if( position.getX() < 0.0 )
					xDiff = - pivot.getX() - pitch.getX();
				else
					xDiff = - pivot.getX() + pitch.getX(); 
				
				if( Math.abs( xDiff ) <= turnRadius ) {
					collisionAngle = MathUtils.capAngle( Math.acos( xDiff / turnRadius ) );
					
					// If we're facing up
					if( rotation < Math.PI  )
						collisionAngle = MathUtils.capAngle( - collisionAngle );
				}
				
				if( !Double.isNaN( collisionAngle ) ) {
					double halfLength = robState.getSize().getX() * .5;
					double halfWidth  = robState.getSize().getY() * .5;
					double angle1 = Math.PI - Math.atan( halfWidth / (halfLength + pivotOffset) );
					double angle2 = Math.PI * 2.0 - angle1;
					
					angle1 = MathUtils.capAngle( angle1 + rotation );
					angle2 = MathUtils.capAngle( angle2 + rotation );
					
					double angleDiff1 = MathUtils.angleDiff( collisionAngle, angle1 );
					double angleDiff2 = MathUtils.angleDiff( collisionAngle, angle2 );
					
					if( Math.abs( angleDiff1 ) < Math.abs( angleDiff2 ) )
						allowedRotation = angleDiff1;
					else
						allowedRotation = angleDiff2;
				}
				
				limitRotation = !Double.isNaN( allowedRotation ) &&
			            rotationError * allowedRotation >= 0.0 &&
			            Math.abs( rotationError ) > Math.abs( allowedRotation*0.95 );
			}
			// /WALL AVOIDANCE
			
			Vector2 toGoal      = destination.minus( position );
			double  toGoalAngle = MathUtils.capAngle( toGoal.computeAngle() );
			
			rotationError = MathUtils.angleDiff( toGoalAngle, rotation );
			
			double  distToGoal   = toGoal.computeNorm() ;
			double  cosineFactor = Math.cos( rotationError );
			
			if( isAnyFlagSet( F_STOP_AT_GOAL ) ) {
				forwardSpeed = Math.min( 1.0, Math.pow(distToGoal / movementSlowStart, movementSlowDown) );
			}
			
			if( !limitRotation )
				forwardSpeed *= Math.max( 0.3, cosineFactor );
			else {
				if( avoidWalls && wouldGoIntoWall( robState, cosineFactor < 0 ^ reverse ) ) {
					cosineFactor = Math.signum( cosineFactor ) * Math.abs( cosineFactor );
					cosineFactor = - cosineFactor;
				}
				
				forwardSpeed *= cosineFactor ;
			}
			
			if( limitRotation ) 
				rotationError = allowedRotation * 0.95;
						
		} else if( isAnyFlagSet( F_MATCH_ANGLE) && !hasReachedGoal() && !isAtRotation() ) {	
			rotationSpeed = aimingSlowDown;
			
			//if( isAnyFlagSet( F_STOP_AT_GOAL ) )
				forwardSpeed = 0.0;
			
			goalState = GoalState.AT_POSITION;
		} else {
			if( isAnyFlagSet( F_STOP_AT_GOAL ) )
				rotationSpeed = forwardSpeed = 0.0;
				
			goalState = GoalState.DONE;
		}
		
		if( reverse )
			forwardSpeed *= -1.0;
		
		double relativeHalfWheelSpeed = computeRelativeWheelSpeed( rotationError, dt ) * rotationSpeed / 2.0;
		wheelSpeeds = new Vector2( forwardSpeed - relativeHalfWheelSpeed, forwardSpeed + relativeHalfWheelSpeed );
		robIntf.setWheelSpeeds( capWheelSpeeds(wheelSpeeds).times( maxWheelSpeed * currentSpeed )   );
	}
	
	private boolean wouldGoIntoWall( RobotState robot, boolean reversing ) {
		Vector2 pos   = robot.getPosition();
		Vector2 sz    = robot.getSize();
		Vector2 pitch = world.getPitchTopRight();
		double  rot   = MathUtils.capAngle( robot.getRotation() + (reversing ? Math.PI : 0.0) );
		double  dist  = (sz.getX() * sz.getX() / 4.0 + sz.getY() * sz.getY() / 4.0) * 11.0;
		
		if( rot > Math.PI && pos.getY() + Vector2.fromAngle( rot, dist ).getY() < - pitch.getY() ) {
			return true;
		}
		
		if (rot < Math.PI && pos.getY() + Vector2.fromAngle(rot, dist).getY() > pitch.getY()) {
			return true;
		}
		
		if (((rot > Math.PI/2) || (rot < 3/2*Math.PI)) && pos.getX() + Vector2.fromAngle(rot, dist).getX() < - pitch.getX()) {
			return true;
		}
		
		if (((rot < Math.PI/2) || (rot > 3/2*Math.PI)) && pos.getX() + Vector2.fromAngle(rot, dist).getX() > pitch.getX()) {
			return true;
		}
		
		return false;
	}
	
	/** Perform clean up before exiting. Shouldn't do anything for Control, in here for completeness. */
	public void dispose() {
		
	}
	
	/** This method returns the necessary relative wheel speed to correct the rotation difference
	 * between the current orientation and the required one. It is used internally by update().
	 * 
	 * @param rotationDiff The rotation difference that needs to be corrected.
	 * @return The required relative wheel speed to correct the rotation difference.
	 */
	private double computeRelativeWheelSpeed( double rotationDiff, double dt ) {
		if( Double.isNaN( previousRotationDiff ) )
			previousRotationDiff = rotationDiff;
		
		double derivative, output;
		
		rotationIntegral    += rotationDiff * dt;
		derivative           = (rotationDiff - previousRotationDiff) / dt;
		output               = rotationP * rotationDiff + rotationI * rotationIntegral + rotationD * derivative;
		previousRotationDiff = rotationDiff;
		
		if( output < -1.0 )
			output = -1.0;
		else if( output > 1.0 )
			output = 1.0;
		
		
		
		return output;
	}
	
	/** Shift wheel speeds to bring both of them in the [-currentSpeed, +currentSpeed].
	 *  Assumes abs(motorSpeeds[1]-motorSpeeds[0]) <= 2*currentSpeed. This means they can be
	 *  shifted to fit.
	 */
	private Vector2 capWheelSpeeds( Vector2 wheelSpeeds ) {
		double correction = 0.0;
		
		assert Math.abs(wheelSpeeds.getX()) <= 1.0 || Math.abs(wheelSpeeds.getY()) <= 1.0 :Math.abs(wheelSpeeds.getX()) + " " + Math.abs(wheelSpeeds.getY()) ;
		assert Math.abs(wheelSpeeds.getY() - wheelSpeeds.getX()) <= 2.0 : wheelSpeeds.getY() - wheelSpeeds.getX();
		
		if( wheelSpeeds.getX() > 1.0 )
			correction = wheelSpeeds.getX() - 1.0;
		else if( wheelSpeeds.getX() < - 1.0 )
			correction = wheelSpeeds.getX() + 1.0;
		else if( wheelSpeeds.getY() > 1.0 )
			correction = wheelSpeeds.getY() - 1.0;
		else if( wheelSpeeds.getY() < - 1.0 )
			correction = wheelSpeeds.getY() + 1.0;
		
		return wheelSpeeds.minus( new Vector2( correction, correction ) );
	}
	
	
	private void populateProperties() {
		new ReflectProperty( "maxWheelSpeed",     0.0,   16.0,  0.1   );
		new ReflectProperty( "positionTolerance", 0.01,   0.1,  0.001 );
		new ReflectProperty( "rotationTolerance", 1.0,   64.0,  0.5   );
		new ReflectProperty( "movementSlowStart", 0.01,   0.5,  0.001 );
		new ReflectProperty( "movementSlowDown",  0.1,    8.0,  0.01  );
		new ReflectProperty( "rotationP",         0.0,   64.0,  0.01  );
		new ReflectProperty( "rotationI",         0.0,   64.0,  0.01  );
		new ReflectProperty( "rotationD",         0.0,   64.0,  0.01  );
		new ReflectProperty( "aimingSlowDown",    0.05,   1.0,  0.001 );
		new ReflectProperty( "turnRadius",        0.17,   0.23, 0.001 );
		new ReflectProperty( "pivotOffset",       0.03,   0.2,  0.001 );
		new ReflectProperty( "navPointOffset",    0.03,   0.2,  0.001 );
		new ReflectProperty( "perspectiveCorr",   0.001,  0.1,  0.001 );
		
		new Property( "pitchRight" , 0.0, 4.0, 0.001 ) {
			@Override public void setValue( double newValue ) {
				Vector2 pitch = world.getPitchTopRight();
				world.setPitchTopRight( new Vector2( newValue, pitch.getY() ) );
			}

			@Override public double getValue() {
				return world.getPitchTopRight().getX();
			}
		};
		new Property( "pitchTop" , 0.0, 4.0, 0.001 ) {
			@Override public void setValue( double newValue ) {
				Vector2 pitch = world.getPitchTopRight();
				world.setPitchTopRight( new Vector2( pitch.getX(), newValue ) );
			}

			@Override public double getValue() {
				return world.getPitchTopRight().getY();
			}
		};
	}
}
