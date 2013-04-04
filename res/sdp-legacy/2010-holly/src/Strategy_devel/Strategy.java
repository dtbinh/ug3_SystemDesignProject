/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Strategy_devel;

import baseSystem.Singleton;
import movement.Movement;

/**
 *
 * @author sdp6
 */
public class Strategy {

	private final int BALL_FREE = 0;
	private final int OPPONENT_HAS_BALL = 1;
	private final int WE_HAVE_BALL = 2;
	private boolean finished = false;
	private int debug;
	private World world;
	private MovementCommands movement;

	private Singleton singleton;

	private int timesRotated = 0;

	public Strategy(int robotColor, int side, int debug) {

		this.debug = debug;
		world = new World(robotColor, side);
		movement = new MovementCommands(world);

	}

	public void run() {

		while (true) {
			Referee ref = new Referee();

			switch (ref.getGameState()) {

				case 0:
					timesRotated=0;
					//Paused - tell the robot to stop moving
					movement.stop();
					break;

				case 1:
					//Game in progress
					computeStrategy();
					break;

				case 2:
					timesRotated=0;
					//Taking penalty
					break;

				case 3:
					timesRotated=0;
					//Defending penalty
					break;
			}
		}
	}

	private void computeStrategy() {
		switch (world.getState()) {

			case (BALL_FREE):
				getToBall();
				timesRotated=0;
				break;

			case (OPPONENT_HAS_BALL):
				block();
				break;

			case (WE_HAVE_BALL):
				moveToScore();
				break;
		}
	}

	private void getToBall(){

		movement.moveToBall();

	}

	private void block(){

		//Are we between the opponent and the goal
		if(world.betweenOpponentAndGoal()){
			movement.mirrorAndCreep();
		}

		else{
			//Panic, run for our goal
			movement.retreat();
		}

	}

	private void moveToScore(){

		if(world.canScore()){
			
			movement.kick();
			timesRotated=0;
			return;
		}

		if(world.canScoreStrafeLeft()){

			movement.scoreStrafeLeft();
			timesRotated=0;
			return;
		}
		if(world.canScoreStrafeRight()){

			movement.scoreStrafeRight();
			timesRotated=0;
			return;
		}

		rotateToScore();
	}

	private void rotateToScore(){
		timesRotated++;
		
		//We're getting nowhere, do something different
		if(timesRotated>4){
			//Try strafing perhaps, or kicking against the wall
			movement.strafeWithBall();
		}
		else{
			movement.rotateAroundBall();
		}
	}
}
