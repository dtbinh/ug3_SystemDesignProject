package sdp.server.nxt;

import java.io.IOException;

import sdp.server.RobotInterface;
import sdp.server.RobotState;
import sdp.server.World;
import sdp.server.math.Vector2;
import sdp.server.vision.Vision;

/** NxtWorld extend World, to provide access to the real, physical robot, this description is copied from World.java, which explains the design in more detail:
 *   The NxtWorld corresponds to the real, physical world. The desired wheel speeds are communicated to the robot, using the
 *   Communication class. The latter is also used to fetch an image from the camera which is processed by a Vision object to
 *   estimate the new positions and orientations which will be available to the Strategy in the next time step.
 */
public class NxtWorld extends World {
	private Camera         cam;
	private Communication  comm;
	private Vision         cv;
	private RobotInterface robot;
	
	public Camera getCamera() {
		return cam;
	}
	
	public Communication getCommunication() {
		return comm;
	}
	
	@Override
	public RobotInterface getRobotInterface( RobotState.Team team ) {
		return robot;
	}
	
	public Vision getVision() {
		return cv;
	}
	
	public NxtWorld( World.Settings settings ) {
		super( settings );
		
		this.comm   = new Communication();
		this.cam    = new Camera();
		this.cv     = new Vision( getScreenProjection() );
		this.robot  = new RobotInterface();
	}
	
	@Override
	public void initialise() throws FailedInitException {
		try {
			System.out.println("NxtWorld: Connecting camera...");
			cam.connect();
			System.out.println("NxtWorld: Initialising vision...");
			cv.initialise( cam.getCameraImage() );
		} catch( IOException e ) {
			throw new FailedInitException( e );
		}
	}
	
	private boolean wasBallDetected = false;
	private RobotState dribbler = null;
	
	@Override
	protected void update() {
		comm.update( robot.getWheelSpeeds() );
		
		if( comm.isConnected() && robot.shouldKick() )
			comm.kick();
		
		cam.update();
		cv.processImage(
			cam.getCameraImage(),
			robotStates,
			ballState,
			getTimeStep()
		);
		
		robot.update();
		
		//// TODO: This shouldn't be here.
		if( !ballState.isDetected() ) {
			if( wasBallDetected ) {
				dribbler = null;
				for( RobotState rs : robotStates ) {
					if( rs.isDetected() ) {
						Vector2 robPos   = rs.getPosition().plus( Vector2.fromAngle( rs.getRotation(), rs.getSize().getX()*0.5 ) );
						Vector2 toBall   = ballState.getPosition().minus( robPos );
						double  distance = toBall.computeNorm();
						double  robRad   = ballState.getRadius()*5.0;
						
						if( distance <= robRad ) {
							if( dribbler == null )
								dribbler = rs;
							else
								dribbler = null;
						}
					}
				}
			} else if( dribbler != null ) {
				ballState.reset(
					dribbler.getPosition().plus(
						Vector2.fromAngle(
							dribbler.getRotation(),
							dribbler.getSize().getX() * 0.4
						)
					)
				);
			}
			
			Vector2 pitch   = getPitchTopRight();
			Vector2 ballPos = ballState.getPosition();
			double ballRad = ballState.getRadius();
			boolean pitchCollision = false;
			
			if( ballPos.getX() > pitch.getX() - ballRad ) {
				ballPos = new Vector2( pitch.getX() - ballRad, ballPos.getY() );
				pitchCollision = true;
			} else if( ballPos.getX() < - pitch.getX() + ballRad ) {
				pitchCollision = true;
				ballPos = new Vector2( - pitch.getX() + ballRad, ballPos.getY() );
			}
			
			if( ballPos.getY() > pitch.getY() - ballRad ) {
				pitchCollision = true;
				ballPos = new Vector2( ballPos.getX(), pitch.getY() - ballRad );
			} else if( ballPos.getY() < - pitch.getY() + ballRad ) {
				pitchCollision = true;
				ballPos = new Vector2( ballPos.getX(), - pitch.getY() + ballRad );
			}
			
			if( pitchCollision )
				ballState.reset( ballPos );
		} else {
			dribbler = null;
		}
		
		wasBallDetected = ballState.isDetected();
		
		try {
			Thread.sleep( Math.max( 40 - (long)(getTimeStep() * 1000), 1 ) );
		} catch( InterruptedException e ) {}
	} 

	@Override
	public void dispose() {
		comm.disconnect();
		cam.disconnect();
	};
}
