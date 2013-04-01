package sdp.server;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

import sdp.server.math.Vector2;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_video.*;

/** This class stores and updates the ball's position and velocity based on measurements made every time step.
 * Internally it uses a Kalman filter for updating the estimated position and velocity. */
public class BallState {
	private Vector2  position;
	private Vector2  velocity;
	private double   radius;
	private CvKalman kalman;
	private CvMat    measured, prediction;
	private CvMat    transitionMatrix;
	private boolean  useKalman  = true;
	private int      undetectedFrameCount = 0;
	
	/** Construct with given size. The position will be set to NaN awaiting first update().
	 * 
	 * @param radius The radius of the ball in metres.
	 */
	public BallState( double radius ) {
		this( Vector2.NaN, radius );
	}
	
	/** Construct with given starting position and size.
	 * 
	 * @param position The initial position of the ball in metres, relative to the centre of the pitch.
	 * @param radius The radius of the ball in metres.
	 */
	public BallState( Vector2 position, double radius ) {
		assert position != null;
		kalman     = cvCreateKalman( 4, 2, 0 );
		measured   = CvMat.create( 2, 1, CV_32FC1 );
		prediction = CvMat.create( 4, 1, CV_32FC1 );
		
		setProcessNoise( 1e-5 );
		setMeasurementNoise( 1e-4 );
		setPostError( 1e-1 );
		
		setupTransitionMatrix();
		
		this.radius = radius;
		reset( position );
	}
	
	/** Set the process noise covariance used by the Kalman filter.
	 * 
	 * @param newProcessNoise The new value for the process noise.
	 */
	public void setProcessNoise( double newProcessNoise ) {
		cvSetIdentity( kalman.process_noise_cov(), cvScalarAll( newProcessNoise ) );
	}
	
	/** Set the measurement noise used by the Kalman filter.
	 * 
	 * @param newMeasurementNoise The new value for the measurement noise.
	 */
	public void setMeasurementNoise( double newMeasurementNoise ) {
		cvSetIdentity( kalman.measurement_noise_cov(), cvScalarAll( newMeasurementNoise ) );
	}
	
	
	/** Set the post error covariance used by the Kalman filter.
	 * 
	 * @param newPostError The new post error covariance value.
	 */
	public void setPostError( double newPostError ) {
		cvSetIdentity( kalman.error_cov_post(), cvScalarAll( newPostError ) );
	}
	
	/** Returns true if the ball cannot be detected. The position is still predicted,
	 * if true.
	 * 
	 * @return True if the ball could not be detected, false otherwise.
	 */
	public boolean isDetected() {
		return undetectedFrameCount <= 5 && !position.isNaN();
	}
	
	/** Get the radius of the ball.
	 * 
	 * @return The radius of the ball in metres.
	 */
	public double getRadius() {
		return radius;
	}
	
	/** Get the ball's position.
	 * 
	 * @return The ball's position in metres relative to the centre of the pitch.
	 */
	public Vector2 getPosition() {
		return position;
	}

	/** Get the ball's linear velocity.
	 * 
	 * @return The ball's linear velocity in metres/second.
	 */
	public Vector2 getLinearVelocity() {
		return velocity;
	}
	
	/** Reset the ball's linear velocity.
	 * 
	 */
	public void resetLinearVelocity( Vector2 newLinearVelocity ) {
		velocity = newLinearVelocity;
		if( useKalman ) {
			kalman.state_pre().put( 2, velocity.getX() );
			kalman.state_pre().put( 3, velocity.getX() );
		}
	}
	
	//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$//
	
	/** Update the ball's position and velocity (based on the time elapsed since the last update).
	 * 
	 * @param newPosition The new estimated position of the ball.
	 * @param timeStep The amount of time elapsed from the last update.
	 */
	public void update( Vector2 newPosition, double timeStep ) {
		assert newPosition == null || !newPosition.isNaN();
		assert timeStep > 0.0;
		
		if( position.isNaN() ) {
			if( newPosition != null )
				reset( newPosition );
		} else if( newPosition != null ) {
			undetectedFrameCount = 0;
			if( useKalman ) {
				prediction = cvKalmanPredict( kalman, prediction );
				
				measured.put( 0, newPosition.getX() );
				measured.put( 1, newPosition.getY() );
				
				CvMat estimated  = cvKalmanCorrect( kalman, measured );
				
				position = new Vector2( estimated.get( 0 ), estimated.get( 1 ) );
				velocity = new Vector2( prediction.get( 2 ) / timeStep, prediction.get( 3 ) / timeStep );
			} else {
				velocity = newPosition.minus( position ).times( 1.0 / timeStep );
				position = newPosition;
			}
		} else {
			++ undetectedFrameCount;
			
			position = position.plus( velocity.times( timeStep ) );
			velocity = velocity.times( 0.9 );
		}
		
	}
	
	/** Return a clone of this BallState. */
	@Override
	public BallState clone() {
		BallState r = new BallState( position, radius );
		r.velocity = velocity;
		return r;
	}
	
	/** Reset the ball's position, and set the velocity to zero - useful after a misdetection.
	 * 
	 * @param newPosition The new estimated position of the ball.
	 */
	public void reset( Vector2 newPosition ) {
		assert newPosition != null;
		
		position = newPosition;
		velocity = Vector2.ZERO;
		
		if( !newPosition.isNaN() ) {
			kalman.state_pre().put( 0, newPosition.getX() );
			kalman.state_pre().put( 1, newPosition.getY() );
			kalman.state_pre().put( 2, 0.0 );
			kalman.state_pre().put( 3, 0.0 );
			cvSetIdentity( kalman.measurement_matrix() );
		}
	}
	
	private void setupTransitionMatrix() {
		transitionMatrix = CvMat.create( 4, 4, CV_32FC1 );
		transitionMatrix.put( 0, 0, 1 );
		transitionMatrix.put( 0, 1, 0 );
		transitionMatrix.put( 0, 2, 1 );
		transitionMatrix.put( 0, 3, 0 );
		
		transitionMatrix.put( 1, 0, 0 );
		transitionMatrix.put( 1, 1, 1 );
		transitionMatrix.put( 1, 2, 0 );
		transitionMatrix.put( 1, 3, 1 );
		
		transitionMatrix.put( 2, 0, 0 );
		transitionMatrix.put( 2, 1, 0 );
		transitionMatrix.put( 2, 2, 1 );
		transitionMatrix.put( 2, 3, 0 );
		
		transitionMatrix.put( 3, 0, 0 );
		transitionMatrix.put( 3, 1, 0 );
		transitionMatrix.put( 3, 2, 0 );
		transitionMatrix.put( 3, 3, 1 );
		
		kalman.transition_matrix( transitionMatrix );
	}
}
