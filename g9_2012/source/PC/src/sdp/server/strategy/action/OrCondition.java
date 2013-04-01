package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class OrCondition implements Condition {
	private Condition a, b;
	
	public OrCondition( Condition a, Condition b ) {
		this.a = a;
		this.b = b;
	}
	
	public OrCondition( Condition a, Condition b, Condition c ) {
		this( a, new OrCondition( b, c ) );
	}
	
	public OrCondition( Condition a, Condition b, Condition c, Condition d ) {
		this( a, b, new OrCondition( c, d ) );
	}
	
	@Override
	public boolean test( Strategy strategy ) {
		return a.test( strategy ) || b.test( strategy );
	}
}
