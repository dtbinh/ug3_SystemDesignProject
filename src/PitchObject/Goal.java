package PitchObject;

public class Goal extends PitchObject {
	private static final Goal goalL = new Goal(new Position(35, 240),
											   new Position(35, 171),
											   new Position(35, 325));
	private static final Goal goalR = new Goal(new Position(603, 240),
			   								   new Position(603, 166),
			   								   new Position(603, 312));
	private Position top;
	private Position bottom;

	public Goal(Position center, Position top, Position bottom) {
		this.coors = center;
		this.top = top;
		this.bottom = bottom;
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
}
