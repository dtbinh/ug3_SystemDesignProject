/**
 * 
 */
package vision.old;

import java.awt.Color;

/**
 * @author Laurentiu
 *
 */
public class Lab {

    static float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
    static float Ls, as, bs;
    static float eps = 216.f/24389.f;
    static float k = 24389.f/27.f;

    static float Xr = 0.964221f;  // reference white D50
    static float Yr = 1.0f;
    static float Zr = 0.825211f;
    static int[] lab = new int[3];

	/**
	 * Maps the RGB space to LAB. For more info : http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
	 * @param R
	 * @param G
	 * @param B
	 * @return returns int[] array with lab values
	 */
	public static int[] rgb2lab(int R, int G, int B) {
		//http://www.brucelindbloom.com
		  
		
		
		// RGB to XYZ
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		
		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Lab
		xr = X/Xr;
		yr = Y/Yr;
		zr = Z/Zr;
				
		if ( xr > eps )
			fx =  (float) Math.pow(xr, 1/3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);
		 
		if ( yr > eps )
			fy =  (float) Math.pow(yr, 1/3.);
		else
		fy = (float) ((k * yr + 16.) / 116.);
		
		if ( zr > eps )
			fz =  (float) Math.pow(zr, 1/3.);
		else
			fz = (float) ((k * zr + 16.) / 116);
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		lab[0] = (int) (2.55*Ls + .5);
		lab[1] = (int) (as + .5); 
		lab[2] = (int) (bs + .5);
		return lab;
	} 
	
	public static String toString(int[] lab)
	{
		return lab[0] + " " + lab[1]+" " + lab[2];
	} 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
                int[] lab = new int[3];
                float[] hsv = new float[3];
                long start = System.nanoTime();
		//Lab l = new Lab();
		
		System.out.println("Lab is " + toString(rgb2lab(255, 96, 54)) ); // 159 60 56
                System.out.println("Runtime: "+((double)(System.nanoTime()-start))/1000000.0+"ms");
                long start2 = System.nanoTime();
                Color.RGBtoHSB(252, 63, 61,hsv);
		System.out.println("HSV is "); // 147 71 47
                System.out.println("Runtime: "+((double)(System.nanoTime()-start2))/1000000.0+"ms");



	}

}
