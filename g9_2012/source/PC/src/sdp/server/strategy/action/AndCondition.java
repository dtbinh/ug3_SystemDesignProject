package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public class AndCondition implements Condition {
	private Condition a, b;
	
	public AndCondition( Condition a, Condition b ) {
		this.a = a;
		this.b = b;
	}
	
	public AndCondition( Condition a, Condition b, Condition c ) {
		this( a, new AndCondition( b, c ) );
	}
	
	public AndCondition( Condition a, Condition b, Condition c, Condition d ) {
		this( a, b, new AndCondition( c, d ) );
	}
	
	@Override
	public boolean test( Strategy strategy ) {
		return a.test( strategy ) && b.test( strategy );
	}
}
