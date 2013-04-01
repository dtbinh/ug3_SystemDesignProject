package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class TimeoutCondition implements Condition {
	private long timeout;
	private long waitUntil;
	
	public TimeoutCondition( double timeoutInSeconds ) {
		this.timeout   = (long) (timeoutInSeconds * 1000);
		this.waitUntil = -1;
	}
	
	@Override
	public boolean test( Strategy strategy ) {
		if( waitUntil == -1 )
			waitUntil = System.currentTimeMillis() + timeout;
		
		return System.currentTimeMillis() > waitUntil;
	}
}
