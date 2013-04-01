package sdp.server.vision;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import sdp.server.RobotState;
import sdp.server.ScreenProjection;
import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;


public class RobotModel extends EntityModel {
	private double area          = 500.0;
	private double compactness   = 0.4;
	private double areaTolerance = 0.8;
	private double compTolerance = 0.5;
	
	private ScreenProjection screen;
	private BlackCircleModel blackModel;
	private double           angle;
		
	private int suppressed          = 0;
	private int suppressFirstFrames = 5;
	private int suppressAngleFrames = 16;
	
	
	@Override
	protected double contourScore( CvSeq c, CvMoments moments ) {
		double  area        = computeArea( moments );
		double  perim       = cvContourPerimeter( c );
		double  compactness = (4 * Math.PI * area) / (perim * perim);
		
		double  compProp = Math.abs( compactness / this.compactness - 1.0 );
		double  areaProp = Math.abs( area / this.area - 1.0 );
				
		boolean compTest = compProp < compTolerance;
		boolean areaTest = areaProp < areaTolerance;
		
		if( compTest && areaTest )
			return - compProp - areaProp;
		else
			return Double.NaN;
	}
	
	public RobotModel( CvMemStorage storage, IplImage channel, IplImage blackChannel, IplImage temp1, ScreenProjection screen ) {
		super(storage, channel, temp1);
		this.screen     = screen;
		this.blackModel = new BlackCircleModel( storage, blackChannel, this, temp1 );
	}
	
	public double getAngle() {
		return angle;
	}
	
	public BlackCircleModel getBlackCircleModel() {
		return blackModel;
	}
	
	@Override
	public void setStorage( CvMemStorage newStorage ) {
		super.setStorage( newStorage );
		blackModel.setStorage( newStorage );
	}
	
	@Override
	public void setDebugImage( IplImage newDebugImage ) {
		super.setDebugImage( newDebugImage );
		blackModel.setDebugImage( newDebugImage );
	}
	
	@Override
	public void threshold( IplImage hsv[] ) {
		blackModel.threshold( hsv );
		super.threshold( hsv );
	}
	
	@Override
	public boolean detect() {		
		if( super.detect() ) {
			// Get the new centroid computed by super.detect()
			Vector2 centroid = getCentroid();
			
			// Detect the black circle
			Vector2 blackPos = blackModel.detect() ? blackModel.getCentroid() : null;
					
			// Detect the centroids of the two bars that make up the T
			Vector2[] split = computeSplitCentroids();
			
			Vector2[] linePoints = {
				blackPos,
				split[ 0 ],
				centroid,
				split[ 1 ]
			};
	
			angle = MathUtils.linearRegression( linePoints );
			
			return true;
		} else {
			return false;
		}
	}
	
	private double lineIntersection( Vector2 o1, Vector2 d1, Vector2 o2, Vector2 d2 ) {
		return ( o2.minus(o1) ).cross(d2) / d1.cross(d2);
	}
	
	private Vector2[] computeSplitCentroids() {
		CvSeq        robotContour  = getContour();
		Vector2      offset        = getRoiOffset();
		Vector2      robotCentroid = getCentroid().minus( offset );
		CvMemStorage storage       = getMemStorage();
		
		assert robotContour != null;
		assert storage      != null;
				
		// - Find the two largest convexity defects.
		CvSeq hull   = cvConvexHull2( robotContour, storage, CV_CLOCKWISE, 0 );
		CvSeq defect = cvConvexityDefects( robotContour, hull, storage );
		
		// We need to have at least two defects.
		if( defect.total() < 2 )
			return new Vector2[] { null, null };
		
		// Find the two defects of largest depth
		CvConvexityDefect[] bigDefects = { null, null };
		for( int i = 0 ; i < defect.total() ; ++ i ) {
			CvConvexityDefect d = new CvConvexityDefect( cvGetSeqElem( defect, i ) );
			if( bigDefects[ 0 ] == null || d.depth() > bigDefects[ 0 ].depth() ) {
				bigDefects[ 1 ] = bigDefects[ 0 ];
				bigDefects[ 0 ] = d;
			} else if( bigDefects[ 1 ] == null || d.depth() > bigDefects[ 1 ].depth() ) {
				bigDefects[ 1 ] = d;
			}
		}
		
		// - Find the line between the two defects - we'll cut the contour along it. 
		CvPoint[] cutLine = { bigDefects[ 0 ].depth_point(), bigDefects[ 1 ].depth_point() };
		int[] cutIdx = { -1, -1 };
		
		if( Math.abs( new Vector2( cutLine[0] ).minus( new Vector2(cutLine[1]) ).computeNorm() / 20 - 1.0 ) > 0.7 )
			return new Vector2[] { null, null };
		
		for( int i = 0 ; i < robotContour.total() ; ++ i ) {
			CvPoint point = new CvPoint( cvGetSeqElem( robotContour, i ) );
			if( point.x() == cutLine[ 0 ].x() && point.y() == cutLine[ 0 ].y() ) {
				cutIdx[ 0 ] = i;
				if( cutIdx[ 1 ] != -1 )
					break;
			}
			if( point.x() == cutLine[ 1 ].x() && point.y() == cutLine[ 1 ].y() ) {
				cutIdx[ 1 ] = i;
				if( cutIdx[ 0 ] != -1 )
					break;
			}
		}
		
		// Ensure the indices are in ascending order
		if( cutIdx[ 0 ] > cutIdx[ 1 ] ) {
			int aux = cutIdx[ 0 ];
			cutIdx[ 0 ] = cutIdx[ 1 ];
			cutIdx[ 1 ] = aux;
		}
		
		// - Cut up the contour into two pieces, along cutLine
		CvSeq[] contours = { null, null };
		contours[ 0 ] = cvSeqSlice( robotContour, cvSlice( cutIdx[0], cutIdx[1] + 1), storage, 0 );
		contours[ 1 ] = cvSeqSlice( robotContour, cvSlice( cutIdx[1] - robotContour.total(), cutIdx[0] + 1), storage, 0 );
		
		// - Get the direction line.
		// The line is defined by the centroids of the two contour slices
		Vector2[] centroids = { computeCentroid( contours[ 0 ] ), computeCentroid( contours[ 1 ] ) };				
		Vector2   direction = centroids[ 1 ].minus(centroids[ 0 ]).computeUnit();
		
		if( centroids[ 0 ].isNaN() || centroids[ 1 ].isNaN() )
			return new Vector2[] { null, null };
		
		// To figure out which way is forwards along direction, we compute the intersection of the line
		// with both contour slices. The direction will be from the intersection point closer to 
		// the centroid towards the one further away.
		Vector2[] intersects = { null, null };
		for( int i = 0 ; i < 2 ; ++ i ) {
			CvSeq c = contours[ i ];
			for( int j = 1 ; j < c.total() ; ++ j ) {
				Vector2 p1 = new Vector2( new CvPoint(cvGetSeqElem(c,j-1)) );
				Vector2 p2 = new Vector2( new CvPoint(cvGetSeqElem(c,j)) );
				Vector2 v  = p2.minus(p1);
				double d = lineIntersection( p1, v, robotCentroid, direction );
				double pix = 1.0 / v.computeNorm();
				if( d >= -pix && d <= 1.0 + pix ) {
					intersects[ i ] = p1.plus( v.times(d) );
					break;
				}
			}
		}
		
		// If, for whatever reason, we didin't find both the intersections
		// we use the centroids to figure out which way is forwards.
		boolean goodIntersects = intersects[0] != null && intersects[1] != null;
		if( !goodIntersects ) {
			intersects[ 0 ] = centroids[ 0 ];
			intersects[ 1 ] = centroids[ 1 ];
		}
			
		// If the intersection point of slice 1 is closer to the centroid, we need to reverse the direction.
		if( intersects[0].minus( robotCentroid ).computeSquaredNorm() > intersects[1].minus(robotCentroid).computeSquaredNorm() ) {
			direction  = direction.times( -1.0 );
			Vector2 aux = centroids[ 0 ];
			centroids[ 0 ] = centroids[ 1 ];
			centroids[ 1 ] = aux;
		}
		
		centroids[ 0 ] = centroids[ 0 ].plus( offset );
		centroids[ 1 ] = centroids[ 1 ].plus( offset );
		
		IplImage debugImage = getDebugImage();
		if( debugImage != null ) {
			CvScalar red = cvScalar( 0, 0, 255, 0 );
			CvScalar mag = cvScalar( 255, 0, 255, 0 );
			CvScalar cyn = cvScalar( 255, 255, 0, 0 );
			CvScalar whi = cvScalarAll(255);
			cvLine( debugImage, offset.plus( new Vector2( cutLine[0] ) ).getCvPoint(), offset.plus( new Vector2( cutLine[1] ) ).getCvPoint(), red, 1, 8, 0 );
			
			Vision.drawCenteredRectangle( debugImage, centroids[0], 3, mag, 1);
			Vision.drawCenteredRectangle( debugImage, centroids[1], 3, mag, 1);
			Vision.drawCenteredRectangle( debugImage, getCentroid(), 5, whi, 1);
			intersects[0]  = intersects[0].plus( offset );
			intersects[1]  = intersects[1].plus( offset );
			if( goodIntersects ) {
				Vision.drawCenteredRectangle( debugImage, intersects[0], 4, cyn, 1);
				Vision.drawCenteredRectangle( debugImage, intersects[1], 4, cyn, 1);
			} else {
				Vision.drawCenteredRectangle( debugImage, intersects[0], 4, cyn, 1);
				Vision.drawCenteredRectangle( debugImage, intersects[1], 4, cyn, 1);
			}
		}
		
		return centroids;
	}

	public void update( RobotState robot, double timeStep ) {
		if( getContour() == null )
			robot.update( null, 0.0, timeStep );
		else {
			//TODO: THIS should totally be in RobotState.
			int     frameId     = getFrameId();
			double  newAngle    = screen.unprojectAngle( getAngle() );
			Vector2 newPosition = screen.unprojectPosition( getCentroid() );
			
			// Smooth and filter the robot rotation.
			double oldAngle = robot.getRotation();
			
			if( frameId < suppressFirstFrames || suppressed > suppressAngleFrames ) {
				// If we're in the first few frames or if we've suppressed an angle change for too
				// long leave the newAngle unchanged. 
				suppressed = 0; 
			} else if( Math.abs( MathUtils.angleDiff( newAngle, oldAngle + robot.getAngularVelocity() * timeStep ) ) <= Math.PI/1.5 ) {
				// If the angle change is reasonable, don't suppress the angle, and smooth it
				// with the old one.
				suppressed = Math.max(0, suppressed - 1 );
			} else {
				// If the angle change is too big, suppress the angle change
				newAngle = oldAngle;
				suppressed ++;
				
				// Show the suppressed angle
				IplImage debugImage = getDebugImage();
				if( debugImage != null ) {
					Vector2 otherPoint = screen.projectPosition( newPosition ).plus( Vector2.fromAngle( newAngle ).times( 50.0 ) );
					cvLine( debugImage, screen.projectPosition( newPosition ).getCvPoint(), otherPoint.getCvPoint(), cvScalar(0,0,255,0), 1, 8, 0 );
				}
			}
			
			
			if( !newPosition.isNaN() && !Double.isNaN( newAngle ) )
				robot.update( newPosition, newAngle, timeStep );
		}
	}
	
}
