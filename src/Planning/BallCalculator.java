package Planning;
import JavaVision.*;

public class BallCalculator extends Thread{
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
	
	public static void getCurrentCoor(Ball ball1, Ball ball2){
		
		if (count == 1) {
			time2 = System.currentTimeMillis();
			difference = time2-time1;
			count = 0;		
			coors2 = ((ball2).getCoors());
			//System.out.println("2 :" + coors2);
			distance = getBallDist(coors1, coors2);
		}
		else{
			time1 = System.currentTimeMillis();
			count++;
			coors1 = ((ball1).getCoors());
			//System.out.println("1 :" + coors1);
			try {
				sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getCurrentCoor(ball1, ball2);
			
		}
	}
	
	
	public static double getBallDist(Position position1, Position position2){
		return Math.sqrt(Math.pow((position2.getX() - position1.getX()), 2) +
				Math.pow((position2.getY() - position1.getY()),2));
		
	}
	
	public static double getBallSpeed(Ball ball1, Ball ball2){
		
		getCurrentCoor(ball1, ball2);
		return distance/difference;
	}

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
			try {
				sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getBallDeltaSpeeds(ball1, ball2, ball3, ball4);
		}
	}
	
	public static double getBallAcceleration(Ball ball1, Ball ball2, Ball ball3, Ball ball4){
		getBallDeltaSpeeds(ball1, ball2, ball3, ball4);
		return (Math.abs(speed1 - speed2) / difference);
	}
	
/**
	private PointF speed = new PointF(0,0);
	private PointF acceleration = new PointF(0,0);
	private long lastSetTime;
	

	private LinkedList<Point> last5 = new LinkedList<Point>();
	public Position getCoors() {
		return coors;
	}

	public PointF getSpeed() {
		return speed;
	}

	public PointF getAcc() {
		return acceleration;
	}

	public void setCoors(Position pos) {
		long timeSinceLastSet = System.currentTimeMillis() - lastSetTime;
		
		if (last5.size() == 5) 
		{
				setSpeed(getRateOfChange(getAveragePoint(), new PointF(pos), timeSinceLastSet),
					timeSinceLastSet);
				lastSetTime = System.currentTimeMillis();
				last5.remove(0);
				last5.add(4,pos);
				coors = pos;
				
		}
		else
		{
			coors = pos;
			last5.add(coors);
		}import JavaVision.*;
		
		coors = pos;
	}
		public PointF getAveragePoint()
		{
			PointF ret = new PointF(0,0);
			for (Point p : last5)
			{
				ret.x += p.x;
				ret.y += p.y;
			}
			ret.x /= 5;
			ret.y /= 5;
			return ret;
		}

		public static final int WALL_TOP = ImageProcessor.yupperlimit;
	public static final int WALL_BOTTOM = ImageProcessor.ylowerlimit;
	public static final int WALL_LEFT = ImageProcessor.xlowerlimit;
	public static final int WALL_RIGHT = ImageProcessor.xupperlimit;

	public Point getExpectedPosition(int timeInFuture) {
		float dx = getCoors().x + getSpeed().x * timeInFuture + 0.5f;// * acceleration.x * timeInFuture * timeInFuture;
		float dy = getCoors().y + getSpeed().y * timeInFuture + 0.5f;// * acceleration.y * timeInFuture * timeInFuture;;
		
		if (dy > WALL_TOP)
		{
			//work out what time it would reach the wall then calculate from that spot where it will go next
			//actually for now, im going to assume the ball stops...
			dy = WALL_TOP;
		}
		
		if (dy < WALL_BOTTOM)
			dy = WALL_BOTTOM;
		
		if (dx < WALL_LEFT )
			dx = WALL_LEFT;
		
		if (dx > WALL_RIGHT)
			dx = WALL_RIGHT;
	
		
		return new PointF(dx,dy).toPoint();
	}

	private void setSpeed(PointF newSpeed, long timeSinceLastSet) {
		if (speed != null) {
		setAcceleration(getRateOfChange(speed, newSpeed, timeSinceLastSet));
		}

		speed = newSpeed;
	}

	private void setAcceleration(PointF acceleration) {
		this.acceleration = acceleration;
	}

	private PointF getRateOfChange(PointF oldP, PointF newP, long timeChange) {
		if (timeChange == 0)
			timeChange = 1;
		return new PointF((newP.x - oldP.x) / timeChange, (newP.y - oldP.y) / timeChange);
	}*/
}
