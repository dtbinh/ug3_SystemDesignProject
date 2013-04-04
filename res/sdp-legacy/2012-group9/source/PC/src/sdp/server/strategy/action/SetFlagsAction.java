package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class SetFlagsAction extends StoredTagAction {
	private long newFlags;
		
	public SetFlagsAction( long newFlags ) {
		this.newFlags = newFlags;
	}
	
	@Override
	protected boolean applyActionNoAbort( Strategy strategy ) {
		strategy.getControl().setFlags( newFlags );
		return true;
	}

}
