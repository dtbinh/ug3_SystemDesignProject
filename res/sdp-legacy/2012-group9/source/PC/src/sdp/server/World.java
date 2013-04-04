package sdp.server;

import sdp.server.RobotState.Team;
import sdp.server.math.Vector2;

/** Abstract class that keeps track of the state of the world. This includes the state of the two robots and the ball, as well
 * as the sensor values of the player's robot. It also stores the desired wheel speeds (that are set by Control), which get
 * sent to the robot every time step.
 * The class is abstract because it doesn't provide the implementation for the world update (method update()). There are two
 * classes which inherit this one: SimWorld and NxtWorld:
 * - In the case of the SimWorld, the update simulates one time step using the wheel speeds provided and (possibly) generates
 *   an image that is then passed to Vision to estimate the new positions and orientations of the robots.
 * - The NxtWorld corresponds to the real, physical world. The desired wheel speeds are communicated to the robot, using the
 *   Communication class. The latter is also used to fetch an image from the camera which is processed by a Vision object to
 *   estimate the new positions and orientations which will be available to the Strategy in the next time step.
 */
public abstract class World {
	protected RobotState[]     robotStates;
	protected BallState        ballState;
	private   Vector2          pitchTopRight;
	private   long             lastTime = 0;
	private   double           timeStep = 0.03;
	private   ScreenProjection screenProj;
	private long 			lastUpdateTime = -1;
	
	public static class FailedInitException extends Exception {
		private static final long serialVersionUID = 1L;

		public FailedInitException( String message ) {
			super( message );
		}
		
		public FailedInitException( Throwable cause ) {
			super( cause );
		}
	}
	
	/** Class to wrap all the parameters needed to specify the initial state of the world. */
	public static class Settings {
		public Vector2      pitchTopRight   = null;
		public RobotState[] robotStates     = null;
		public BallState    ballState       = null;
		
		public Settings() {
			
		}
		
		public Settings( BallState ball, RobotState[] robotStates, Vector2 pitchTopRight ) {
			this.ballState     = ball;
			this.robotStates   = robotStates;
			this.pitchTopRight = pitchTopRight;
		}
		
		
		public boolean isValid() {
			return pitchTopRight            != null &&
			       ballState                != null &&
			       robotStates              != null &&
			       robotStates[ Team.YELLOW.ordinal() ].getTeam() == Team.YELLOW && 
			       robotStates[ Team.BLUE.ordinal() ].getTeam()   == Team.BLUE;
		}
	}
	
	/** Construct using given settings. */
	public World( Settings settings ) {
		assert settings != null;
		assert settings.isValid();
		
		this.pitchTopRight   = settings.pitchTopRight;
		this.robotStates     = settings.robotStates;
		this.ballState       = settings.ballState;
		
		screenProj = new ScreenProjection( Vector2.NaN );
		lastTime = System.currentTimeMillis();
	}
	
	public void setLastUpdateTime(long newTime){
		this.lastUpdateTime = newTime;
	}
	
	public long getLastUpdateTime(){
		return lastUpdateTime;
	}
	
	/** Set the pitch size. */
	public void setPitchTopRight( Vector2 newPitchTopRight ) {
		pitchTopRight = newPitchTopRight;
	}
	
	/** Initialise the world. Separated from constructor, because it may throw an exception. */
	public void initialise() throws FailedInitException {
		
	}
	
	/** Get the screen projection. This can be used to convert between world and screen coordinates. */
	public ScreenProjection getScreenProjection() {
		return screenProj;
	}
	
	/** Get the state of a robot of a given team. */
	public RobotState getRobotState( Team team ) {
		return robotStates[ team.ordinal() ];
	}
	
	/** Return the interface for communicating with a particular robot. This is the same
	 * for both robots in the case of NxtWorld.
	 * 
	 * @param team The team of the robot whose interface is required.
	 * @return The RobotInterface for the robot of the given team.
	 */
	public abstract RobotInterface getRobotInterface( Team team );
	
	/** Get both robot states. */
	public RobotState[] getRobotStates() {
		return robotStates;
	}
	
	/** Get the ball's state. */
	public BallState getBallState() {
		return ballState;
	}
	
	/** Get the position of the pitch's top right corner. */
	public Vector2 getPitchTopRight() {
		return pitchTopRight;
	}
	
	/** Get the time elapsed between the last two updates. */
	public double getTimeStep() {
		return timeStep;
	}
	
	/** Perform one time step. Computes the timestep and calls update(). */
	public void step() {
		long currentTime = System.currentTimeMillis();
		timeStep = (double)(currentTime - lastTime) / 1000.0;
		lastTime = currentTime;
		
		//this.lastUpdateTime = currentTime;
		
		update();
	}
	
	/** Perform clean up before exiting. This should disconnect any hardware, unlock resources etc. */
	public void dispose() {
		
	}
	
	/** This method gets called every timestep by the step(). */
	protected abstract void update();
}
