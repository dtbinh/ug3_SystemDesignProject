package sdp.server.gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.WindowConstants;

import sdp.server.ScreenProjection;
import sdp.server.math.Vector2;

import com.googlecode.javacv.CanvasFrame;
import static com.googlecode.javacv.cpp.opencv_core.*;

/** VERY thin wrapper around CanvasFrame. This allows images to be shown using the showImage method(). */
public class ImageViewer extends CanvasFrame {
	private static final long serialVersionUID = -8624000383921431065L;
	
	private ArrayList< NewBufferedImageListener > buffListeners;
	private ArrayList< NewIplImageListener >      iplListeners;
	private ScreenProjection                      scrProj;
	private ShapeRenderer                         renderer;
	
	public interface NewBufferedImageListener {
		public void newBufferedImage( Graphics2D graphics );
	}
	
	public interface NewIplImageListener {
		public void newIplImage( IplImage image );
	}
	
	public interface Shape {
		public void draw( ScreenProjection screenProjection, IplImage image ); 
	}
	
	public class ShapeRenderer implements NewIplImageListener {
		private ArrayList< Shape > shapes;
		
		ShapeRenderer() {
			shapes = new ArrayList<Shape>();
		}
		
		void addShape( Shape shape ) {
			shapes.add( shape );
		}
		
		@Override
		public void newIplImage(IplImage image) {
			for( Shape s : shapes )
				s.draw( scrProj, image );
			
			shapes.clear();
		}
	}
	
	public static class Circle implements Shape {
		private CvScalar colour;
		private double   radius;
		private Vector2  centre;
		
		public Circle( Vector2 centre, double radius, int red, int green, int blue ) {
			this.centre = centre;
			this.radius = radius;
			this.colour = cvScalar( blue, green, red, 0 );
		}
		
		@Override public void draw( ScreenProjection s, IplImage img ) {
			CvPoint p = s.projectPosition( centre ).getCvPoint();
			int     r = (int) ( radius / s.getScale());
			
			cvCircle( img, p, r, colour, 1, 8, 0 );
		}
	}
	
	public static class Line implements Shape {
		private Vector2  p1, p2;
		private CvScalar colour;
		
		public Line( Vector2 p1, Vector2 p2, int red, int green, int blue ) {
			this.p1     = p1;
			this.p2     = p2;
			this.colour = cvScalar( blue, green, red, 0 );
		}
		
		@Override public void draw( ScreenProjection s, IplImage img ) {
			CvPoint c1 = s.projectPosition( p1 ).getCvPoint();
			CvPoint c2 = s.projectPosition( p2 ).getCvPoint();
						
			cvLine( img, c1, c2, colour, 1, 8, 0 );
		}
	}
	
	public ImageViewer( ScreenProjection screenProjection ) {
		super( "Video Feed" );
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		
		buffListeners = new ArrayList<ImageViewer.NewBufferedImageListener>();
		iplListeners  = new ArrayList<ImageViewer.NewIplImageListener>();
		scrProj       = screenProjection;
		renderer      = new ShapeRenderer();
				
		addNewIplImageListener( renderer );
	}
	
	public void drawShape( Shape shape ) {
		renderer.addShape( shape );
	}
	
	public void drawCircle( Vector2 centre, double radius, int red, int green, int blue ) {
		drawShape( new Circle( centre, radius, red, green, blue ) );
	}
	
	public void drawLine( Vector2 p1, Vector2 p2, int red, int green, int blue ) {
		drawShape( new Line( p1, p2, red, green, blue ) );
	}
	
	public void addNewBufferedImageListener( NewBufferedImageListener newBufferedImageListener ) {
		buffListeners.add( newBufferedImageListener );
	}
	
	public void addNewIplImageListener( NewIplImageListener newIplImageListener ) {
		iplListeners.add( newIplImageListener );
	}
	
	public void removeNewBufferedImageListener( NewBufferedImageListener newBufferedImageListener ) {
		buffListeners.remove( newBufferedImageListener );
	}
	
	public void removeNewIplImageListener( NewIplImageListener newIplImageListener ) {
		iplListeners.remove( newIplImageListener );
	}
	
	public void showImage( IplImage iplImage ) {
		if( iplImage != null ) {
			for( NewIplImageListener l : iplListeners )
				l.newIplImage( iplImage );
			
			BufferedImage buffImg = iplImage.getBufferedImage();
			Graphics2D    g       = buffImg.createGraphics();
			
			for( NewBufferedImageListener l : buffListeners )
				l.newBufferedImage( g );
			
			if( buffImg.getWidth() != getCanvas().getWidth() || buffImg.getHeight() != getCanvas().getHeight() ) {
				getCanvas().setSize( buffImg.getWidth(), buffImg.getHeight() );
				pack();
			}
			
			super.showImage( buffImg );
		}
	}
	
	public void showImage( BufferedImage buffImg ) {
		if( buffImg != null ) {
			Graphics2D g = buffImg.createGraphics();
			
			IplImage iplImage = IplImage.createFrom( buffImg );
			
			for( NewIplImageListener l : iplListeners )
				l.newIplImage( iplImage );
			
			buffImg = iplImage.getBufferedImage();
			
			for( NewBufferedImageListener l : buffListeners )
				l.newBufferedImage( g );
			
			if( buffImg.getWidth() != getCanvas().getWidth() || buffImg.getHeight() != getCanvas().getHeight() ) {
				getCanvas().setSize( buffImg.getWidth(), buffImg.getHeight() );
				pack();
			}
			iplImage.release();
			iplImage = null;
			super.showImage( buffImg );
		}
	}
	
	
	
}