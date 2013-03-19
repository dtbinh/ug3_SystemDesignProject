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
			//Position intersectionPoint = ball.intersectinPosition(ourRobot);
			Position retreatPoint = getRetreatPoint(shootingRight);
			if ((ball.getCoors() != null) && (!ourRobot.closeToPoint(retreatPoint))){
				plannedCommands.pushMoveToFacePoint(retreatPoint,theirGoal.getCoors());
				//planMoveToFace(retreatPoint, theirGoal.getCoors());
			}
			//planRotate(theirGoal.getCoors());
			System.out.println("Moving to retreat point " + retreatPoint);
		} else {
			if (taskNo == 1 || taskNo == 2) {
				// TODO: test
				// just stop when we have the ball- this should be enough
				sendZeros();
			} else if (taskNo == 3) {
				Position target = theirRobot.getTarget(theirGoal);
				int newX = (ourRobot.getCoors().getX() + target.getX())/2;
				Position movePoint = new Position(newX, target.getY());
				// TODO: implement
				// score a goal
				// 1) move to a point where the opponent doesn't intersect
				//    the line from our robot to goal centre
				
				if(!ourRobot.isFacing(target))
				{
					planMoveAndTurn(ourRobot.getCoors(), target); //face the enemy goal
					System.out.println("Rotating to face enemy goal... " + target);
				}
				else{
				// 2) move closer to goal
				
				//*do some object avoidance here*
				//we also need to make sure the ball continues to be in our possession during movement
				
				//planMoveAndTurn(theirGoal.getOptimalPosition(), theirGoal.getOptimalPosition());
				plannedCommands.pushMoveToFacePoint(movePoint, target);
				}
				// 3) shoot
				
				if(ball.robotHasBall(ourRobot) && ourRobot.isFacing(target) &&(ourRobot.withinKickingRange(target))) 
				{
					sendKickCommand(plannedCommands.pop());
					System.out.println("Kick!");
				}
				
				sendZeros();
			} else {
				System.err.println("Task not specified.");
				System.exit(0);
			}
		}
		playExecute();
	}

	private static Position getRetreatPoint(boolean shootDirection) {
		if(ourRobot.getCoors() == null){
			return ourGoal.getCoors();
		}
		
		int xPoint =  ourGoal.getOptimalPosition().getX();
		xPoint -= shootDirection ? -30 : 30;
		int yPoint = ball.getCoors().getY(); 

		return new Position (xPoint, yPoint);
	}


}
