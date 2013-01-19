/**
 * 
 */
package vision.old;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
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
 * @author Laurentiu
 *
 */
public class AdaptiveThresh {

	/**
	 * This method has as input a normal RGB image (its raster) 
	 * and outputs a black and white image in a boolean matrix
	 * @param data raster
	 * @param width of the raster in pixels
	 * @param height of the raster in pixels
	 * @return boolean matrix representing the image
	 */
	
	public boolean[][] getBlackWhiteThresh(Raster data, int width, int height)
	{
			
		
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		WritableRaster threshWRaster = data.createCompatibleWritableRaster();
	    threshWRaster.setDataElements(0, 0, data);
	    BufferedImage debugThreshImage = new BufferedImage(cm, threshWRaster, false, null);
	    
		int red, green, blue;
		int[] rgbvals = new int[3];
		boolean[][] binaryImg = new boolean[width][height];
		int[][] greyImg = new int[width][height];
		int[][] subimage; 
		int n=12; // chosen window
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				binaryImg[x][y] = false;
				data.getPixel(x, y, rgbvals);
				red = rgbvals[0];
				green = rgbvals[1];
				blue = rgbvals[2];
				greyImg[x][y] = (int) (0.2989 * red + 0.5870 * green  + 0.1140 * blue);

				
				}
			}
		
		
		int n2 = n/2;    
		for (int i = 0+n2; i < width-n2; i++) {
			for (int j = 0+n2; j < height-n2; j++) {
		        // extract subimage
				/*
				subimage = extract_image(i-n2,i+n2,j-n2,j+n2, greyImg);
		        int threshold = mean_raster(2*n2, 2*n2,subimage);
		       */
				int threshold = extract_thresh(i-n2,i+n2,j-n2,j+n2, greyImg);
		        if (greyImg[i][j]<threshold+10)
		        {
		        	binaryImg[i][j] = true;
		        	threshWRaster.setPixel(i, j, new int[] {0, 0, 255});
		        }
		        else
		        {
		        	binaryImg[i][j] = false;
		        	threshWRaster.setPixel(i, j, new int[] {255, 255, 255});
		        }
			}
			}
		
		try {
			ImageIO.write(debugThreshImage, "png", new File("adaptive"));
		} catch (IOException e) {
			System.out.println("VISION ERROR >> Could not save in adaptive thresholding" );
			e.printStackTrace();
		}
		
		return binaryImg;

	}
	
	public int extract_thresh(int ilow, int ihigh, int jlow, int jhigh, int[][] greyImg)
	{
		int t = 0;
		int Constant=12;
		int maxi = ihigh-ilow;
		int maxj = jhigh-jlow;
		//System.out.println( ilow+" "+ ihigh+" " +jlow+" "+jhigh);
		
		for (int i = ilow; i < ihigh; i++) {
			for (int j = jlow; j < jhigh; j++) {
				t += greyImg[i][j];
				//System.out.println(i+" "+j+" " + k+" "+l);
			}
		}
	
		return  (t/(maxi*maxj))-Constant;
	}
	
	public int[][] extract_image(int ilow, int ihigh, int jlow, int jhigh, int[][] greyImg)
	{
		int maxi = ihigh-ilow+1;
		int maxj = jhigh-jlow+1;
		int[][] subimage = new int[maxi][maxj];
		//System.out.println( ilow+" "+ ihigh+" " +jlow+" "+jhigh);
		
		for (int i = ilow, k=0; i < ihigh; i++, k++) {
			for (int j = jlow, l=0; j < jhigh; j++, l++) {
				subimage[k][l] = greyImg[i][j];
				//System.out.println(i+" "+j+" " + k+" "+l);
			}
		}
	
		return subimage;
	}
	
	public int mean_raster(int ihigh, int jhigh, int[][] subimg)
	{
		int t = 0;
		int Constant=12;
		for (int i = 0; i < ihigh; i++) {
			for (int j = 0; j < jhigh; j++) {
				t += subimg[i][j];
			}
		}
		
		return (t/(ihigh*jhigh))-Constant;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		try {
			//image = ImageIO.read(new File("src/img/extremes/shot0008.png"));
			image = ImageIO.read(new File("shot0007.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Raster data = image.getData();

		AdaptiveThresh at = new AdaptiveThresh();
		
		//while (true) {
			long start = System.currentTimeMillis();
			at.getBlackWhiteThresh(data, data.getWidth(), data.getHeight());
			System.out.println((System.currentTimeMillis() - start) + " ms");
			System.out.println("FPS: " + 1000 / (System.currentTimeMillis() - start));
		//}
	}

}
