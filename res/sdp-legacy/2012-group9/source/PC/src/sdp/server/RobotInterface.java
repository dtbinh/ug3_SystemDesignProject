package sdp.server;

import sdp.server.math.Vector2;

public class RobotInterface {
	private Vector2 wheelSpeeds = Vector2.ZERO;
	private boolean kickNext    = false;
	
	public RobotInterface() {
		
	}
	
	/** Set the new wheel speeds. Has no effect for non-controllable robots.
	 * 
	 * @param newWheelSpeeds The new wheel speeds in rad/s.
	 */
	public void setWheelSpeeds( Vector2 newWheelSpeeds ) {
		this.wheelSpeeds = newWheelSpeeds;
	}
	
	/** Get the new wheel speeds.
	 * 
	 * @return The wheel speeds in rad/s that have been set with setNewWheelSpeeds.
	 */
	public Vector2 getWheelSpeeds() {
		return wheelSpeeds;
	}
	
	/** Perform kick in next timestep. */
	public void kick() {
		kickNext = true;
	}
	
	/** Check whether the robot should kick (i.e. whether kick() was called or not).
	 * 
	 * @return Whether the robot should kick.
	 */
	public boolean shouldKick() {
		return kickNext;
	}
	
	/** Called every timestep. Only resets the kicking flag for now - but might do more in the future. */
	public void update() {
		kickNext = false;
	}
}
