package Planning;
import JavaVision.*;


public class HasBall extends ObjectDetails {

	protected Position coors;
        static Robot robot;

        
	
	public Position getCoors(Robot robot) {
		return coors;

	}

    public bool hasBall(Position coors){
	

    }

       public Point[] getCorners(ArrayList<Point> points){
Point centroid = getCentroid(points);
Point furthest = new Point(0,0);
Point opposite = new Point(0,0);
Point adjacent = new Point(0,0);
Point adjacent2 = new Point(0,0);
double dist = 0;
for(Point p : points){
if(Point.distance(p.x, p.y, centroid.x, centroid.y) > dist){
furthest = p;
dist = Point.distance(p.x, p.y, centroid.x, centroid.y);
}
}
dist = 0;
for(Point p : points){
if(Point.distance(p.x, p.y, furthest.x, furthest.y) > dist){
opposite = p;
dist = Point.distance(p.x, p.y, furthest.x, furthest.y);
}
}
dist = 0;
for(Point p : points){
if(Line2D.ptLineDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y) > dist){
adjacent = p;
dist = Line2D.ptLineDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y);
}
}
dist = 0;
ArrayList<Point> outside = new ArrayList<Point>();
for(Point p : points){
if(!isPointInTriangle(furthest,opposite,adjacent,p)){
outside.add(p);
}
}
for(Point p : outside){
if(Line2D.ptLineDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y) > dist){
adjacent2 = p;
dist = Line2D.ptLineDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y);
}
}
return new Point[] {
DistortionFix.barrelCorrected(furthest),
DistortionFix.barrelCorrected(opposite),
DistortionFix.barrelCorrected(adjacent),
DistortionFix.barrelCorrected(adjacent2)
};
}
	
	


}
