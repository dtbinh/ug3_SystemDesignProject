package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public interface Action {
	public void      setTag( int newTag );
	public int       getTag();
	public boolean   applyAction( Strategy strategy );
	public void      setAbortCondition( Condition condition );
	public Condition getAbortCondition();
}
