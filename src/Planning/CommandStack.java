package Planning;

import JavaVision.Position;
import java.awt.Point;
import java.util.List;
import java.util.LinkedList;

/**
 * Holds the actions the robot wants to perform
 * 
 * @author c-w
 */
public class CommandStack {
	private LinkedList<Command> stack;

	public CommandStack() {
		stack = new LinkedList<Command>();
	}

	public Command getFirst() {
		if (stack.isEmpty()) {
			return null ;
		} else {
			return stack.getFirst();
		}
	}

	public Command pop() {
		return stack.pop();
	}

	public void clear() {
		stack.clear();
	}
	
	public void pushMoveCommand(List<Point> points) {
		int numel = points.size();
		Position lastPosition = new Position(points.get(numel - 1));
		// Skip first element of list (current robot coordinates) - we're there already
		for (int i = numel - 1; i > 0; i--) {
			pushMoveCommand(new Position(points.get(i)), lastPosition, i == 1);
		}
	}

	public void pushMoveCommand(Position movePoint, Position rotatePoint,
								boolean hardRotate) {
		stack.push(new MoveCommand(movePoint, rotatePoint, hardRotate));
		//while (stack.size()> 10){
		//	stack.remove(10);
		//}
		//cleaned the stack to avoid memory issues.
	}
	
	public void pushKickCommand(Position kickPoint, Position ballPoint) {
		KickCommand kickCommand = new KickCommand();
		if (!stack.contains(kickCommand)) {
			stack.push(kickCommand);
			stack.push(new MoveCommand(kickPoint, ballPoint, true));
		}
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int size() {
		return stack.size();
	}
}