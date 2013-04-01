package sdp.server.strategy.action;

import sdp.server.math.Vector2;
import sdp.server.strategy.Strategy;

public interface MovementAction extends Action {
	public Vector2 getPosition( Strategy strategy );
}
