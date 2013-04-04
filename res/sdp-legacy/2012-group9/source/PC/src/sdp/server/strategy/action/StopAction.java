package sdp.server.strategy.action;

import sdp.server.Control;
import sdp.server.strategy.Strategy;

public class StopAction extends StoredTagAction {

	@Override protected boolean applyActionNoAbort( Strategy strategy ) {
		strategy.getControl().setFlags( Control.F_STATIONARY );
		strategy.getControl().setDestination( strategy.getControl().getCurrentNavPoint() );
		return true;
	}
	
}
