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
	private static boolean score = false;
	private static boolean facing = false;
	//TODO: CHANGE TOMORROW BEFORE MILESTONE !!!!!!!!!!!!!
	private static int yLeft = 143;
	private static int yRight = 312;

	
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
		//score =false;
		if (!(ball.getCoors() == null)){
			Position temp = getOptimalGoal();
			System.out.println("Goal COORS X " + theirGoal.getCoors().getX() + " Y: " + theirGoal.getCoors().getY());
			System.out.println();
			System.out.println("Optimal COORS X " + temp.getX() + " Y: " + temp.getY());
			System.out.println();
			System.out.println("BALL COORS X " + ball.getCoors().getX() + " Y: " + ball.getCoors().getY());
		}
	
		//ourRobot.moveAndTurn(ball.getCoors(), ball.getCoors());
		planRotate(ball.getCoors());
		
		System.out.println((openPlay(ball.getCoors(), ourGoal.getCoors())));
		if((openPlay(ball.getCoors(), ourGoal.getCoors()))){
			//Release robot from retreating to x
			score = true;
		}
		System.out.println("is the ball within 100: " + score);

		
		if (!(ball.getCoors() ==null)) facing = ourRobot.isFacing(ball.getCoors());
		
		if(!score) {
			
			//Position intersectionPoint = ball.intersectinPosition(ourRobot);
			Position retreatPoint = getRetreatPoint(shootingRight);
			if ((ball.getCoors() != null)) //&& (!ourRobot.closeToPoint(retreatPoint))){
				//plannedCommands.pushMoveToFacePoint(retreatPoint,theirGoal.getCoors());
				planMoveToFace(retreatPoint, theirGoal.getCoors());
			//}
			//planRotate(theirGoal.getCoors());
			System.out.println("Moving to retreat point " + retreatPoint);
		} else {
			if (taskNo == 1 || taskNo == 2) {
				// TODO: test
				// just stop when we have the ball- this should be enough
				sendZeros();
			} else if (taskNo == 3) {
			      // score a goal
		         // 1) move to a point where the opponent doesn't intersect
		         //    the line from our robot to goal centre

			   
				
				System.out.println("Are we facing the ball: " + facing);
				//If we are not facing the ball go to the ball
				//TODO: potential go behind ball but that method needs fixed
			   if(!(ball.robotHasBall(ourRobot))){
				   Position behindBall = ball.pointBehindBall(theirGoal.getCoors(), 25);
				   //planMoveToFace(behindBall, ball.getCoors());
				   planMoveAndTurn(ball.getCoors(), ball.getCoors());
			   }else if (!ourRobot.isFacing(getOptimalGoal())) {
	              planMoveToFace(ourRobot.getCoors(), getOptimalGoal()); //face the enemy goal
	              
		          System.out.println("Rotating to face enemy goal... " + theirGoal.getOptimalPosition());
			     
		          // 2) move closer to goal
			       
			      //*do some object avoidance here*
			       //we also need to make sure the ball continues to be in our possession during movement
			      
			       planMoveToFace(theirGoal.getOptimalPosition(), theirGoal.getOptimalPosition());
			      
			         // 3) shoot
			       
			      if(ball.robotHasBall(ourRobot)) 
		           {
			         sendKickCommand();
			         System.out.println("Kick!");
			       }
			        
			        sendZeros();
			   }
		       

//				Position target = theirRobot.getTarget(theirGoal);
//				int newX = (ourRobot.getCoors().getX() + target.getX())/2;
//				Position movePoint = new Position(newX, target.getY());
//				// TODO: implement
//				// score a goal
//				// 1) move to a point where the opponent doesn't intersect
//				//    the line from our robot to goal centre
//				
//				if(!ourRobot.isFacing(target))
//				{
//					planMoveAndTurn(ourRobot.getCoors(), target); //face the enemy goal
//					System.out.println("Rotating to face enemy goal... " + target);
//				}
//				else{
//				// 2) move closer to goal
//				
//				//*do some object avoidance here*
//				//we also need to make sure the ball continues to be in our possession during movement
//				
//				//planMoveAndTurn(theirGoal.getOptimalPosition(), theirGoal.getOptimalPosition());
//				plannedCommands.pushMoveToFacePoint(movePoint, target);
//				}
//				// 3) shoot
//				
//				if(ball.robotHasBall(ourRobot) && ourRobot.isFacing(target) &&(ourRobot.withinKickingRange(target))) 
//				{
//					sendKickCommand(plannedCommands.pop());
//					System.out.println("Kick!");
//				}
//				
//				sendZeros();
//			} else {
//				System.err.println("Task not specified.");
//				System.exit(0);
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
	
	private static boolean openPlay(Position pos1, Position pos2){
		//Checks to see if the x distance is less than 100
		if (pos1 == null) return false;
		int xDist = (int) (Math.sqrt((double)Math.pow((pos1.getX() - pos2.getX()),2)));
		System.out.println("xDist = " + xDist);
		
		//Reason for not 35 is that goal initialises before coordinate 
		// goal x is 35 
		if(xDist <100 && (!(xDist == 35))){
			return true;
		}
		return false;
	}
	
	private static Position getOptimalGoal(){
		int robotY = theirRobot.getCoors().getY();
		if (robotY > yLeft && robotY < yRight){
			int midY;
			int right = yRight - robotY;
			int left = robotY - yLeft;
			if(left > right ){
				midY = (robotY + yLeft)/2;
			}else {
			    midY = (yRight + robotY)/2;
			}	
			if (shootingRight){
				return new Position(theirGoal.getCoors().getX() -150,midY);
			}else{
				return new Position(theirGoal.getCoors().getX() +150,midY);
			}
		}else{	
			return (theirGoal.getCoors());
		}
	}

}
