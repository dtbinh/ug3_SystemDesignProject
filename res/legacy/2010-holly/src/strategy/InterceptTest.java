package strategy;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;

public class InterceptTest {
	
	int ballX;
	int ballY;
	boolean intercept;
	Point ballPreviousPosition = null;
	boolean interceptFinished = false;
	int ourX;
	int ourY;
	int ourAngle;
	Strategy strategy;
	ArrayList<int[]> commands = new ArrayList<int[]>();
	
	public InterceptTest(boolean intercept, int ballX, int ballY, int robotX, int robotY, int robotAngle) {
		
		this.ballX = ballX;
		this.ballY = ballY;
		this.intercept = intercept;
		this.ourX = robotX;
		this.ourY = robotY;
		this.ourAngle = robotAngle;
		this.strategy = new Strategy(true, false, 0,1,1);
		
		//ballThread adjusts ball's position at every specified time interval
		BallThread ballThread = new BallThread();
		ballThread.start();
	}
	
	public void computeStrategy() {
		if (interceptFinished == false) {
		if (intercept) {
			
			//use two frames in vision system to get angle
			//might need tweaking if testing with hand rolling the ball because it messes up vision
			
			if (ballX < 100) {
				//discard it altogether for testing, so we can roll the ball from the left side of the pitch
			}
			
			else {
				System.out.println("INTERCEPT: DETECTED BALL");			
				if (ballPreviousPosition == null) {
					ballPreviousPosition = new Point(ballX,ballY);
				}
				Point currentPosition = new Point(ballX,ballY);
				//no change in the ball's position
				if (currentPosition.distance(ballPreviousPosition) > 1) {

					interceptFinished = true;
					double ballAngle = Angles.getAngle(ballPreviousPosition, currentPosition);
					System.out.println("INTERCEPT: DETECTED ANGLE: " + ballAngle);
					int deltaX = getOurPosition().x - ballPreviousPosition.x;
//					int deltaY = getOurPosition().y - ballPreviousPosition.y;
					int predictedY = ballPreviousPosition.y - (int) (deltaX * Math.tan(Math.toRadians(ballAngle)));
					System.out.println("Our robot position: " + getOurPosition());
					System.out.println("Move to y position: " + predictedY);
					int distanceY = predictedY - getOurPosition().y;
					
					//assume we are facing downwards for now (facing positive direction)
					
					int[] cmd = new int[6];
					if (distanceY > 0)
						cmd[0] = 1;							
					else
						cmd[0] = 2;
					cmd[1] = Math.abs(distanceY);				//command value
					cmd[2] = 0;									//angle to turn
					cmd[3] = getOurPosition().x;				//way-point x
					cmd[4] = predictedY;				//way-point y
					cmd[5] = 700;								//speed
					if (commands.size() > 0) {
						commands.clear();
					}
					commands.add(cmd);
					sendCommands(commands);
				}
				ballPreviousPosition = currentPosition;
			}
		}
			}
	}
	
	public void sendCommands(ArrayList<int[]> commands) {
		int[] command = commands.get(0);
		ourX = command[3];
		ourY = command[4];
	}
	
	public Point getOurPosition() {
		return new Point(ourX,ourY);
	}

	public static void main(String args[]) {
		InterceptTest test = new InterceptTest(true,20,30,500,300,270);
	}

	public class BallThread extends Thread {
		
		int delay = 200;
		
		InterceptFrame frame;
		
		public BallThread() {
			frame = new InterceptFrame();
			frame.setSize(700,400);
			frame.setBackground(Color.green);
			frame.setVisible(true);
		}
		
		public void run() {
			while (true) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(ballX + " " + ballY);
				ballX += 10;
				ballY += 3;
				frame.update(frame.getGraphics());
				computeStrategy();
			}
		}
		
		public class InterceptFrame extends JFrame{
			public InterceptFrame() {
				this.setSize(700,400);
				this.setBackground(Color.green);
			}
			
			public void paint(Graphics g) {
				System.out.println("called paint");
				Graphics2D g2d = (Graphics2D) this.getGraphics();
				g2d.setColor(Color.green);
				g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
				g2d.setColor(Color.red);
				g2d.fillOval(ballX-12, ballY-12, 25, 25);
				g2d.setColor(Color.blue);
				g2d.fillRect(ourX-30, ourY-25, 60, 50);
			}
		}
	}	
}
