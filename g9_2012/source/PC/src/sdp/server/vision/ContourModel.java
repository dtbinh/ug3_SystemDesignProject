package sdp.server.vision;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import sdp.server.math.Vector2;

public class ContourModel {
	private int N_MOMENTS = 10;
	
	private double[] moments     = null;
	private double[] bestMoments = null;
	private double[] newMoments  = null;
	private boolean  doReset     = true;
	
	private double   detectionThresold = 160000;
	private int      suppressed        = 0;
	private int      maxSuppression    = 9;
	
	public ContourModel() {
		moments = new double[ N_MOMENTS ];
		newMoments = new double[ N_MOMENTS ];
	}
	
	public void reset() {
		doReset = true;
	}
	
	public Vector2 getCentroid() {
		return new Vector2( bestMoments[0], bestMoments[1] );
	}
	
	public CvSeq findContour( CvSeq contours, Vector2 velocity ) {
		if( contours == null || contours.isNull() )
			return null;
		
		
		CvSeq contour = null;
		if( doReset ) {
			contour = largestContour( contours );
			if( contour != null ) {
				computeContourMoments( contour );
				moments     = newMoments.clone();
				bestMoments = newMoments.clone();
				suppressed  = 0;
				//doReset     = false; // UNCOMMENT THIS LINE TO GET MOMENTS CHECKING
			}
		} else {
			double minError = Double.MAX_VALUE; 
			
			for( CvSeq c = contours ; c != null && !c.isNull() ; c = c.h_next() ) {
				computeContourMoments( c );
				moments[ 0 ] += velocity.getX();
				moments[ 1 ] += velocity.getY();
				double error = contourError( newMoments, moments );
				if( error < minError ) {
					minError    = error;
					contour     = c;
					bestMoments = newMoments.clone();
				}
			}
			
			if( minError > detectionThresold ) {
				++ suppressed;
				if( suppressed > maxSuppression ) {
					suppressed = 0;
				} else {
					contour = null;
				}
			} else {
				for( int i = 0 ; i < N_MOMENTS ; ++ i )
					moments[ i ] = bestMoments[ i ];
				
				if( suppressed > 0 )
					-- suppressed;
			}
		}
		
		return contour;
	}
	
	private double contourError( double[] a, double[] b ) {
		double e = 0.0;
		double d;
		for( int i = 0 ; i < 2 ; ++ i ) {
			d = (a[ i ] - b[ i ]) / 5.0;
			e += d * d;
		}
		
		d = (a[2]-b[2]) / 50.0;
		e += d*d;
		
		for( int i = 2 ; i < N_MOMENTS ; ++ i ) {
			d = a[ i ] - b[ i ];
			e += d * d;
		}
		
		return e;
	}
	
	private CvSeq largestContour( CvSeq contours ) {
		CvSeq  maxSeq  = null;
		double maxArea = 0.0;
		
		if( contours.isNull() )
			return null;
		else {
			for( CvSeq s = contours ; s != null && !s.isNull() ; s = s.h_next() ) {
				double area = Math.abs( cvContourArea( s, CV_WHOLE_SEQ, 0  ) );
				if( area > maxArea ) {
					maxArea = area;
					maxSeq  = s;
				}
			}
		}
		
		return maxSeq;
	}
	
	private void computeContourMoments( CvSeq contour ) {
		CvMoments moments = new CvMoments();
		CvHuMoments hu = new CvHuMoments();
		cvMoments( contour, moments, 0 );
		cvGetHuMoments( moments, hu );
		
		newMoments[ 0 ] = moments.m10() / moments.m00();
		newMoments[ 1 ] = moments.m01() / moments.m00();
		newMoments[ 2 ] = moments.m00();
		newMoments[ 3 ] = hu.hu1();
		newMoments[ 4 ] = hu.hu2();
		newMoments[ 5 ] = hu.hu3();
		newMoments[ 6 ] = hu.hu4();
		newMoments[ 7 ] = hu.hu5();
		newMoments[ 8 ] = hu.hu6();
		newMoments[ 9 ] = hu.hu7();
	}
}
