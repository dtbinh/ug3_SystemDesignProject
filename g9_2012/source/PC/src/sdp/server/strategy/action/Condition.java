package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public interface Condition {
	boolean test( Strategy strategy );
}
