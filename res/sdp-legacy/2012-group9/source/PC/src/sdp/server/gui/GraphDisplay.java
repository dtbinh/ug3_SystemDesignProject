package sdp.server.gui;

import sdp.server.math.MathUtils;
import sdp.server.math.Vector2;

import static com.googlecode.javacv.cpp.opencv_core.*;

public class GraphDisplay implements ImageViewer.NewIplImageListener {
	private Vector2  pos, size;
	private double   min, max;
	private double[] points;
	private int      startIndex;
	private CvFont   font;
	private String   caption;
	private double   borderSize = 3.0;
	
	public GraphDisplay( String caption, Vector2 pos, Vector2 size, int nPoints, double min, double max ) {
		this.caption = caption;
		this.pos     = pos;
		this.size    = size;
		this.min     = min;
		this.max     = max;
		
		this.points     = new double[ nPoints ];
		this.font       = new CvFont();
		this.startIndex = 0;
		
		for( int i = 0 ; i < nPoints ; ++ i )
			points[ i ] = Double.NaN;
		
		
		
		cvInitFont( font, CV_FONT_HERSHEY_SIMPLEX|CV_FONT_ITALIC, 0.3,0.3, 0, 1, 8 );
	}
	
	void addPoint( double value ) {
		points[ startIndex ] = value;
		startIndex = ( startIndex + 1 ) % points.length;
	}
	
	private double valueToCoord( double yOffset, double v ) {
		double h = size.getY() - borderSize * 2.0 - yOffset;
		return pos.getY() + yOffset + (MathUtils.clamp( v, min, max ) - min) / (max - min) * h  + borderSize; 
	}
	
	@Override
	public void newIplImage(IplImage image) {
		int[]  ymin     = new int[ 1 ];
		CvSize  textCvSize = new CvSize();
		cvGetTextSize( caption, font, textCvSize, ymin );
		
		double  yOffset   = borderSize + textCvSize.height() + ymin[0];
		CvPoint textPos   = pos.plus( new Vector2( borderSize, yOffset ) ).getCvPoint();
		CvRect  boundRect = cvRect( pos.getIntX(), pos.getIntY(), size.getIntX(), size.getIntY() );
		
		yOffset = textCvSize.height() + borderSize;
		
		CvPoint prevPoint = null;
		for( int i = 0 ; i < points.length ; ++ i ) {
			CvPoint newPoint = null;
			double  value    = points[ (i + startIndex) % points.length ];
			
			if( !Double.isNaN( value ) ) {
				int yCoord = (int)Math.round( valueToCoord( yOffset, value ) );
				int xCoord = (int)Math.round( i * ( size.getX() - borderSize*2.0 ) / points.length  + borderSize + pos.getX() );
				newPoint = cvPoint( xCoord, yCoord );
			}
			
			if( prevPoint != null && newPoint != null ) {
				cvLine( image, prevPoint, newPoint, cvScalarAll(255), 1, 8, 0 );
			}
			
			prevPoint = newPoint;
		}
		
		cvPutText( image, caption, textPos, font, cvScalarAll( 255 ) );
		cvRectangleR( image, boundRect, cvScalarAll(255), 1, 8, 0 );		
	}
	
}
