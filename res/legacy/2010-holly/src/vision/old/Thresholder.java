package vision.old;
//package vision;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.Image;
//import java.awt.Transparency;
//import java.awt.color.ColorSpace;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.WindowEvent;
//import java.awt.image.BufferedImage;
//import java.awt.image.ColorModel;
//import java.awt.image.ComponentColorModel;
//import java.awt.image.DataBuffer;
//import java.awt.image.Raster;
//import java.awt.image.WritableRaster;
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import baseSystem.Singleton;
//import java.util.EventListener;
//import javax.imageio.ImageIO;
//import javax.swing.JFileChooser;
//import javax.swing.JTabbedPane;
//
//
//public class Thresholder extends JFrame implements EventListener {
//
//	private static final long serialVersionUID = 1L;
//	private static final int INFOPANEL_HEIGHT = 24;
//        private static final int THRESH_PANEL = 300;
//	private final int width;
//	private final int height;
//	private Singleton singleton;
//        JTabbedPane controlPanel;
//	private ImageProcessor3 ip;
//	private ImagePanel imagep;
//	private InfoPanel infop;
//
//        private ThreshPanel rgbThresh, hsvThresh;
//	private int[] coords;
//	private Image track;
//	private int mode;
//
//	// debug
//	ColorSpace cs;
//	ColorModel cm;
//	WritableRaster wraster;
//	BufferedImage bgimage, threshImage;
//
//	public Thresholder() {
//		// get pointer for IP since it will be needed later
//		// get the initial sizes from the Vision module
//		width = Vision.getPitchWidth() + THRESH_PANEL;
//		height = Vision.getPitchHeight() + INFOPANEL_HEIGHT;
//
//		// load the object information and image
//		track = null;
//		// set the default display mode
//		mode = 0;
//
//		// set-up other things
//		cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
//		cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
//
//		// handles window actions
//		addComponentListener(new ComponentAdapter() {
//			public void componentResized(ComponentEvent evt) {
//			}
//		});
//		addWindowListener(new java.awt.event.WindowAdapter() {
//			public void windowClosing(WindowEvent winEvt) {
//				System.exit(0);
//			}
//		});
//
//		// set layout and size
//		setLayout(new BorderLayout());
//		setSize(width, height);
//
//		// create and populate menus
//		JMenuBar menuBar = new JMenuBar();
//		JMenu fileMenu = new JMenu("File");
//		//JMenu modeMenu = new JMenu("Mode");
//
//		JMenuItem exitItem = new JMenuItem("Exit");
//                JMenuItem openImage = new JMenuItem("Open Image");
//		exitItem.setMnemonic('x');
////		JMenuItem clnModeItem = new JMenuItem("Clean");
////		JMenuItem locModeItem = new JMenuItem("Location");
////		JMenuItem blbModeItem = new JMenuItem("Blobs");
////		JMenuItem thrModeItem = new JMenuItem("Thresholding");
//
//                fileMenu.add(openImage);
//		fileMenu.add(exitItem);
////		modeMenu.add(clnModeItem);
////		modeMenu.add(locModeItem);
////		modeMenu.add(blbModeItem);
////		modeMenu.add(thrModeItem);
////                menuBar.add(modeMenu);
//
//		menuBar.add(fileMenu);
//
//		setJMenuBar(menuBar);
//
//
//		// handles opening an image action
//		openImage.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//                                JFileChooser fileOpen = new JFileChooser();
//                                if(fileOpen.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                                    try {
//                                        bgimage = ImageIO.read(fileOpen.getSelectedFile());
//                                        update(bgimage.getRaster());
//                                    } catch (IOException ex) {
//                                        System.out.println("Couldn't open the Image");
//                                        ex.printStackTrace();
//                                    }
//				}
//
//			}
//		});
//
//                // handles exit menu action
//		exitItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				System.exit(0);
//			}
//		});
//		// handles mode switches
////		clnModeItem.addActionListener(new ActionListener() {
////			public void actionPerformed(ActionEvent e) {
////				mode = 0;
////				imagep.revalidate();
////			}
////		});
////		locModeItem.addActionListener(new ActionListener() {
////			public void actionPerformed(ActionEvent e) {
////				mode = 1;
////				imagep.revalidate();
////			}
////		});
////		blbModeItem.addActionListener(new ActionListener() {
////			public void actionPerformed(ActionEvent e) {
////				mode = 2;
////				imagep.revalidate();
////			}
////		});
////		thrModeItem.addActionListener(new ActionListener() {
////			public void actionPerformed(ActionEvent e) {
////				mode = 3;
////				imagep.revalidate();
////			}
////		});
//
//		// add the panels
//                imagep = new ImagePanel(track);
//
//                controlPanel = new JTabbedPane();
//                rgbThresh = new ThreshPanel(this,ThreshPanel.RGB);
//                hsvThresh = new ThreshPanel(this,ThreshPanel.HSV);
//                controlPanel.addTab("RGB", rgbThresh);
//                controlPanel.addTab("HSV", hsvThresh);
//                //thresholds.add
//
//                //this.setLayout(layout);
//		getContentPane().add(imagep, BorderLayout.CENTER);
//                getContentPane().add(controlPanel, BorderLayout.EAST);
//
//               // getContentPane().add(infop, BorderLayout.SOUTH);
//
//		// get the frame ready for displaying
//		pack();
//		setVisible(true);
//	}
//
//	public void update(Raster raster) {
//		// this is just the current frame's background
//		wraster = raster.createCompatibleWritableRaster();
//		wraster.setDataElements(0, 0, raster);
//		threshImage = new BufferedImage(bgimage.getColorModel(), wraster, false, null);
//
//		// update the coordinates
////		this.coords = singleton.getCoordinates();
//
//		// update the GUI content
//		imagep.repaint();
////		infop.revalidate();
//	}
//
//        public void updateThresholds(IncomingDataEvent evt, int colorSpace) {
//            long start = System.currentTimeMillis();
//            int[] thArr = evt.getThresholds();
//            int[] pixel = new int[4];
//            threshImage.setData(bgimage.getData());
//            for(int x = 0; x < bgimage.getWidth(); x++) {
//                for(int y = 0; y < bgimage.getHeight(); y++) {
//                    pixel = bgimage.getRaster().getPixel(x, y, pixel);
//                   // System.out.println(pixel[0]+" "+pixel[1]+" "+pixel[2]);
//                    if(!isInThreshold(pixel, colorSpace, thArr[0], thArr[1], thArr[2], thArr[3], thArr[4], thArr[5])) {
//                        threshImage.getRaster().setPixel(x, y, new int[] {255, 255, 255});
//                    }
//                }
//            }
//            System.out.println("THRESHOLD MASK IS DONE: "+(System.currentTimeMillis()-start)+"ms");
//            imagep.repaint();
//        }
//
//        private boolean isInThreshold(int[] pixel, int colorSpace, int redLow, int greenLow, int blueLow, int redHigh, int greenHigh, int blueHigh) {
//            if(pixel!=null && pixel.length >= 3) {
//                int red = pixel[0];
//                int green = pixel[1];
//                int blue = pixel[2];
//                if(colorSpace == ThreshPanel.HSV) {
//                    /*
//                     * when working on HSV R is H, G is S, B is V
//                     */
//                    float[] hsbvals = new float[3];
//                    hsbvals = Color.RGBtoHSB(red, red, blue, hsbvals);
//                    red = Math.round(360 * hsbvals[0]);
//                    green = Math.round(100 * hsbvals[1]);
//                    blue = Math.round(100 * hsbvals[2]);
//                   // System.out.println("HSV values: "+red+" "+green+" "+blue);
//                }
//                //System.out.println("pixel not null: "+red+" "+green+" "+blue);
//                if((red >= redLow && red <= redHigh) && (green >= greenLow && green <= greenHigh) && (blue >= blueLow && blue <= blueHigh)) {
//                    return true;
//                }
//            }
//                return false;
//        }
//
//	class ImagePanel extends JPanel {
//
//		private static final long serialVersionUID = 1L;
//
//		public ImagePanel(Image tr) {
//			track = tr;
//			setPreferredSize(new Dimension(width - THRESH_PANEL, height - INFOPANEL_HEIGHT));
//		}
//
//        @Override
//		public void paintComponent(Graphics g) {
//			try {
//				if (mode == 0) {
//					track = getToolkit().createImage(threshImage.getSource());
//				} else if (mode == 1) {
//					track = getToolkit().createImage(ip.getDebugLocationImage().getSource());
//				} else if (mode == 2) {
//					track = getToolkit().createImage(ip.getDebugPlateImage().getSource());
//				} else if (mode == 3) {
//					track = getToolkit().createImage(ip.getDebugThreshImage().getSource());
//				}
//			} catch (NullPointerException e) {
//				// in case the images in IP have not been created yet
//				track = null;
//			}
//			g.drawImage(track, 0, 0, getWidth(), getHeight(), this);
//		}
//	}
//
//	class InfoPanel extends JPanel {
//
//		private static final long serialVersionUID = 1L;
//
//		public InfoPanel() {
//			setPreferredSize(new Dimension(width, INFOPANEL_HEIGHT));
//		}
//
//		public void paintComponent(Graphics g) {
//			g.setColor(Color.black);
//			g.setFont(new Font("Default", Font.BOLD, 14));
//			g.drawString("Ball:", 0, 15);
//			g.drawString(Integer.toString(coords[0]), 34, 15);
//			g.drawString(Integer.toString(coords[1]), 64, 15);
//			g.drawString("Blue:", 102, 15);
//			g.drawString(Integer.toString(coords[0]), 140, 15);
//			g.drawString(Integer.toString(coords[1]), 170, 15);
//			g.drawString(Integer.toString(coords[2]), 200, 15);
//			g.drawString("Yellow:", 240, 15);
//			g.drawString(Integer.toString(coords[0]), 296, 15);
//			g.drawString(Integer.toString(coords[1]), 326, 15);
//			g.drawString(Integer.toString(coords[2]), 356, 15);
//		}
//	}
//
//	public static void main(String[] args) {
//		Thresholder frame = new Thresholder();
//	}
//}
