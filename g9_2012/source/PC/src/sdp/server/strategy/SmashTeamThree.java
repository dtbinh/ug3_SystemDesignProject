package sdp.server.strategy;

import sdp.server.BallState;
import sdp.server.Control;
import sdp.server.PcServer;
import sdp.server.math.Vector2;
import sdp.server.strategy.action.KickBallAction;

public class SmashTeamThree extends Strategy {
	long changeAt = -1;
	double phi = 0.0;
	
	public SmashTeamThree( PcServer server ) {
		super( server );
		
	}
	
	public void enable() {
		super.enable();
		
		changeAt = System.currentTimeMillis() + 4000;
	}
	
	public void update() {
		if( shouldKick() )
			ctrl.kick();
		
		if( System.currentTimeMillis() > changeAt ) {
			System.out.println("SmashTeamThree: update(): Going to normal match strategy.");
			NormalMatchStrategy s = new NormalMatchStrategy( server, ourSide );
			s.action = KickBallAction.AROUND_OPPONENT;
			server.setStrategy( s );
		} else {
			ctrl.resetFlags( Control.F_DEFAULT & (~ Control.F_STATIONARY) );
			ctrl.setTargetAngle( ctrl.getCurrentRotation() + Math.PI/2.0 );
		}
		
		super.update();
	}
	
}
