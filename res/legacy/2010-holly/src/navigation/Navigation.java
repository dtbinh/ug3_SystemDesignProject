/**
 * Main class of the navigation subsystem
 */
package navigation;

/** This is a singleton class of the Navigation component
 * @author s0792034 + matt
 *
 */
public class Navigation {

        // we might want them to be float (or double)
        // take into account that you won't get sub-pixel precision out of the vision system, furthermore integer arithmetic is much faster than floating point one - Martin
	private int robotX; // robot location
	private int robotY;
	private int robotAngle, opponentAngle;
	private int ballX; // ball location
	private int ballY;
	private int opponentX; // opponent location
	private int opponentY;
        private static Navigation instance;

        private Navigation() {

        }

        public static Navigation getInstance() {
            if(instance == null) {
                instance = new Navigation();
            }
            return instance;
        }
	public void goToBall(int x, int y)
	{
		// use pitagora in order to determine angle
		double nx, ny, hyp, angle;
		nx = robotX - ballX;
		ny = robotY - ballY;
		hyp = (double) Math.sqrt(nx*nx+ny*ny);
		angle = (double) Math.acos(nx/hyp);

                /* TODO: call functions to go to the ball
                 * with the translation to the real distance
                 * turn(angle);
                 * goForward(getDistance(hyp))
                 */
	}


        public void updateBallPosition(int x, int y, int angle) {
            //TODO: are we going to store angle info or leave it out?
            ballX = x;
            ballY = y;
        }

        public void updateOpponentPosition(int x, int y, int angle) {
            opponentX = x;
            opponentY = y;
            opponentAngle = angle;
        }

        public void updateRobotPosition(int x, int y, int angle) {
            robotX = x;
            robotY = y;
            robotAngle = angle;
        }

        @Deprecated
        public void updateRobotAngle(int angle) {
            robotAngle = angle;
        }

	/** Main function used for testing Navigation component only
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
