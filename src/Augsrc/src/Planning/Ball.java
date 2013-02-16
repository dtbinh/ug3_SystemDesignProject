package Planning;

import JavaVision.Position;

public class Ball extends ObjectDetails {
	private static Position coors1;
	private static Position coors2;
	static long time1;
	static long time2;
	public static int count = 0;
	public static int count2 = 0;
	static long difference;
	static double distance;
	static double speed1;
	static double speed2;
	
	@Override public void setCoors(Position current){
		coors2 = this.coors;
		coors1 = current;
		time2 = time1;
		time1 = System.currentTimeMillis();
		this.coors = current;
	}
	
	public static void getCurrentCoor(){
			difference = time2-time1;
			distance = getBallDist();
	}
	
	
	
	public static double getBallDist(){
		return Math.sqrt(Math.pow((coors2.getX() - coors1.getX()), 2) +
				Math.pow((coors2.getY() - coors1.getY()),2));
		
	}
	
	public double getBallSpeed(){
		getCurrentCoor();
		return (distance/difference)*25;
	}
/*
	public static void getBallDeltaSpeeds(Ball ball1, Ball ball2, Ball ball3, Ball ball4){
		
		if (count2 == 1){
			getBallSpeed(ball3, ball4);
			speed2 = distance/difference;
			count2 = 0;
		}
		else {
			getBallSpeed(ball1, ball2);
			speed1 = distance/difference;
			count2++;
			getBallDeltaSpeeds(ball1, ball2, ball3, ball4);
		}
	}
	
	public static double getBallAcceleration(Ball ball1, Ball ball2, Ball ball3, Ball ball4){
		getBallDeltaSpeeds(ball1, ball2, ball3, ball4);
		return (Math.abs(speed1 - speed2) / difference);
	}
	*/
}
