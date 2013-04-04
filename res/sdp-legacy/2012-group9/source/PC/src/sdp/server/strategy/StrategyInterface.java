package sdp.server.strategy;

public interface StrategyInterface {
	public boolean isEnabled();
	public void enable();
	public void disable();
	public void update();
}
