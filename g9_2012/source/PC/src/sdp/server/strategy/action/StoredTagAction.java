package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public abstract class StoredTagAction implements Action {
	private int       tag;
	private Condition abortCondition;
	
	public StoredTagAction() {
		this.tag = 0;
	}
	
	public StoredTagAction( int tag ) {
		this.tag = tag;
	}
	
	@Override
	public void setTag( int newTag ) {
		this.tag = newTag;
	}
	
	@Override
	public int getTag() {
		return tag;
	}
	
	@Override
	public void setAbortCondition( Condition condition ) {
		abortCondition = condition;
	}
	
	@Override
	public Condition getAbortCondition() {
		return abortCondition;
	}
	
	@Override
	public boolean applyAction(  Strategy strategy  ) {
		if( abortCondition != null && abortCondition.test( strategy ) )
			return true;
		else
			return applyActionNoAbort( strategy );
	}
	
	abstract protected boolean applyActionNoAbort( Strategy strategy );
}
