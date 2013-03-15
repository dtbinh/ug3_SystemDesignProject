package Script;

import PitchObject.PitchObject;
import PitchObject.Position;

/**
 * GeneralPlanningScript is an outline for how all 
 * strategy scripts should look.
 * I recommend reading the documentation in RobotMath and 
 * ObjectAvoidance before starting any planning task. 
 * Being familiar with the ObjectDetails subclasses (balls and robots)
 * might also be of use
 * 
 * @see RobotMath
 * @see ObjectAvoidance
 * @see PitchObject
 * 
 * @author		c-w
 */
public class Milestone4Script extends AbstractBaseScript {
	private int taskNo;

	public static void main(String[] args) {
		Milestone4Script m4 = new Milestone4Script(args);
		m4.run();
	}

	public Milestone4Script(String[] args) {
		super(args);
		taskNo = Integer.parseInt(args[2]);
	}

	public void run() {
		while (true) {
			updateWorldState();
			doTask(this.taskNo);
		}
	}

	static void doTask(int taskNo) {
		/*
		 * Do we need to wait for the ball to move before we 
		 * start going towards it?
		 * For part 3 do we have to score past the robot?
		 */
		if(!ball.robotHasBall(ourRobot)) {
			Position intersectionPoint = ball.intersectinPosition(ourRobot);
			planMove(intersectionPoint);
			System.out.println("Moving to predicted point " + intersectionPoint);
		} else {
			if (taskNo == 1 || taskNo == 2) {
				// TODO: test
				// just stop when we have the ball- this should be enough
				sendZeros();
			} else if (taskNo == 3) {
				// TODO: implement
				// score a goal
				// 1) move to a point where the opponent doesn't intersect
				//    the line from our robot to goal centre
				// 2) move closer to goal
				// 3) shoot
			} else {
				System.err.println("Task not specified.");
				System.exit(0);
			}
		}
		playExecute();
	}
}
