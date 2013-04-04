package vision.old;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

/**
 * Test class to play around with possible fixes for the barrel distortion
 * caused by the camera
 * @author martin
 */
public class BarrelDistortionTest extends Component {

    BufferedImage img = null;

    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    public BarrelDistortionTest() {
        try {
            img = ImageIO.read(new File("src/img/shot0010.png"));//("src/vision/radial_distortion.png"));
            double fx = 1323;
            double cx = 535;
            double fy = 1325;
            double cy = 365;
            img = this.correct(new double[] {
                fx , 0  , cx ,
                0  , fy , cy ,
                0  , 0  , 1  }, 
                new double[] {-0.2, 0.12, -0.002, 0.002});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Dimension getPreferredSize() {
        if (img == null) {
            return new Dimension(100, 100);
        } else {
            return new Dimension(img.getWidth(), img.getHeight());
        }
    }

    // camMatrix[9] - intrinsic matrix
    // dist[4] - dist coeff
    // http://stefanix.net/ofcvcameracalibration
    // fulla -g -0.005:-0.015:-0.060:1.2 radial_distortion.png -o radial_distortion_corr.png
    public BufferedImage correct(double[] camMatrix, double[] dist) {
        int w = img.getWidth();
        int h = img.getHeight();

        double u0 = camMatrix[2];
        double v0 = camMatrix[5];
        double fx = camMatrix[0];
        double fy = camMatrix[4];
        double _fx = 1.0 / fx;
        double _fy = 1.0 / fy;
        double k1 = dist[0];
        double k2 = dist[1];
        double p1 = dist[2];
        double p2 = dist[3];

        BufferedImage processed = new BufferedImage(w+1000, h+1000, BufferedImage.TYPE_INT_ARGB);

        for (int v = 0; v <= h; v++) {
            for (int u = 0; u <= w; u++) {
                double y = (v - v0) * _fy;
                double y2 = y * y;
                double ky = 1 + (k1 + k2 * y2) * y2;
                double k2y = 2 * k2 * y2;
                double _2p1y = 2 * p1 * y;
                double _3p1y2 = 3 * p1 * y2;
                double p2y2 = p2 * y2;

                double x = (u - u0) * _fx;
                double x2 = x * x;
                double kx = (k1 + k2 * x2) * x2;
                double d = kx + ky + k2y * x2;
                double _u = fx * (x * (d + _2p1y) + p2y2 + (3 * p2) * x2) + u0;
                double _v = fy * (y * (d + (2 * p2) * x) + _3p1y2 + p1 * x2) + v0;

               // System.out.println(u + " " + v + " " + (int) Math.round(_u) + " " + (int) Math.round(_v));
                if(u<w && v<h && _u<w && _v<h)
                {
                    //processed.setRGB((int) Math.round(_u), (int) Math.round(_v), img.getRGB(u, v));
                    processed.setRGB((int) Math.round(_u), (int) Math.round(_v),Color.RED.getRGB());
                }
            }
        }
        return processed;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Image");

        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        long start = System.currentTimeMillis();
        f.add(new BarrelDistortionTest());
        System.out.println(System.currentTimeMillis()-start);
        f.pack();
        f.setVisible(true);
    }
}
