package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class SpeedChangeAction extends StoredTagAction implements SpeedAction {
	private double speed;
	
	public SpeedChangeAction( double newSpeed ) {
		speed = newSpeed;
	}
		
	@Override
	public double getSpeed( Strategy strategy ) {
		return speed;
	}
	
	@Override
	protected boolean applyActionNoAbort(Strategy strategy) {
		strategy.getControl().setCurrentSpeed( speed );
		return true;
	}

}
