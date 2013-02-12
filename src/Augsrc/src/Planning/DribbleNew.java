    package Planning;
     
    import au.edu.jcu.v4l4j.V4L4JConstants;
    import au.edu.jcu.v4l4j.exceptions.V4L4JException;
    import JavaVision.*;
     
    import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;
     
    public class DribbleNew extends Thread {
            public static final double TWOPI = Math.PI * 2;
            public static final double TENPI = Math.PI * 2;
            // Objects
            public static Ball ball;
            static WorldState state;
            static Move move;
            static DribbleNew instance = null;
            static Robot blueRobot;
            static Robot yellowRobot;
            private static ControlGUI thresholdsGUI;
            Vision vision;
            static double ballangle = 0;
            private static Context context;
            private static Socket socket;
            boolean wantsToRotate = false;
            boolean wantsToStop = false;
            boolean visitedBall = false;
            boolean visitedBehindPoint = false;
            Position dribblepoint;
            boolean hasSet = false;
            static String colour;
            static Robot ourRobot;
            public static int its = 0;
            static int dist;
            static int balldist;
            static Position behindBall = new Position(100,100);
            static Robot goalL;
            static Robot goalR;
           
            public static void main(String args[]) {
                    context = ZMQ.context(1);
                    colour = args[0];
     
                    //  Socket to talk to clients over IPC
                    socket = context.socket(ZMQ.REQ);
                    socket.connect("ipc:///tmp/nxt_bluetooth_robott");
                   
                    instance = new DribbleNew();
     
            }
     
            /**
             * Instantiate objects and start the planning thread
             */
            public DribbleNew() {
                   
                    blueRobot = new Robot();
                    yellowRobot = new Robot();
                    ball = new Ball();
                    move = new Move();
                    goalL = new Robot();
                    goalR = new Robot();
                   
                    goalR.setAngle(0);
                    goalR.setCoors(new Position(603,240));
                    start();
            }
     
            /**
             * Planning thread which begins planning loop - bluetooth server start will also go here later
             */
            public void run() {            
                    startVision();
                   
                    do {
                    try {
                            sleep(40);
                    } catch (InterruptedException e) {
                            System.out.println("Interrupted!");
                            e.printStackTrace();
                    }
                   
                    mainLoop();
                    }while(true);
            }
     
            /**
             * Method to initiate the vision
             */
            private void startVision() {       
                    /**
                     * Creates the control
                     * GUI, and initialises the image processing.
                     *
                     * @param args        Program arguments. Not used.
                     */
                    WorldState worldState = new WorldState();
                    ThresholdsState thresholdsState = new ThresholdsState();
     
                    /* Default to main pitch. */
                    PitchConstants pitchConstants = new PitchConstants(0);
     
                    /* Default values for the main vision window. */
                    String videoDevice = "/dev/video0";
                    int width = 640;
                    int height = 480;
                    int channel = 0;
                    int videoStandard = V4L4JConstants.STANDARD_PAL;
                    int compressionQuality = 100; //dropped compression of the camera slightly - feel free to experiment further
     
                    try {
                            /* Create a new Vision object to serve the main vision window. */
                            vision = new Vision(videoDevice, width, height, channel, videoStandard,
                                            compressionQuality, worldState, thresholdsState, pitchConstants);
     
                            /* Create the Control GUI for threshold setting/etc. */
                            thresholdsGUI = new ControlGUI(thresholdsState, worldState, pitchConstants);
                            thresholdsGUI.initGUI();
     
                    } catch (V4L4JException e) {
                            e.printStackTrace();
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
            }
     
     
            private void mainLoop() {
                   
                    getPitchInfo();
                    wantsToRotate = false;
                    wantsToStop = false;
                    if (colour.equals("yellow")     ){
                            ourRobot = yellowRobot;
                    } else{
                            ourRobot = blueRobot;
                    }
              
           
                   
                    // New stuff for setting position behind ball to initially move to
                    Position ballLocs = ball.getCoors();
                
                    Ball fakeBall = new Ball();
                    fakeBall.setCoors(behindBall);
                   
     
                    // Getting distance to this point
                    dist = move.getDist(ourRobot, fakeBall);
                    balldist = move.getDist(ourRobot, ball);
                    wantsToStop = (dist<20);
                    System.out.println("wantstostop " + wantsToStop);
                    System.out.println(ball.getCoors().getX() + " "+ ball.getCoors().getY());
                   
                    //set first time only
                    //the "its" variable is to give the vision system some time to initialise.
                    if ((!hasSet)&&(its>25)) {
                    		if(ourRobot.getCoors().getX() < ball.getCoors().getX()){
                                    //dribblepoint = new Position ((ball.getCoors().getX() + 100), ball.getCoors().getY());
                    				dribblepoint = projectPoint(ball.getCoors(), getAngleFromRobotToPoint(goalR, ball.getCoors()), 100);
                            }else{
                            		dribblepoint = projectPoint(ball.getCoors(), getAngleFromRobotToPoint(goalR, ball.getCoors()), -100);
                                    //dribblepoint = new Position ((ball.getCoors().getX() - 100), ball.getCoors().getY());
                            }
                    }
                   
                    behindBall = pointBehindBall(goalR, ballLocs);
                   
                    // Check our robot is facing the right way to dribble
                    // Maths might need to be changed in getMotorValues
                    // Specifically this line if (( !(wantsToStop) && !(visitedBall)) || ((dist<100) && isFacing(ourRobot, ball.getCoors())))
                    if (dist < 20 && its > 30 && isFacing(ourRobot, ball.getCoors())) visitedBehindPoint =true;  
                 
                   
                    // Go to point behind ball while rotating to face the "dribblepoint" I hope
                    String sig;
                    if (visitedBehindPoint) {
                            sig = getSigToPoint(ourRobot, dribblepoint, dribblepoint);
                            System.out.println("Going to DP");
                    } else {
                    		System.out.println("Going to point");
                            sig = getSigToPoint(ourRobot, behindBall, ball.getCoors());
                           
                            if(ourRobot.getCoors().getX() < ball.getCoors().getX()){
                                    //dribblepoint = new Position ((ball.getCoors().getX() + 100), ball.getCoors().getY());
                            		dribblepoint = projectPoint(ball.getCoors(), ((Math.PI)/2), 100 );
                                    hasSet = true;
                            }else{
                                    //dribblepoint = new Position ((ball.getCoors().getX() - 100), ball.getCoors().getY());
                            		dribblepoint = projectPoint(ball.getCoors(), ((Math.PI)/2), -100 );
                                    hasSet = true;
                            }
                           
                    }
                    System.out.println(behindBall.getX() + " " + behindBall.getY());
                    System.out.println(dribblepoint.getX() + " " + dribblepoint.getY());
                    Ball dribbleBall = new Ball();
                    dribbleBall.setCoors(dribblepoint);
                   
                    //I theorised that we didn't actually care about the Y co-ordinate, so I just compared X's - more reliable stopping achieved.
                    if (((dribblepoint.getX() < ourRobot.getCoors().getX()) && visitedBehindPoint) || (its < 30)){
                            sig = ("1 0 0 0 0");
                    }
                   
           
                   
                   
                                                   
                    socket.send(sig, 0);
                    System.out.println("Sending OK");
                    socket.recv(0);
                    System.out.println("Recieving OK");
                   
                    its++;
               
            }
     
     
            /**
             * Get the most recent information from vision
             */
            public void getPitchInfo() {
     
                    // Get pitch information from vision
                    state = vision.getWorldState();
                    ball.setCoors(new Position(state.getBallX(), state.getBallY()));       
                   
                    yellowRobot.setAngle(state.getYellowOrientation());
                    yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
                   
                    blueRobot.setAngle(state.getBlueOrientation());
                    blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
                           
                   
                    }
            //TODO See if this is even possible - I think motors suck too hard to implement this.
            public String shimmy(){
                   
                    return "1 0 0 0 0";
                   
            }
           
            //ALSO TODO - move this to a dedicated planning class, will need to mess about with bools.
           
           
            public double getRobotAngle(Robot robot){
                    // robot.getAngle() returns the angle between the robot and the left bottom
                    // corner of the screen
                    double robAngle = robot.getAngle();
                    // we rotate robAngle by 90 degrees in order to have the angle between the robot and
                    // the top left corner of the screen
                    // remember: top left corner of screen = (0, 0)
                    robAngle = robAngle - Math.PI/2;
                    robAngle += TENPI;
                    robAngle = robAngle % TWOPI;
                   
                   
                    return robAngle;
                   
            }
           
           
            public double getAngleFromRobotToPoint(Robot robot, Position point) {
                    // angleToPoint is the angle between the top left corner of the pitch and the point
                    // angleToRobot is the angle between the top left corner of the pitch and the robot
                    // angleBetweenRobotAndPoint is the clockwise angle between the robot and the point
                    double angleToPoint = Math.atan2( point.getY() - robot.getCoors().getY(), point.getX()-robot.getCoors().getX());
                    double angleToRobot = getRobotAngle(robot);
                    double angleBetweenRobotAndPoint = angleToPoint - angleToRobot;
                    //it was giving a weird slightly negative number here in the robot north, ball in q2 case. Resolved.
                    angleBetweenRobotAndPoint += TENPI;
                    angleBetweenRobotAndPoint = angleBetweenRobotAndPoint % TWOPI;
     
                    return angleBetweenRobotAndPoint;
            }
           
            public double getRotationValue(double angle){
                    double value = 0;;
                    if (angle > (Math.PI) ){
                            if (((Math.PI*2) - angle) > (Math.PI/10)) {
                                    value = 0.1;
                                    System.out.println("CCW rotation");
                            }
                           
                    } else if (angle > Math.PI/10) {
                            value = -0.1;
                            System.out.println("CW rotation");
                    }
                    wantsToRotate = (!(value == 0)) ;
           
                    return value;
            }
           
            public int[] getMotorValues(double rotationfactor, double angle){
                    double[] motors = {0,0,0,0};
                    int[] returnvalues = {0,0,0,0};
                    double multfactor = 200;
                    double maxval = 0.0001;
                    if (wantsToRotate) {
                            for (int i = 0; i<4;i++){
                                    motors[i] += rotationfactor;
                            }
                    }
                    if ( !(wantsToStop)  || (visitedBehindPoint)) {
                            System.out.println("moved");
                            motors[0] -= (Math.cos(angle));
                            motors[1] += (Math.sin(angle));
                            motors[2] -= (Math.sin(angle));
                            motors[3] += (Math.cos(angle));
                    }
                    System.out.println(visitedBehindPoint+ " "+ wantsToStop);
                   
                    if (wantsToStop && wantsToRotate){
                            multfactor = (multfactor/Math.abs(rotationfactor))/2;
//                   }  else if (visitedBehindPoint) {
//                           
//                            multfactor = multfactor/1.5;
                    }       else {
                            for (int i = 0; i<4;i++){
                                    if (Math.abs(motors[i]) > maxval) maxval = Math.abs(motors[i]);
                            }
                            multfactor = (multfactor/maxval);
                           
                    }
                   
                    for (int i = 0;i<4;i++) {
                            returnvalues[i] = (int) (motors[i]*multfactor);
                    }
                                   
                    return returnvalues;
                   
            }
           
            public String createSignal(int[] codes){
                    String sig = "1 "+codes[0]+" "+codes[1]+" "+codes[2]+" "+codes[3];
                   
                    return sig;
            }
           
            public String getSigToPoint(Robot robot, Position destination, Position rotation){
                    double movementangle = getAngleFromRobotToPoint(robot,destination);
                    double rotationangle = getAngleFromRobotToPoint(robot,rotation);
                    return createSignal(getMotorValues(getRotationValue(rotationangle),movementangle));
                   
            }
           
            public boolean isFacing(Robot robot, Position point){
            	
                    double angle = getAngleFromRobotToPoint(robot,point);
                    double value = getRotationValue(angle);
                   
                    return (value == 0);
            }
           
            public Position pointBehindBall(Robot goal, Position ball){
                   
                    //double goalBallAng = getAngleFromRobotToPoint(goal,ball);
                   
                    //double rvrsBallToGoal = Math.PI - goalBallAng;
            	    double rvrsBallToGoal = (3*Math.PI)/2;
                    Position goPoint;
                   
                    if(goal == goalL){
             
                            goPoint = projectPoint(ball, rvrsBallToGoal, -100);
                    }else{
                            
                            goPoint = projectPoint(ball, rvrsBallToGoal, 100);
                    }
                   
                    if (!withinPitch(goPoint)){
                            goPoint.setX((ball.getX()));
                            goPoint.setY((ball.getY()));
                    }
                    return goPoint;
                   
            }
            
            public static Position projectPoint(Position pos, double ang, int dist){
            	int newX = (int) (pos.getX() + (dist*Math.sin(ang)));
                int newY = (int) (pos.getY() + (dist*Math.cos(ang)));
                Position goPoint = new Position(newX,newY);
                return goPoint;
            }
           
            public static boolean withinPitch(Position coors){
                    int coorX = coors.getX();
                    int coorY = coors.getY();
                   
                    if(coorX > 39 && coorX < 602 && coorY > 100 && coorY < 389) return true;
                    return false;
            }
           
    }
