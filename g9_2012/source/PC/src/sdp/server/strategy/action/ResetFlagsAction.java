package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class ResetFlagsAction extends StoredTagAction {
	private long newFlags;
	
	public ResetFlagsAction( long newFlags ) {
		this.newFlags = newFlags;
	}
	
	@Override
	protected boolean applyActionNoAbort( Strategy strategy ) {
		strategy.getControl().resetFlags( newFlags );
		return true;
	}
}
