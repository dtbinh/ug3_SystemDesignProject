package Commands;

import JavaVision.Position;

import java.awt.Point;
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

	public void pushMoveCommand(List<Point> points) {
		int numel = points.size();
		Position lastPosition = new Position(points.get(numel - 1));
		// Skip first element of list (current robot coordinates) - we're there already
		for (int i = numel - 1; i > 0; i--) {
			this.pushMoveCommand(new Position(points.get(i)), lastPosition, i == 1);
		}
	}

	public void pushMoveCommand(Position movePoint, Position rotatePoint,
								boolean hardRotate) {
		this.push(new MoveCommand(movePoint, rotatePoint, hardRotate));
	}

	public void pushKickCommand(Position kickPoint, Position ballPoint) {
		KickCommand kickCommand = new KickCommand();
		if (!this.contains(kickCommand)) {
			this.push(kickCommand);
			this.push(new MoveCommand(kickPoint, ballPoint, true));
		}
	}

	public void push(Command c) {
		stack.push(c);
	}

	public Command getFirst() {
		if (stack.isEmpty()) {
			return null ;
		} else {
			return stack.peek();
		}
	}

	public Command pop() {
		return stack.pop();
	}

	public void clear() {
		stack.clear();
	}

	public boolean contains(Command c) {
		return stack.search(c) != -1;
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int size() {
		return stack.size();
	}
}