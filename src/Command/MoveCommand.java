package Command;

import PitchObject.Position;

public class MoveCommand extends Command {
	//TODO: Delete errything.

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
	public Position getMoveTowardspoint() {
		return this.moveTowardsPoint;
	}
	public Position getRotateTowardsPoint() {
		return this.rotateTowardsPoint;
	}
	public boolean getHardRotate() {
		return this.shouldMovementEndFacingRotateTowardsPoint;
	}
	
	
}