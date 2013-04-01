package sdp.server.strategy.action;

import sdp.server.Control;
import sdp.server.math.Vector2;
import sdp.server.strategy.Strategy;

public class DriveAction extends StoredTagAction implements MovementAction {
	private Vector2 gotoPosition;
		
	public DriveAction( Vector2 position ) {
		this.gotoPosition = position;
	}
	
	@Override
	public Vector2 getPosition( Strategy strategy ) {
		return gotoPosition;
	}
	
	@Override
	protected boolean applyActionNoAbort( Strategy strategy ) {
		Control ctrl = strategy.getControl();
		ctrl.unsetFlags( Control.F_STATIONARY );
		ctrl.setDestination( gotoPosition );
		return true;
	}
}
