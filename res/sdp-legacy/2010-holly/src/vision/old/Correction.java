/**
 * 
 */
package vision.old;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * @author laurentiu
 * 
 */
public class Correction {

	public static final String DEBUG_PICTURE = "correctedPic.jpg";
	public static final String DEBUG_PICTURE2 = "correctedPic2.jpg";
	
	public void barrel(Raster data) {

		double alphax = 0.02, alphay = 0.08;

		
		/* Level of antialiasing */
		int aa = 3; //3;

		int nx = data.getWidth();
		int ny = data.getHeight();
		int[] pixeldata = new int[3];
		int[] newpixeldata = new int[3];
		Raster newdata = data.createCompatibleWritableRaster();
		double x, y, r, x2, y2, x3, y3;
		int red = 0, green = 0, blue = 0, newred = 0, newgreen = 0, newblue = 0;

		/* Size of the output image and initialisation */
		int nxout = nx, nyout = ny;
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false,
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		/*
		WritableRaster wraster = data
				.createCompatibleWritableRaster();
		wraster.setDataElements(0, 0, data);
		BufferedImage newimage = new BufferedImage(cm, wraster, false,
				null);
	

		// Create first output image 
		//nxout = nx / 2;
		//nyout = ny / 2;

		
		// Distort the image even more 
		for (int i = 0; i < nxout; i++) {
			for (int j = 0; j < nyout; j++) {
				int aacount = 0;
				newred = 0;
				newgreen = 0;
				newblue = 0;

				for (int ii = 0; ii < aa; ii++) {
					for (int jj = 0; jj < aa; jj++) {
						x = (2 * (i + ii / (double) aa) - nxout)
								/ (double) nxout;
						y = (2 * (j + jj / (double) aa) - nyout)
								/ (double) nyout;
						r = x * x + y * y;
						x3 = x / (1 - alphax * r);
						y3 = y / (1 - alphay * r);
						x2 = x / (1 - alphax * (x3 * x3 + y3 * y3));
						y2 = y / (1 - alphay * (x3 * x3 + y3 * y3));
						int i2, j2;
						i2 = (int) ((x2 + 1) * nx / 2);
						j2 = (int) ((y2 + 1) * ny / 2);
						if (i2 >= 0 && i2 < nx && j2 >= 0 && j2 < ny) {
							data.getPixel(i2, j2, pixeldata);
							red = pixeldata[0];
							green = pixeldata[1];
							blue = pixeldata[2];
							newred += red;
							newgreen += green;
							newblue += blue;
							aacount++;
						}
						newpixeldata[0] = newred;
						newpixeldata[1] = newgreen;
						newpixeldata[2] = newblue;
						
						//System.out.println(aacount);
					}
				}
				if (aacount > 0) {
					
					red = newred / aacount;
					green = newgreen / aacount;
					blue = newblue / aacount;
					
					// setup what we need for drawing and exporting
					if(red>=0 && red<=255 && green>=0 && green<=255 && blue>=0 && blue<=255)
					{
						Color c = new Color(red, green, blue);
						//Color c = new Color(0,0,0);
						newimage.setRGB(i, j, c.getRGB());
					}
					else
					{
						System.out.println("Bad formed color" + red + " " +green+" " + blue+" "+newred+" "+newgreen+" " + newblue);
						Color c = Color.RED; 
						newimage.setRGB(i, j, c.getRGB());
					}
					
				}

			}
		}
		
		
		try {
			ImageIO.write(newimage, "jpg", new File(DEBUG_PICTURE));
		} catch (IOException e) {
			System.out.println("Could not save debug picture!");
			e.printStackTrace();
		}
		*/
		
		// Create the second image 
		WritableRaster wraster2 = data.createCompatibleWritableRaster();
		wraster2.setDataElements(0, 0, data);
		BufferedImage newimage2 = new BufferedImage(cm, wraster2, false,null);
		BufferedImage newimage3 = new BufferedImage(cm, wraster2, false,null);
		
		// DO image correction  
	   for (int i=0;i<nxout;i++) {
	      for (int j=0;j<nyout;j++) {
	         int aacount = 0;
	         newred = 0;
			 newgreen = 0;
		     newblue = 0;
	         
	         for (int ii=0;ii<aa;ii++) {
	            for (int jj=0;jj<aa;jj++) {
	               x = (2 * (i+ii/(double)aa) - nxout) / (double)nxout;
	               y = (2 * (j+jj/(double)aa) - nyout) / (double)nyout;
	               r = x*x + y*y;
	               x2 = x * (1 - alphax * r);
	               y2 = y * (1 - alphay * r);
	               int i2,j2;
	               i2 = (int) ((x2 + 1) * nxout / 2);
	               j2 = (int) ((y2 + 1) * nyout / 2);
	               if (i2 >= 0 && i2 < nxout && j2 >= 0 && j2 < nyout) {
	            	    //newimage.getRaster().getPixel(i2, j2, pixeldata);
	            	    data.getPixel(i2, j2, pixeldata);
						red = pixeldata[0];
						green = pixeldata[1];
						blue = pixeldata[2];
						newred += red;
						newgreen += green;
						newblue += blue;
						aacount++;
	               }
	               newpixeldata[0] = newred;
				   newpixeldata[1] = newgreen;
				   newpixeldata[2] = newblue;

        		   //System.out.println(aacount);
	            }
	         }
	         if (aacount > 0) {
					
					red = newred / aacount;
					green = newgreen / aacount;
					blue = newblue / aacount;
					// Draw_Pixel(output1,nxout,nyout,i,j,c);
					// setup what we need for drawing and exporting
					if(red>=0 && red<=255 && green>=0 && green<=255 && blue>=0 && blue<=255)
					{
						Color c = new Color(red, green, blue);
						//Color c = new Color(0,0,0);
						newimage2.setRGB(i, j, c.getRGB());
					}
					else
					{
						System.out.println("Bad formed color" + red + " " +green+" " + blue+" "+newred+" "+newgreen+" " + newblue);
						Color c = Color.RED; 
						newimage2.setRGB(i, j, c.getRGB());
					}
					
				}
	      }
	   }

	   try {
			ImageIO.write(newimage2, "jpg", new File(DEBUG_PICTURE2));
			ImageIO.write(newimage3, "jpg", new File(DEBUG_PICTURE2+"3"));
		} catch (IOException e) {
			System.out.println("Could not save debug picture!");
			e.printStackTrace();
		}
		
		
		
	}

	/**
	 * @param args
	 *
	 */
	public static void main(String[] args) {
		BufferedImage image = new BufferedImage(1, 1,
				BufferedImage.TYPE_3BYTE_BGR);
		long start = System.currentTimeMillis();
		try {
			image = ImageIO.read(new File("shot0014.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Raster data = image.getData();
		Correction c = new Correction();
		c.barrel(data);
		System.out.println("FPS: " + 1000
				/ (System.currentTimeMillis() - start)+" total time " + (System.currentTimeMillis() - start));
	}

}
