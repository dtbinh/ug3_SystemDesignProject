package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;


public class WaitAction extends StoredTagAction {
	public WaitAction() {
		
	}
	
	public WaitAction( double timeoutInSeconds ) {
		setAbortCondition( new TimeoutCondition(timeoutInSeconds) );
	}
	
	public WaitAction( Condition untilCondition ) {
		setAbortCondition( untilCondition );
	}
	
	@Override
	public boolean applyActionNoAbort( Strategy strategy ) {
		return false;
	}

}
