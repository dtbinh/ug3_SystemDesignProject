package sdp.server.strategy;

import sdp.server.Control;
import sdp.server.PcServer;
import sdp.server.math.Vector2;

public class DefendPenaltyStrategy extends Strategy {
		private static final long forceStrategyChangeTimeout = 30000;
	
		private long     forceStrategyChangeAt; 
		private Vector2  oldPos;
		private boolean  ballMoved = false;
		
		public DefendPenaltyStrategy( PcServer server ) {
			super( server );
			ctrl = server.getControl();
		}
		
		@Override
		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public void enable() {
			super.enable();
			ourSide = Math.signum( ctrl.getRobotState().getPosition().getX() );
				
			forceStrategyChangeAt = System.currentTimeMillis() + forceStrategyChangeTimeout;
			oldPos = server.getWorld().getBallState().getPosition();	
		}

		@Override
		public void disable() {
			super.disable();
		}

		@Override
		public void update() {
			
			if( !ballMoved && server.getWorld().getBallState().getPosition().minus( oldPos ).computeNorm() > 0.1 ) {
				System.out.println("DefendPenaltyStrategy: update(): Ball moved, go into match strategy.");
				ctrl.resetFlags( Control.F_DEFAULT );
				ctrl.unsetFlags( Control.F_STATIONARY );
				driveTo( new Vector2( ctrl.getCurrentPivot().getX(), - Math.signum(ctrl.getCurrentPivot().getY()) * 0.25 ) );
				forceStrategyChangeAt = System.currentTimeMillis() + 300;
				ballMoved = true;
			} else if( System.currentTimeMillis() >= forceStrategyChangeAt ) {
				server.setStrategy( new NormalMatchStrategy( server, ourSide ) );
			}
			
			super.update();
		}
}
