package vision.old;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import javax.imageio.ImageIO;

/**
 *
 * @author Matt
 */
public class ColorMap implements Serializable {

    private Hashtable<Integer, int[]> pixelMap = new Hashtable<Integer, int[]>(400000);

    public Hashtable fillHashtable(String filename, Raster data) {
        if(new File(filename).exists()) {
            read(filename);
            System.out.println("Hashtable read!");
        }
        int pix, index = 0;
        int[] lab_vals = new int[3];
        int[] pixels = new int[data.getHeight()*data.getWidth()*3];
        data.getPixels(0, 0, data.getWidth(), data.getHeight(), pixels);
        System.out.println("Pixel count: "+data.getWidth()*data.getHeight());
        for(int i = 0; i < data.getWidth()*data.getHeight()*3; i=i+3) {
            pix = (pixels[i] << 16) | (pixels[i+1] << 8) | pixels[i+2];
            if(!pixelMap.containsKey(pix)) {
                index++;
                lab_vals = Lab.rgb2lab(pixels[i],pixels[i+1],pixels[i+2]);
                pixelMap.put(pix,new int[] {lab_vals[0], lab_vals[1], lab_vals[2]});
            }

        }
        if(save(filename)) {
            System.out.println("Hashtable saved!");
        }
        System.out.println("Unique pixel count: "+index);
        System.out.println("Hashtable size: "+pixelMap.size());
        return pixelMap;

    }

    public int[] getColor(int[] rgb) {
        int pix = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
        if(pixelMap.containsKey(pix)) {
            return (int[])pixelMap.get(pix);
        }
        else {
            int[] lab_vals = Lab.rgb2lab(rgb[0], rgb[1], rgb[2]);
            pixelMap.put(pix, lab_vals);
            return lab_vals;
        }
    }

    public boolean save(String filename) {
        FileOutputStream f = null;
        try {
            f = new FileOutputStream(filename);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(pixelMap);
            s.flush();
            s.close();
            f.flush();
            f.close();
            return true;
        }
        catch (IOException ex) {
            System.out.println("Could not open the file");
            ex.printStackTrace();
        }
        finally {
            try {
                f.close();
            } catch (IOException ex) {
                System.out.println("Could not close the FileOutputStream");
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean read(String filename) {
        FileInputStream f = null;
        try {
            f = new FileInputStream(filename);
            ObjectInputStream s = new ObjectInputStream(f);
            pixelMap = (Hashtable) s.readObject();
            return true;
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found!");
            ex.printStackTrace();
        }
        catch (IOException ex) {
            System.out.println("Could not open the file");
            ex.printStackTrace();
        }
        catch (ClassNotFoundException ex) {
            System.out.println("Could not find the type of the object");
            ex.printStackTrace();
        }
        finally {
            try {
                f.close();
            } catch (IOException ex) {
                System.out.println("Could not close the FileOutputStream");
                ex.printStackTrace();
            }
        }
        return false;
    }


    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        try {
                //image = ImageIO.read(new File("src/img/extremes/shot0008.png"));
                image = ImageIO.read(new File("/home/woosh/Projects/athletic-intelligence/Robinho/live_imgs/shot0014.png"));
        } catch (IOException e) {
                e.printStackTrace();
        }
        if(image.getHeight() != 576 || image.getWidth() != 768) {
            image = image.getSubimage(0, 0, Vision.getPitchWidth(), Vision.getPitchHeight());
        }
        Raster data = image.getData().createChild(0, 0, Vision.getPitchWidth(), Vision.getPitchHeight(), 0, 0, null);


        ColorMap map = new ColorMap();
        for(int i = 0; i < 10000; i++);
        long start = System.nanoTime();
        map.fillHashtable("colorMap.lab", data);
        System.out.println("Hashtable built time: "+(System.nanoTime()-start)/1000000.0+"ms");

//        for(int i = 0; i < 10000; i++);
//        start = System.nanoTime();
//        for(int i = 0; i < 1000000; i++) {
//            Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsv);
//        }
//        System.out.println("HSV Computing time: "+(System.nanoTime()-start)/1000000.0+"ms");
//        for(int i = 0; i < 10000; i++);
//        start = System.nanoTime();
//        for(int i = 0; i < 1000000; i++) {
//            Lab.rgb2lab(rgb[0], rgb[1], rgb[2]);
//        }
//        System.out.println("LAB Computing time: "+(System.nanoTime()-start)/1000000.0+"ms");

     //   System.out.println("Runtime: "+(System.nanoTime()-start)/1000000+"ms");
      //  System.out.println("Number of unique colors in the frame: "+pixelMap.size());

    }

}
