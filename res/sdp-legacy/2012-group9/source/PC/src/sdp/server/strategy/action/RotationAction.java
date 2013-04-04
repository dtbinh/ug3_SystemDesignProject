package sdp.server.strategy.action;

import sdp.server.strategy.Strategy;

public interface RotationAction extends Action {
	public double getRotation( Strategy strategy );
}
