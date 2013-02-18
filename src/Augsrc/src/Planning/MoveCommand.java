package Planning;

import JavaVision.Position;

public class MoveCommand extends Command {
	public Position moveTowardsPoint;
	public Position rotateTowardsPoint;
	public boolean shouldMovementEndFacingRotateTowardsPoint;
	
	public MoveCommand(Position moveTowardsPoint, Position rotateTowardsPoint,
					  boolean shouldMovementEndFacingRotateTowardsPoint) {
		this.moveTowardsPoint = moveTowardsPoint;
		this.rotateTowardsPoint = rotateTowardsPoint;
		// call it smefrtp for short ^^
		this.shouldMovementEndFacingRotateTowardsPoint = shouldMovementEndFacingRotateTowardsPoint;
	}
}