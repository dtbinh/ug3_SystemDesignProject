/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Strategy_devel;

import baseSystem.Singleton;
import java.awt.Point;
import java.util.ArrayList;
import movement.Movement;
import strategy.PathSearch;

/**
 *
 * @author sdp6
 */
public class MovementCommands {

	Singleton singleton;
	World world;
	ArrayList<int[]> commands;
	int[] command;

	public MovementCommands(World world) {
		singleton = Singleton.getSingleton();
		this.world = world;
	}

	protected void rotateAroundBall() {
		//TODO: for now just rotate

		command[0] = 5;									//command type, kick
		command[1] = 45;								//command value
		command[2] = 0;									//angle to turn
		command[3] = world.ballX;									//way-point x
		command[4] = world.ballY;									//way-point y
		command[5] = 255;

		singleton.sendCommands(commands);

	}

	protected void kick() {

		int[] kickCmd = new int[6];
			kickCmd[0] = 8;									//command type, kick
			kickCmd[1] = 900;								//command value
			kickCmd[2] = 0;									//angle to turn
			kickCmd[3] = 0;									//way-point x
			kickCmd[4] = 0;									//way-point y
			kickCmd[5] = 600;
		singleton.sendCommands(commands);
	}

	protected void moveToBall() {

		int ballOffSet_x = 0;
		int ballOffSet_y = 0;

		if (world.ourSide == world.LEFT) {
			ballOffSet_x = -45;
			commands = PathSearch.getPath2(new Point(world.ballX + ballOffSet_x, world.ballY + ballOffSet_y), world.getOurPosition(), world.getOurAngle(), world.getOppPosition(), world.getOppAngle(), world.LEFT);

		} else {
			ballOffSet_x = 45;
			commands = PathSearch.getPath2(new Point(world.ballX + ballOffSet_x, world.ballY + ballOffSet_y), world.getOurPosition(), world.getOurAngle(), world.getOppPosition(), world.getOppAngle(), world.RIGHT);

		}
		singleton.sendCommands(commands);

	}

	protected void strafeWithBall() {
		//TODO:
	}

	protected void retreat() {
		//TODO:
	}

	protected void mirror() {
		//TODO:
	}

	protected void mirrorAndCreep() {
		//TODO:
	}

	protected void scoreStrafeLeft() {

		command[0] = 4;									//command type, kick
		command[1] = 200;								//command value
		command[2] = 0;									//angle to turn
		command[3] = world.ballX;									//way-point x
		command[4] = world.ballY;									//way-point y
		command[5] = 255;

		singleton.sendCommands(commands);

	}

	protected void scoreStrafeRight() {

		command[0] = 3;									//command type, kick
		command[1] = 200;								//command value
		command[2] = 0;									//angle to turn
		command[3] = world.ballX;									//way-point x
		command[4] = world.ballY;									//way-point y
		command[5] = 255;

		singleton.sendCommands(commands);

	}

	protected void stop() {

		commands.clear();
		singleton.sendCommands(commands);

	}
}
