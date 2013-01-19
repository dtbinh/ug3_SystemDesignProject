/*
 * This class stores all variables that are to be shared between subsystems
 */
package baseSystem;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import movement.Movement;
import strategy.Strategy;
import vision.Vision;

/**
 *
 * @author Ben Ledbury
 * @author Joe Tam
 */

public class Singleton {

	public final static int BLUE = 0;
	public final static int YELLOW = 1;
	public final static int LEFT = 0;
	public final static int RIGHT = 1;

	/*****************************************/
	public final static int OUR_COLOR = BLUE;
	/*****************************************/
	public final boolean RUN_WITHOUT_ROBOT = false;
	public final boolean RUN_WITHOUT_VISION = false;
	public final boolean USE_MOVEMENT_CORRECTION = false;
	public final boolean USE_DIAGONAL_MOVEMENT = true;

	boolean waypointChanged = false;

	final int VISION_DEBUG_LEVEL = 0;
	final int STRATEGY_DEBUG_LEVEL = 2;
	final boolean MOVEMENT_DEBUG_LEVEL = true;

    private static Singleton singleton;
    private Vision vision;
    private Strategy strategy;
    private Movement movement;

    public int ourColor;
    public int sidePlaying;

    private int framesSinceBallFound;
    private int framesSinceBlueFound;
    private int framesSinceBlueAngleFound;
    private int framesSinceYellowFound;
    private int framesSinceYellowAngleFound;

    long thresh = 500;

    int[] ballPos = {-1, -1, -1};
    int[] bluePos = {-1, -1, -1};
    int[] yellPos = {-1, -1, -1};

    boolean debug;
    boolean[] collided = new boolean[4];
    boolean awaitingCommands = true;

    private ArrayList<int[]> comm;

    private Singleton(int robotColor, int sidePlaying, boolean debug) {

        this.debug = debug;
        this.ourColor = robotColor;
        this.sidePlaying = sidePlaying;
        singleton = this;

		comm = new ArrayList<int[]>();

        startThreads();

        if(debug){
            startDebug();
        }
    }

    public static synchronized void startSingleton(int color, int side, boolean debug) {
        if (singleton == null) {
            singleton = new Singleton(color, side, debug);
        }
    }

    public static synchronized Singleton getSingleton() {
        return singleton;
    }

    public void startDebug() {

        while(true){
            try {
                System.out.println("Current coordinates:");
                System.out.println("Ball (" + ballPos[0] + "," + ballPos[1] + ")");
                System.out.println("Blue (" + bluePos[0] + "," + bluePos[1] + ") Angle: " + bluePos[2]);
                System.out.println("Yellow (" + yellPos[0] + "," + yellPos[1] + ") Angle: " + yellPos[2]);
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Singleton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void startThreads() {
        if (!RUN_WITHOUT_VISION) {
            vision = new Vision(VISION_DEBUG_LEVEL, true);
        }
        strategy = new Strategy(false, USE_MOVEMENT_CORRECTION | USE_DIAGONAL_MOVEMENT, ourColor, sidePlaying, STRATEGY_DEBUG_LEVEL);

        if (!RUN_WITHOUT_ROBOT) {
            movement = new Movement(ourColor, MOVEMENT_DEBUG_LEVEL, USE_MOVEMENT_CORRECTION, USE_DIAGONAL_MOVEMENT);
        }

        //Starts threads but does not yet notify them
        if (!RUN_WITHOUT_VISION) {
            vision.start();


            while (bluePos[0] == -1 && yellPos[0] == -1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Singleton.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (USE_MOVEMENT_CORRECTION) {
                movement.start();
            }
        }
        strategy.start();

    }

    /**
     * Getter and setter methods for all variables
     */
    public synchronized void setCoordinates(int[][] coordinates) {

        //Coords[2] is 0 if ball uncertain, 1 if ball certain and -1 if ball not found
        if(coordinates[2][0] == 0){
	        //If the ball was found but we are not certain update the coordinates but increment the counter
	        //IGNORE FOR NOW
                if(RUN_WITHOUT_VISION) {
                    ballPos = new int[]{coordinates[0][0], coordinates[1][0], coordinates[2][0]};
                }
	        framesSinceBallFound++;
        }

        //If we definitely found the ball then update the coordinates and reset the counter
        else if(coordinates[2][0] == 1){
	        ballPos = new int[]{coordinates[0][0], coordinates[1][0], coordinates[2][0]};
	        framesSinceBallFound = 0;
        }
        else{
			framesSinceBallFound++;
        }

        //If we found the blue plate then update the coordinates and reset the counter
        if(coordinates[3][0] != -1 && coordinates[4][0] != -1){
	        bluePos[0] = coordinates[3][0];
	        bluePos[1] = coordinates[4][0];
	        framesSinceBlueFound = 0;
        }
        else{
	        framesSinceBlueFound++;
        }

        //If we found the blue angle then update the coordinates and reset the counter
        if(coordinates[5][0] !=-1){
	        bluePos[2] = coordinates[5][0];
	        framesSinceBlueAngleFound = 0;
        }
        else{
	        framesSinceBlueAngleFound++;
        }

        //If we found the yellow plate then update the coordinates and reset the counter
        if(coordinates[6][0] !=-1 && coordinates[7][0]!=-1){
	        yellPos[0] = coordinates[6][0];
	        yellPos[1] = coordinates[7][0];
	        framesSinceYellowFound = 0;
        }
        else{
	        framesSinceYellowFound++;
        }

        //If we found the yellow angle then update the coordinates and reset the counter
        if(coordinates[8][0]!=-1){
	        yellPos[2] = coordinates[8][0];
	        framesSinceYellowAngleFound = 0;
        }
        else{
	        framesSinceYellowAngleFound++;
        }

    }

    public synchronized int[] getCoordinates() {
        return new int[]{ballPos[0], ballPos[1], ballPos[2], bluePos[0], bluePos[1], bluePos[2], yellPos[0], yellPos[1], yellPos[2]};
    }

    /**
     * For checking how old the current position data from Vision is.
     * @return Time in milliseconds.
     */
    public long getVisionDataAge() {
    	return (System.currentTimeMillis() - vision.getFrameTime());
    }

    public int getRobotColor() {
        return ourColor;
    }

    public void setSide(int side) {
    	this.sidePlaying = side;
    }

    public int getSide() {
        return sidePlaying;
    }


    public void setWaiting(boolean value){
        awaitingCommands = value;
    }

	public void setWaiting() {
		awaitingCommands = true;
	}

    public boolean getWaiting(){

        return awaitingCommands;

    }

    public void setCollided(boolean[] collisions){

    	for(int i=0; i<collisions.length; i++){
    		if(collisions[i]) this.collided[i] = collisions[i];
    	}
    }

    public boolean[] getCollided(){

    	boolean[] coll = new boolean[4];

    	//Ensure that a collision if one occurs is always read at least once
    	for(int i=0; i<collided.length; i++){
    		if(collided[i]){
    			coll[i] = collided[i];
    			collided[i] = false;
    		}
    	}

        return coll;
    }

    public void sendCommands(ArrayList<int[]> commands){

		waypointChanged = true;
    	if(commands.size()==0){
			comm.clear();
    		setWaiting();
    		return;
    	}

		comm = commands;

		if (RUN_WITHOUT_ROBOT == false) {

			if(!USE_MOVEMENT_CORRECTION){
				movement.executeArrayList(commands, strategy.getOppGoal());
			}
		}

    }

	public boolean waypointChanged(){
		if(waypointChanged){
			waypointChanged = false;
			return true;
		}
		return false;
	}

    public synchronized ArrayList<int[]> getCommands(){
    	return comm;
    }

	/**
	 * Should be only used by System Overview.
	 * @return Reference to strategy.
	 */
    public Strategy getStrategy() {
    	return strategy;
    }

	/**
	 * Should be only used by System Overview.
	 * @return Reference to movement.
	 */
	public Movement getMovement() {
		return movement;
	}

    public static void main(String[] args) {
        Singleton.startSingleton(OUR_COLOR, LEFT, false);
    }
}
