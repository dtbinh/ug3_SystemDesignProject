package Command;

import PitchObject.Position;

//import java.awt.Point;
import java.util.List;
import java.util.Stack;

/**
 * Holds the actions the robot wants to perform
 * 
 * @author c-w
 */
public class CommandStack {
	private Stack<Command> stack;

	public CommandStack() {
		stack = new Stack<Command>();
	}

	// Methods for pushing particular command types
	public void pushKickCommand() {
		this.push(new KickCommand());
	}
	public void pushMoveStraightCommand(Position destination){
		this.push(new MoveStraightCommand(destination));
	}
	public void pushMoveAndTurnCommand(Position destination, double direction){
		this.push(new MoveAndTurnCommand(destination, direction));
	}
	public void pushMoveToFaceCommand(Position destination, double direction){
		this.push(new MoveToFaceCommand(destination, direction));
	}
	public void pushRotateCommand(double direction) {
		this.push(new RotateCommand(direction));
	}

	
	/**
	 * Push move commands along a path
	 * @param points list of points in path
	 */
	public void pushMoveStraightPath(List<Position> points) {
		this.pushMovePath(points, false, 0, false);
	}
	/**
	 * Push move and turn commands along a path
	 * @param points list of points in path
	 * @param coorsToFace turn to face this Position
	 */
	public void pushMoveAndTurnPath(List<Position> points, Position coorsToFace) {
		Position lastPos = points.get(points.size()-1);
		double direction = lastPos.getAngleToPosition(coorsToFace);
		this.pushMovePath(points, true, direction, false);
	}
	/**
	 * Push move and turn commands along a path (+ at the end, take HARD turns)
	 * @param points list of points in path
	 * @param coorsToFace turn to face this Position
	 */
	public void pushMoveToFacePath(List<Position> points, Position coorsToFace) {
		Position lastPos = points.get(points.size()-1);
		double direction = lastPos.getAngleToPosition(coorsToFace);
		this.pushMovePath(points, true, direction, true);
	}
	
	/**
	 * Push move/move&turn commands along a given path (most general method)
	 * @param points list of points in path
	 * @param rotate turn while moving?
	 * @param direction if yes, towards which Position?
	 * @param face if yes, HARD rotate?
	 */
	private void pushMovePath(List<Position> points, boolean rotate, double direction, boolean face) {
		// Deal separately with last element of list
		Position lastPoint = points.get(points.size()-1);
		if (rotate) {
			if (face) {	this.pushMoveToFaceCommand(lastPoint, direction); }
			else      { this.pushMoveAndTurnCommand(lastPoint, direction); }
		}
		else          { this.pushMoveStraightCommand(lastPoint); }
		// Skip first element of list (current robot coordinates) - we're there already
		for (int i = points.size()-2; i > 0; i--) {
			if (rotate) { this.pushMoveAndTurnCommand(points.get(i), direction); }
			else        { this.pushMoveStraightCommand(points.get(i)); }
		}
		
	}
	
	public void pushMoveToFacePoint(Position destination, Position rotate){
		double direction = destination.getAngleToPosition(rotate);
		this.pushMoveToFaceCommand(destination, direction);
	}
	
	
	public void push(Command c) {
		stack.push(c);
	}

	public Command pop() {
		return stack.pop();
	}

	public void clear() {
		stack.clear();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int size() {
		return stack.size();
	}
}