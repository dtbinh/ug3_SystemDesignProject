package PitchObject;

public class Goal extends PitchObject {
	private static final Goal goalL = new Goal(new Position(35, 240),
											   new Position(35, 171),
											   new Position(35, 325),
											   new Position(55, 240));
	private static final Goal goalR = new Goal(new Position(603, 240),
			   								   new Position(603, 166),
			   								   new Position(603, 312),
			   								   new Position(583, 240));
	private Position top;
	private Position bottom;
	private Position optimalPosition;

	public Goal(Position center, Position top, Position bottom,
			    Position optimalPosition) {
		this.coors = center;
		this.top = top;
		this.bottom = bottom;
		this.optimalPosition = optimalPosition;
		this.angle = (float) (3 * Math.PI / 2);
	}

	public static Goal goalL() {
		return goalL;
	}

	public static Goal goalR() {
		return goalR;
	}

	public Position getTop() {
		return this.top;
	}

	public Position getBottom() {
		return this.bottom;
	}
	
	public Position getOptimalPosition() {
		return this.optimalPosition;
	}
}
