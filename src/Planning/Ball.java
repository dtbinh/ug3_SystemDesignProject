package Planning;
import JavaVision.Position;
public class Ball extends ObjectDetails {
private static Position coors2 = null;
private static Position coors3 = null;
static long time1;
static long time2;
static long time3;
public static int count = 0;
public static int count2 = 0;
static long difference;
static double distance;
static double speed1;
static double speed2;
private RealVector lastVelocity = null;
@Override public void setCoors(Position current){
coors3 = coors2;
coors2 = this.coors;
this.coors = current;
time3 = time2;
time2 = time1;
time1 = System.currentTimeMillis();
}
/*
public static void getCurrentCoor(){
difference = time2-time1;
distance = getBallDist();
}



public static double getBallDist(){
return Math.sqrt(Math.pow((coors2.getX() - this.coors.getX()), 2) +
Math.pow((coors2.getY() - this.coors.getY()),2));

}

public double getBallSpeed(){
getCurrentCoor();
return (distance/difference)*25;
}
*/
/**
* @return	2-D vector representing velocity of ball.
*/
/**
* Note from Sarah
* Why don't we just get the initial velocity, between times 1 and 2,
* then get the current velocity at times 3 and 4. So two types of get velocity methods.
* Makes acceleration accurate?
*/
private RealVector getVelocity() {
if (this.coors == null || coors2 == null || coors3 == null) {
return new RealVector(0, 0);
}
double interval = (double) Math.abs(time1 - time2);
double speedX = (coors2.getX() - this.coors.getX()) / interval;
double speedY = (coors2.getY() - this.coors.getY()) / interval;
return new RealVector(speedX, speedY);
}
/**
private RealVector getCurrentVelocity(){
double interval = (double) (time4 - time3);
double speedX = (coors4.getX() - coors3.getX()) / interval;
double speedY = (coors4.getY() - coors3.getY()) / interval;
return (new RealVector(speedX, speedY));
*/
// TODO: now it's just a guess -- should at least test factors
/**
* @return	2-D vector representing acceleration of ball. 
*/
/**
* Acceleration = change in velocity over change in time
*/
private RealVector getAcceleration() {
if (lastVelocity == null) {
lastVelocity = this.getVelocity();
return new RealVector(0, 0);
} else {
RealVector curVelocity = this.getVelocity();
double interval = (double) Math.abs(time1 - time3);
double accelX = (lastVelocity.getX() - curVelocity.getX()) / interval;
double accelY = (lastVelocity.getY() - curVelocity.getY()) / interval;
return new RealVector(accelX, accelY);
}
}
/**
* Method, returns first reachable position of ball. 
* 
* Iterates with a 400ms step until ball is reachable or prediction timespan reaches 8s. 
* 
* @param robotCoors Position of robot.
* @param robotSpeed Average speed of robot.
* @return Position where ball is reachable.
*/
public Position getReachableCoors(Position robotCoors, double robotSpeed) {
// get variables for predicting position
Position c = this.getCoors();
RealVector v = this.getVelocity();
RealVector a = this.getAcceleration();
// get variables for keeping position within boundaries
int minX = GeneralPlanningScript.vision.getMinX(); 
int maxX = GeneralPlanningScript.vision.getMaxX();
int minY = GeneralPlanningScript.vision.getMinY(); 
int maxY = GeneralPlanningScript.vision.getMaxY();
// main loop that tries to find a reachable position
for (int t = 400; t < 8001; t+=400) {
Position ballCoors = getPredictedCoors(t, c, v, a, false); 
ballCoors = reflectInside(ballCoors, minX, maxX, minY, maxY);
int timeToBall = (int) (RobotMath.euclidDist(robotCoors, ballCoors) / robotSpeed);
if (timeToBall <= t) return ballCoors;
}
return null;
}
/**
* Method, uses info about speed and acceleration of ball to 
* predict where it will be after the given time.
* 
* <p><b>*** Not sure whether useful (delete this comment if useful) ***</b>
* 
* @param timespan Time span to consider.
* @return Predicted position of ball.
*/
public Position getPredictedCoors(int timespan) {
return getPredictedCoors(timespan, this.getCoors(), 
this.getVelocity(), this.getAcceleration(), true);
}
// TODO: move to other class and make public (possibly useful for ANY moving objects)
/**
* Class for real 2-D vectors, used e.g. to represent velocity and acceleration.
* @author s1047194
*
*/
private class RealVector {
private final double x;
private final double y;
public RealVector(double x, double y) {	this.x = x;	this.y = y; }
public double getX() { return x; }
public double getY() { return y; }
}
// TODO: move to other class and make public (possibly useful for ANY moving objects)
/**
* Function, predicts where the object will be after the given time. 
* @param timespan Timespan to consider.
* @param objCoors Initial position of object.
* @param speed Velocity of moving object.
* @param accel Acceleration of moving object.
* @return Predicted position of object.
*/
private static Position getPredictedCoors(int timespan, Position objCoors, 
RealVector velocity, RealVector acceler, boolean reflect) {
// if object velocity reaches 0 within timespan
if (Math.abs(velocity.getX()) < Math.abs(acceler.getX() * timespan)) {
// reduce time span to "when the object stops"
timespan = (int) (velocity.getX() / acceler.getX()); 
}
int x = objCoors.getX() + getDisplacement(timespan, velocity.getX(), acceler.getX());
int y = objCoors.getY() + getDisplacement(timespan, velocity.getY(), acceler.getY());

Position predictedCoors = new Position(x, y);
if (reflect) { 
predictedCoors = reflectInside(predictedCoors, GeneralPlanningScript.vision); 
}
return predictedCoors;
}
// TODO: move to other class and make public (possibly useful for ANY moving objects)
/**
* Function, applies formula for covered distance.
* <p> Premises: <ul>
* <li> we only care about <b>covered</b> distance (initial position is 0) </li>
* <li> movement is <b>linear</b> for simplicity (only one direction) </li> </ul>
* <p> Formula is well known in equations of motion with constant linear acceleration. 
* <p> r = r_0 + v_0 * t + a * t^2 / 2
* @param speed Velocity (can be negative)
* @param accel Acceleration (probably against speed, i.e. deceleration) 
* @param timespan Time span of motion (should be positive)
* @return Covered distance.
*/
private static int getDisplacement(int timespan, double speed, double accel) {
return (int) (speed * timespan + accel * timespan * timespan / 2);
}
// TODO: move to other class and make public (possibly useful for ANY moving objects)
/**
* Method to reflect positions inside the pitch as if walls were mirrors.
* @param coors Coordinates to reflect.
* @param vision VisionReader that can get boundaries.
* @return Coordinates of inside "image" of position.
*/
private static Position reflectInside(Position coors, VisionReader vision) {
return reflectInside(coors, vision.getMinX(), vision.getMaxX(), 
vision.getMinY(), vision.getMaxY());
}
// TODO: move to other class and make public (possibly useful for ANY moving objects)
/**
* Method to reflect positions inside the pitch as if walls were mirrors.
* @param coors	Coordinates to reflect.
* @param minX Boundary (left).
* @param maxX Boundary (right).
* @param minY Boundary (top).
* @param maxY Boundary (bottom).
* @return Coordinates of inside "image" of position.
*/
private static Position reflectInside(Position coors, int minX, int maxX, int minY, int maxY) {
Position rCoors = new Position(coors.getX(), coors.getY());
while (rCoors.getX() < minX || rCoors.getX() > maxX) {
if (rCoors.getX() < minX) { rCoors.setX(2*minX - rCoors.getX()); }
else { rCoors.setX(2*maxX - rCoors.getX()); }
}
while (rCoors.getY() < minY || rCoors.getY() > maxY) {
if (rCoors.getY() < minY) { rCoors.setY(2*minY - rCoors.getY()); }
else { rCoors.setY(2*maxY - rCoors.getY()); }
}
return rCoors;
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