package sdp.server.strategy;

import sdp.server.Control;
import sdp.server.PcServer;
import sdp.server.math.Vector2;

public class PenaltyKickStrategy extends Strategy {
	private long startNormalAt = -1;
	
	public PenaltyKickStrategy( PcServer server ) {
		super( server );
	}

	@Override
	public void enable() {
		assert ctrl != null;
		assert ctrl.getRobotState() != null;
		assert ctrl.getRobotState().getPosition() != null;
		super.enable();
		ourSide = - Math.signum( ctrl.getRobotState().getPosition().getX() );
		ctrl.resetFlags( Control.F_DEFAULT );
		ctrl.unsetFlags( Control.F_STATIONARY );
		ctrl.setCurrentSpeed( 1.0 );
		
		Vector2 kickAt = computeWithOpponentKickPoint( world.getBallState().getPosition() );
		ctrl.setTargetAngle( kickAt.minus( ctrl.getCurrentPivot() ).computeAngle() );
	}

	@Override
	public void disable() {
		super.disable();
		enabled = false;
	}

	@Override
	public void update() {
		if( startNormalAt == -1 ) {
			ctrl.setDestination( ctrl.getRobotState().getPosition().plus( Vector2.fromAngle( ctrl.getRobotState().getRotation(), 0.15 ) ) );
			ctrl.kick();
			startNormalAt = System.currentTimeMillis() + 500;
		} else if( System.currentTimeMillis() > startNormalAt ) {
			server.setStrategy( new NormalMatchStrategy( server, ourSide ) );
		}
	}

}

