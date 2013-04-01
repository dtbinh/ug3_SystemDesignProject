package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class UnsetFlagsAction extends StoredTagAction {
	private long newFlags;
	
	public UnsetFlagsAction( long newFlags ) {
		this.newFlags = newFlags;
	}
	
	@Override
	protected boolean applyActionNoAbort( Strategy strategy ) {
		strategy.getControl().unsetFlags( newFlags );
		return true;
	}

}
