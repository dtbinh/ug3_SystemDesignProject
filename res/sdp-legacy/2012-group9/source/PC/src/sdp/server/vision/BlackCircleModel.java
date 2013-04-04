package sdp.server.vision;


import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

public class BlackCircleModel extends EntityModel {
	private RobotModel robotModel;

	private double    samplePointDist =  8;
	private double    area            = 40.0;
	private double    centroidDist    = 20.0;
	private double    areaTolerance   =  30.0;
	private double    circTolerance   =  0.3;
	private double    distTolerance   =  10;
	
	public BlackCircleModel( CvMemStorage storage, IplImage channel, RobotModel robotModel, IplImage temp1 ) {
		super( storage, channel, temp1 );
		this.robotModel = robotModel;
		setRoiSize( 128 );
		populateProperties();
	}
	
	@Override
	public boolean detect() {
		if( super.detect() ) {
			IplImage debugImage = getDebugImage();
			if( debugImage != null && robotModel.getContour() != null) {
				Vector2 robotCentroid = robotModel.getCentroid();
				CvSeq   robotContour  = robotModel.getContour();
				CvRect  robotBox      = cvBoundingRect( robotContour, 1 );
				Vector2 robotOffset   = robotModel.getRoiOffset();
				
				cvRectangleR( debugImage, MathUtils.offsetCvRect( robotBox, robotOffset.times(1.0) ), cvScalarAll(255), 1, 8, 0 );
				cvCircle( debugImage, getCentroid().getCvPoint(), 5, cvScalar(255, 0, 255, 0), 1, 8, 0);
				cvCircle( debugImage, robotCentroid.minus(getCentroid()).computeUnit().times( samplePointDist ).plus( robotCentroid ).getCvPoint(), 2, cvScalar(255, 0, 255, 0), 1, 8, 0);
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	protected double contourScore( CvSeq c, CvMoments moments ) {
		CvRect  bound         = cvBoundingRect( c, 1 );
		double  radius        = (double)(bound.width() + bound.height()) / 4;
		Vector2 centroid      = computeCentroid( moments );
		Vector2 robotCentroid = robotModel.getCentroid();
		double  area          = computeArea( moments );
		boolean smplTest      = true;
		Vector2 roiOffset     = getRoiOffset();
		
		centroid = centroid.plus( roiOffset );
		
		if( robotModel.getContour() != null ) {
			IplImage robotChannel = robotModel.getBinaryImage();
			CvSeq    robotContour = robotModel.getContour();
			CvRect   robotBox     = cvBoundingRect( robotContour, 1 );
			Vector2  robotOffset  = robotModel.getRoiOffset();
			Vector2 samplePt = robotCentroid.minus(centroid).computeUnit().times( samplePointDist ).plus( robotCentroid );
			
			if( MathUtils.isInBounds( samplePt.minus(robotOffset), robotBox, 1.0 ) ) {
				CvScalar s = cvGet2D( robotChannel, samplePt.getIntY(), samplePt.getIntX() );
				smplTest   = ((int)s.getVal(0)) > 0;
			} else
				smplTest = false;
		}
		
		double  circProp = Math.abs( radius / Math.sqrt( area / Math.PI ) - 1.0 );
		double  areaProp = Math.abs( area - this.area );
		double  distProp = Math.abs( robotCentroid.minus(centroid).computeNorm() - centroidDist );
		
		boolean circTest = circProp < circTolerance;
		boolean distTest = distProp < distTolerance;
		boolean areaTest = areaProp < areaTolerance;
		
		
		if(  smplTest && circTest && distTest && areaTest  ) {
			return - circProp - areaProp - distProp;
		} else
			return Double.NaN;
	}
	
	private void populateProperties() {
		new ReflectProperty( "SamplePtDist",       "samplePointDist", 0.0,   2.0, 0.1 );
		new ReflectProperty( "Area.Value",         "area",            0.0, 500.0, 1.0 );
		new ReflectProperty( "Area.Tolerance",     "areaTolerance",   0.0,   2.0, 0.1 );
		new ReflectProperty( "Circle.Tolerance",   "circTolerance",   0.0,   2.0, 0.1 );
		new ReflectProperty( "Distance.Value",     "centroidDist",    0.0, 100.0, 0.5 );
		new ReflectProperty( "Distance.Tolerance", "distTolerance",   0.0,   2.0, 0.1 );
	}
}
