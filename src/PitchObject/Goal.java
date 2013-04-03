package PitchObject;

public class Goal extends PitchObject {
	private static final Goal goalL = new Goal(new Position(18, 218),
											   new Position(18, 131),
											   new Position(18, 306),
											   new Position(60, 218));
	private static final Goal goalR = new Goal(new Position(698, 210),
			   								   new Position(698, 122),
			   								   new Position(698, 299),
			   								   new Position(655, 210));
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
