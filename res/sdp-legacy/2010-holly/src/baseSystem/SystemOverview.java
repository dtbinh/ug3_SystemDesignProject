package baseSystem;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import movement.Movement;
import strategy.Strategy;
import vision.ImageProcessor;
import vision.Vision;

/*
 * TODO ability to reopen the frame once closed
 * TODO add JavaDoc
 */
/**
 * @author Martin
 * @author Ben
 * @author Matt
 * @author Tom
 */
public class SystemOverview extends JFrame implements Runnable{

    private static final long serialVersionUID = 1L;

    private static final int INFOPANEL_HEIGHT = 24;
    private static final int SIDEPANEL_WIDTH = 200;

    private final int width;
    private final int height;
    private final int imgheight;
    private ImageProcessor ip;
    private Singleton singleton;
    private Strategy strategy;
    private Movement movement;
    private ImagePanel imagep;
    private InfoPanel infop;
    private JTabbedPane sidep;
    private ControlPanel controlTab;
    private ThreshPanel threshTab;

    private int[][] coords;
    private int mode;
    private int newMode;
    private boolean drawStrategy;
    private int count;
    private long frameSum;
    private int mode_;
    private int fps;
    private long start;
    private List<int[]> colours;

    private ColorSpace cs;
    private ColorModel cm;
    private WritableRaster wraster;
    private Raster curFrame;
    private Raster curDebugThresh;
    private BufferedImage image;

    private boolean repaint;
    private boolean waiting;
    private ArrayList<int[]> commands;
    private ArrayList<ArrayList<int[]>> debugPlates;
    // button vars
    private JRadioButton normalModeButton;

    public SystemOverview(final ImageProcessor ip, Singleton singleton) {
        // get pointers to other subsystems for control purposes
        this.ip = ip;
        this.singleton = singleton;
        try {
            strategy = singleton.getStrategy();
        } catch (NullPointerException e) {
            strategy = null;
        }
        try {
            movement = singleton.getMovement();
        } catch (NullPointerException e) {
            movement = null;
        }

        // get the initial sizes from Vision
        width = Vision.getPitchWidth();
        imgheight = Vision.getPitchHeight();
        height = imgheight + INFOPANEL_HEIGHT;
        // load the object information and image
        coords = new int[9][2];
        cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        wraster = Raster.createInterleavedRaster(new DataBufferByte(new byte[imgheight * width * 3], imgheight * width * 3), width, imgheight, 3 * width, 3, new int[]{0, 1, 2}, null);
        image = new BufferedImage(cm, wraster, false, null);
        // set the default and restore display mode
        mode = 1;
        newMode=1;
        mode_ = 1;
        // drawing settings
        drawStrategy = true;
        // speed measuring
        fps = 0;
        start = System.currentTimeMillis();
        // red, orange, violet, cyan, pink (colours to use for blob labelling)
        colours = Arrays.asList(new int[]{255, 0, 0}, new int[]{255, 165, 0}, new int[]{127, 0, 255}, new int[]{0, 255, 255}, new int[]{255, 192, 203});

        // handles window actions
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                setVisible(false);
            }
        });

        // set layout and size
        setLayout(new BorderLayout());
        setSize(width, height);

        // create and populate menus
        addMenusAndActions();

        // add the panels
        imagep = new ImagePanel(image);
        infop = new InfoPanel();
        sidep = new JTabbedPane();
        // uncomment when devel closed
        if (strategy != null) {
            controlTab = new ControlPanel();
            sidep.addTab("Control", controlTab);
        }
        threshTab = new ThreshPanel();
        sidep.addTab("Thresholds", threshTab);
        getContentPane().add(imagep, BorderLayout.CENTER);
        getContentPane().add(infop, BorderLayout.SOUTH);
        getContentPane().add(sidep, BorderLayout.EAST);

        // get the frame ready for displaying
        pack();
        setTitle("System Overview");
        setVisible(true);
        imagep.createBufferStrategy(2);
    }

    /**
     * Adds a menu bar and respective action events.
     */
    private void addMenusAndActions() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenu modeMenu = new JMenu("Mode");
        modeMenu.setToolTipText("Change the display mode");
        JMenu settMenu = new JMenu("Settings");
        settMenu.setToolTipText("Change image processor settings");
        JMenu dblvMenu = new JMenu("Debug");
        dblvMenu.setToolTipText("Change the debug level");
        JMenu thrsMenu = new JMenu("Thresholding");
        thrsMenu.setToolTipText("Change threshold related settings");
        JMenu platMenu = new JMenu("Plate");
        platMenu.setToolTipText("Change plate settings");
        JMenu ballMenu = new JMenu("Ball");
        ballMenu.setToolTipText("Change ball settings");

        // file
        JMenuItem resFileItem = new JMenuItem("Restore");
        resFileItem.setMnemonic('R');
        JMenuItem exitFileItem = new JMenuItem("Exit");
        exitFileItem.setMnemonic('x');
        fileMenu.add(resFileItem);
        fileMenu.add(exitFileItem);

        // mode
        JRadioButtonMenuItem offModeItem = new JRadioButtonMenuItem("Off" ,(mode == -1) ? true : false);
        JRadioButtonMenuItem clnModeItem = new JRadioButtonMenuItem("Clean", (mode == 0) ? true : false);
        JRadioButtonMenuItem locModeItem = new JRadioButtonMenuItem("Location", (mode == 1) ? true : false);
        JRadioButtonMenuItem blbModeItem = new JRadioButtonMenuItem("Blobs", (mode == 2) ? true : false);
        JRadioButtonMenuItem thrModeItem = new JRadioButtonMenuItem("Thresholding", (mode == 3) ? true : false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(offModeItem);
        modeGroup.add(clnModeItem);
        modeGroup.add(locModeItem);
        modeGroup.add(blbModeItem);
        modeGroup.add(thrModeItem);
        final JCheckBoxMenuItem stratDrawModeItem = new JCheckBoxMenuItem("Draw Strategy", drawStrategy);
        modeMenu.add(offModeItem);
        modeMenu.add(clnModeItem);
        modeMenu.add(locModeItem);
        modeMenu.add(blbModeItem);
        modeMenu.add(thrModeItem);
        modeMenu.add(stratDrawModeItem);

        // debug
        JRadioButtonMenuItem zerDebuItem = new JRadioButtonMenuItem("0", (ip.debugLevel() == 0) ? true : false);
        JRadioButtonMenuItem oneDebuItem = new JRadioButtonMenuItem("1", (ip.debugLevel() == 1) ? true : false);
        JRadioButtonMenuItem twoDebuItem = new JRadioButtonMenuItem("2", (ip.debugLevel() == 2) ? true : false);
        ButtonGroup debuGroup = new ButtonGroup();
        debuGroup.add(zerDebuItem);
        debuGroup.add(oneDebuItem);
        debuGroup.add(twoDebuItem);
        dblvMenu.add(zerDebuItem);
        dblvMenu.add(oneDebuItem);
        dblvMenu.add(twoDebuItem);

        // thresholding
        JMenuItem maxThrItem = new JMenuItem("Set Threshold Limit");
        maxThrItem.setToolTipText("Set the maximum number of thresholded pixels for discarding a frame");
        JMenuItem resThrItem = new JMenuItem("Reset Threshold Limit");
        resThrItem.setToolTipText("Reset the maximum number of thresholded pixels");
        final JCheckBoxMenuItem onBGSubItem = new JCheckBoxMenuItem("Background Subtraction", ip.isBGSub());
        onBGSubItem.setToolTipText("Turn background subtraction on/off");
        JMenuItem exBGSubItem = new JMenuItem("Update Background Subtraction");
        exBGSubItem.setToolTipText("Update the background image for subtraction");
        thrsMenu.add(maxThrItem);
        thrsMenu.add(resThrItem);
        thrsMenu.add(onBGSubItem);
        thrsMenu.add(exBGSubItem);

        // plate
        JMenuItem setPlateConnItem = new JMenuItem("Set Connectivity Value");
        JMenuItem setPlateSizeItem = new JMenuItem("Set Minimum Size");
        JMenuItem resPlateConnItem = new JMenuItem("Reset Connectivity");
        resPlateConnItem.setToolTipText("Reset the value to the default");
        JMenuItem resPlateSizeItem = new JMenuItem("Reset Minimum Size");
        resPlateSizeItem.setToolTipText("Reset the value to the default");
        platMenu.add(setPlateConnItem);
        platMenu.add(setPlateSizeItem);
        platMenu.add(resPlateConnItem);
        platMenu.add(resPlateSizeItem);
        // black dot
        JMenu bDotMenu = new JMenu("Black Dot");
        bDotMenu.setToolTipText("Change black dot settings");
        JMenuItem setBDotConnItem = new JMenuItem("Set Connectivity Value");
        JMenuItem setBDotSizeItem = new JMenuItem("Set Minimum Size");
        JMenuItem resBDotConnItem = new JMenuItem("Reset Connectivity");
        resBDotConnItem.setToolTipText("Reset the value to the default");
        JMenuItem resBDotSizeItem = new JMenuItem("Reset Minimum Size");
        resBDotSizeItem.setToolTipText("Reset the value to the default");
        bDotMenu.add(setBDotConnItem);
        bDotMenu.add(setBDotSizeItem);
        bDotMenu.add(resBDotConnItem);
        bDotMenu.add(resBDotSizeItem);
        platMenu.add(bDotMenu);
        // plate type
        JMenu plateTypeMenu = new JMenu("Plate Type");
        JRadioButtonMenuItem hybPlateTypeItem = new JRadioButtonMenuItem("Hybrid", (ip.getPlateType() == 0) ? true : false);
        hybPlateTypeItem.setToolTipText("Ben's as default and Martin's as fallback");
        JRadioButtonMenuItem benPlateTypeItem = new JRadioButtonMenuItem("Ben's", (ip.getPlateType() == 1) ? true : false);
        benPlateTypeItem.setToolTipText("Fast and moderate accuracy");
        JRadioButtonMenuItem marPlateTypeItem = new JRadioButtonMenuItem("Martin's", (ip.getPlateType() == 2) ? true : false);
        marPlateTypeItem.setToolTipText("Slow and robust accuracy");
        ButtonGroup plateTypeGroup = new ButtonGroup();
        plateTypeGroup.add(hybPlateTypeItem);
        plateTypeGroup.add(benPlateTypeItem);
        plateTypeGroup.add(marPlateTypeItem);
        plateTypeMenu.add(hybPlateTypeItem);
        plateTypeMenu.add(benPlateTypeItem);
        plateTypeMenu.add(marPlateTypeItem);
        platMenu.add(plateTypeMenu);

        // ball
        JMenuItem setBallConnItem = new JMenuItem("Set Connectivity Value");
        JMenuItem setBallSizeItem = new JMenuItem("Set Minimum Size");
        JMenuItem resBallConnItem = new JMenuItem("Reset Connectivity");
        resPlateConnItem.setToolTipText("Reset the value to the default");
        JMenuItem resBallSizeItem = new JMenuItem("Reset Minimum Size");
        resPlateSizeItem.setToolTipText("Reset the value to the default");
        ballMenu.add(setBallConnItem);
        ballMenu.add(setBallSizeItem);
        ballMenu.add(resBallConnItem);
        ballMenu.add(resBallSizeItem);

        settMenu.add(dblvMenu);
        settMenu.add(thrsMenu);
        settMenu.add(platMenu);
        settMenu.add(ballMenu);

        menuBar.add(fileMenu);
        menuBar.add(modeMenu);
        menuBar.add(settMenu);

        setJMenuBar(menuBar);

        // handles restore window size
        resFileItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSize(getPreferredSize());
            }
        });
        // handles exit menu action
        exitFileItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        // handles mode switches
        offModeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newMode = -1;
                ip.setMode(newMode);
            }
        });
        clnModeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newMode = 0;
                ip.setMode(newMode);
            }
        });
        locModeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newMode = 1;
                ip.setMode(newMode);
            }
        });
        blbModeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newMode = 2;
                ip.setMode(newMode);
            }
        });
        thrModeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newMode = 3;
                ip.setMode(newMode);
            }
        });
        stratDrawModeItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                drawStrategy = stratDrawModeItem.isSelected();
            }
        });
        // handles debug switches
        zerDebuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.setDebug(0);
                mode_ = mode;
                newMode = 0;
                ip.setMode(mode);
            }
        });
        oneDebuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.setDebug(1);
                newMode = mode_;
                ip.setMode(newMode);
            }
        });
        twoDebuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(null,
                        "This might slow everything down.\nAre you sure you you would like to continue?",
                        "Confirm Debug Change",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    ip.setDebug(2);
                    newMode = mode_;
                }
            }});
        // handles thresholding options
        maxThrItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null, 
                        "Enter the new value for the maximum number of pixels to threshold:", 
                        "Set Maximum Threshold Value",
                        JOptionPane.QUESTION_MESSAGE, 
                        null, 
                        null, 
                        ip.getThrCountMax());
                if (result != null) {
                    try {
                        ip.setThrCountMax(Integer.parseInt(result));
                    } catch (NumberFormatException ex) {
                        return;
                    }
                }
            }
        });
        resThrItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetThrCountMax();
            }
        });
        onBGSubItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ip.setBGSub(onBGSubItem.isSelected());
            }
        });
        exBGSubItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.execBGSubtraction();
            }
        });
        // handles plate changes
        setPlateConnItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null,
                        "Enter the new value for blobbing connectivity:",
                        "Set Connectivity", 
                        JOptionPane.QUESTION_MESSAGE, 
                        null,
                        new String[] {"1", "2", "3", "4", "5", "6", "7", "8"},
                        Integer.toString(ip.getConnectivity(0)));
                if (result != null) {
                    ip.setConnectivity(0, Integer.parseInt(result));
                }
            }
        });
        setPlateSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null, 
                        "Enter the new value for minimum blob size:", 
                        "Set Minimum Size",
                        JOptionPane.QUESTION_MESSAGE, 
                        null, 
                        null, 
                        ip.getMinSize(0));
                if (result != null) {
                    try {
                        ip.setMinSize(0, Integer.parseInt(result));
                    } catch (NumberFormatException ex) {
                        return;
                    }
                }
            }
        });
        resPlateConnItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetConnectivity(0);
            }
        });
        resPlateSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetMinSize(0);
            }
        });
        // handles black dot changes
        setBDotConnItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null,
                        "Enter the new value for blobbing connectivity:",
                        "Set Connectivity",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] {"1", "2", "3", "4", "5", "6", "7", "8"},
                        Integer.toString(ip.getConnectivity(1)));
                if (result != null) {
                    ip.setConnectivity(2, Integer.parseInt(result));
                }
            }
        });
        setBDotSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null,
                        "Enter the new value for minimum blob size:", 
                        "Set Minimum Size",
                        JOptionPane.QUESTION_MESSAGE, 
                        null, 
                        null, 
                        ip.getMinSize(2));
                if (result != null) {
                    try {
                        ip.setMinSize(2, Integer.parseInt(result));
                    } catch (NumberFormatException ex) {
                        return;
                    }
                }
            }
        });
        resBDotConnItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetConnectivity(2);
            }
        });
        resBDotSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetMinSize(2);
            }
        });
        // handles plate mode changes
        hybPlateTypeItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ip.setPlateType(0);
            }
        });
        benPlateTypeItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ip.setPlateType(1);
            }
        });
        marPlateTypeItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ip.setPlateType(2);
            }
        });
        // handles ball blobbing changes
        setBallConnItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null,
                        "Enter the new value for blobbing connectivity:",
                        "Set Connectivity",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] {"1", "2", "3", "4", "5", "6", "7", "8"},
                        Integer.toString(ip.getConnectivity(1)));
                if (result != null) {
                    ip.setConnectivity(1, Integer.parseInt(result));
                }
            }
        });
        setBallSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = (String) JOptionPane.showInputDialog(null,
                        "Enter the new value for minimum blob size:", 
                        "Set Minimum Size",
                        JOptionPane.QUESTION_MESSAGE, 
                        null, 
                        null, 
                        ip.getMinSize(1));
                if (result != null) {
                    try {
                        ip.setMinSize(1, Integer.parseInt(result));
                    } catch (NumberFormatException ex) {
                        return;
                    }
                }
            }
        });
        resBallConnItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetConnectivity(1);
            }
        });
        resBallSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.resetMinSize(1);
            }
        });
    }

    public void update(Raster raster, Raster debugThresh, ArrayList<ArrayList<int[]>> debugPlates, int[][] coords) {
        // this is just the current frame's background
        if (newMode >= 0) {
            mode=newMode;
        }

        count++;

        if (count > 50) {
            count=1;
            frameSum=0;
        }

        if (start != 0) {
            frameSum += System.nanoTime() - start;
        }

        // update the fps count
        fps = (int) (1000 / (frameSum / (count * 1000000)));
        start = System.nanoTime();

        // update the coordinates
        this.coords = coords;
        infop.repaint();
        
        // update strategy status
        if (controlTab != null) {
            controlTab.updateControlPanel();
        }

        // if not finished repainting the previous raster then don't carry on
        if (waiting) {
            return;
        }

        if (mode == 0 || mode == 1) {
            curFrame = raster;
        }
        if (mode == 3) {
            curDebugThresh = debugThresh;
        }
        if (mode == 2) {
            curFrame=raster;
            this.debugPlates = debugPlates;
        }

        repaint = true;
    }

    public void run() {
        while (true) {
            if (repaint && !waiting) {
                if (mode != 3) {
                    image.setData(curFrame);
                }
                waiting = true;
                imagep.draw();
            } else {
                Thread.yield();
            }
        }
    }

    class ImagePanel extends Canvas {

        private static final long serialVersionUID = 1L;

        public ImagePanel(BufferedImage img) {
            image = img;
            setPreferredSize(new Dimension(width, imgheight));
        }

        public void paintComponent(Graphics g) {
            if (mode == 0) {
                // CLEAN
            } else if (mode == 1) {
                // LOCATION
                // radii for object painting
                int ballRad = 4;
                int plateXRad = 26, plateYRad = 18;

                // do the drawing here
                Graphics2D g2d = image.createGraphics();
                AffineTransform origat = new AffineTransform(g2d.getTransform());
                AffineTransform at = new AffineTransform();
                Rectangle2D.Double rect = new Rectangle2D.Double();
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2));
                // draw ball
                if (coords[0][0] != -1) {
                    g2d.drawRect(coords[0][0] - ballRad, coords[1][0] - ballRad, ballRad * 2, ballRad * 2);
                }
                // draw blue robot
                if (coords[3][0] != -1) {
                    rect.setRect(coords[3][0] - plateXRad, coords[4][0] - plateYRad, plateXRad * 2, plateYRad * 2);
                    if (coords[5][0] != -1) {
                        at = AffineTransform.getRotateInstance(Math.toRadians(-coords[5][0]), coords[3][0], coords[4][0]);
                        g2d.drawString(Integer.toString(coords[5][0]), coords[3][0], coords[4][0]);
                    }
                    g2d.draw(at.createTransformedShape(rect));
                }
                // reset transform
                g2d.setTransform(origat);
                // draw yellow robot
                if (coords[6][0] != -1) {
                    rect.setRect(coords[6][0] - plateXRad, coords[7][0] - plateYRad, plateXRad * 2, plateYRad * 2);
                    if (coords[8][0] != -1) {
                        at = AffineTransform.getRotateInstance(Math.toRadians(-coords[8][0]), coords[6][0], coords[7][0]);
                        g2d.drawString(Integer.toString(coords[8][0]), coords[6][0], coords[7][0]);
                    }
                    g2d.draw(at.createTransformedShape(rect));
                }

                if (drawStrategy) {
                    // draw information from Strategy
                    int[] command;
                    int[] nextCommand;
                    g2d.setStroke(new BasicStroke(3));

                    if (singleton != null) {
                        commands = singleton.getCommands();
                    }

                    if (commands != null && commands.size() > 0) {
                        try {
                            if (strategy.getOurColor() == 0) {
                                g2d.setColor(Color.red);
                                g2d.drawLine(coords[3][0], coords[4][0], commands.get(0)[3], commands.get(0)[4]);
                                g2d.setColor(Color.ORANGE);
                                g2d.drawString(String.valueOf(commands.get(0)[2]), (coords[3][0] + commands.get(0)[3]) / 2, (coords[4][0] + commands.get(0)[4]) / 2 - 5);
                            } else {
                                g2d.setColor(Color.red);
                                g2d.drawLine(coords[6][0], coords[7][0], commands.get(0)[3], commands.get(0)[4]);
                                g2d.setColor(Color.ORANGE);
                                g2d.drawString(String.valueOf(commands.get(0)[2]), (coords[6][0] + commands.get(0)[3]) / 2, (coords[7][0] + commands.get(0)[4]) / 2 - 5);
                            }

                            for (int i = 0; i < commands.size() - 1; i++) {
                                command = commands.get(i);
                                nextCommand = commands.get(i + 1);

                                g2d.setColor(Color.red);
                                g2d.drawLine(command[3], command[4], nextCommand[3], nextCommand[4]);

                                //g2d.setColor(Color.WHITE);
                                //g2d.drawString(String.valueOf(i+1), command[3], (command[4] - 5));

                                g2d.setColor(Color.ORANGE);
                                g2d.drawString(String.valueOf(nextCommand[2]), (command[3] + nextCommand[3]) / 2, (command[4] + nextCommand[4]) / 2 - 5);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                            return;
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }

                g2d.dispose();
            } else if (mode == 2) {
                // BLOBS
                if (debugPlates == null) { 
                    return;
                }
                for (int i = 0; i < debugPlates.size(); i++) {
                    //System.out.println("Blob " + i + " with " +debugPlates.get(i).size() + " points ");
                    for (int j = 0; j < debugPlates.get(i).size(); j++) {
                        wraster.setPixel(debugPlates.get(i).get(j)[0], debugPlates.get(i).get(j)[1], colours.get(i % (colours.size() - 1)));
                    }
                }

                image.setData(wraster);
            } else if (mode == 3) {
                // THRESHOLDING
                if (curDebugThresh == null) {
                    return;
                }
                image.setData(curDebugThresh);
            }

            if (newMode != -1) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        }

        private void draw() {
            Graphics g = null;
            try {
                g = this.getBufferStrategy().getDrawGraphics();
                paintComponent(g);
            } finally {
                g.dispose();
            }
            this.getBufferStrategy().show();
            Toolkit.getDefaultToolkit().sync();

            repaint = false;
            waiting = false;
        }
    }

    class InfoPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public InfoPanel() {
            setPreferredSize(new Dimension(width, INFOPANEL_HEIGHT));
        }

        public void paintComponent(Graphics g) {
            g.clearRect(0, 0, getWidth(), INFOPANEL_HEIGHT);
            g.setColor(Color.black);
            g.setFont(new Font("Default", Font.BOLD, 14));
            g.drawString("Ball:", 0, 15);
            g.drawString(Integer.toString(coords[0][0]), 34, 15);
            g.drawString(Integer.toString(coords[1][0]), 64, 15);
            g.drawString("Blue:", 102, 15);
            g.drawString(Integer.toString(coords[3][0]), 140, 15);
            g.drawString(Integer.toString(coords[4][0]), 170, 15);
            g.drawString(Integer.toString(coords[5][0]), 200, 15);
            g.drawString("Yellow:", 240, 15);
            g.drawString(Integer.toString(coords[6][0]), 296, 15);
            g.drawString(Integer.toString(coords[7][0]), 326, 15);
            g.drawString(Integer.toString(coords[8][0]), 356, 15);
            g.drawString("FPS:", getWidth() - 55, 15);
            g.drawString(Integer.toString(fps), getWidth() - 20, 15);
        }
    }

    class ControlPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        final JCheckBox runningStateItem;
        private JLabel strategyLbl;

        public ControlPanel() {
            setPreferredSize(new Dimension(SIDEPANEL_WIDTH, getHeight()));
            
            // blue/yellow switches
            JRadioButton colorBlueButton = new JRadioButton("Blue");
            JRadioButton colorYellowButton = new JRadioButton("Yellow");
            ButtonGroup colorGroup = new ButtonGroup();
            colorGroup.add(colorBlueButton);
            colorGroup.add(colorYellowButton);
            if (strategy != null) {
                if (strategy.getOurColor() == Strategy.BLUE) {
                    colorBlueButton.setSelected(true);
                } else {
                    colorYellowButton.setSelected(true);
                }
            }
            
            // left/right switches
            JRadioButton sideLeftButton = new JRadioButton("Left");
            JRadioButton sideRightButton = new JRadioButton("Right");
            ButtonGroup sideGroup = new ButtonGroup();
            sideGroup.add(sideLeftButton);
            sideGroup.add(sideRightButton);
            if (strategy != null) {
                if (strategy.getOurSide() == Strategy.LEFT) {
                    sideLeftButton.setSelected(true);
                } else {
                    sideRightButton.setSelected(true);
                }
            }
            
            // gameplay switches
            normalModeButton = new JRadioButton("Normal");
            JRadioButton defPenModeButton = new JRadioButton("Defence Penalty");
            JRadioButton offPenModeButton = new JRadioButton("Offence Penalty");
            ButtonGroup modeGroup = new ButtonGroup();
            modeGroup.add(normalModeButton);
            modeGroup.add(defPenModeButton);
            modeGroup.add(offPenModeButton);
            if (strategy != null) {
                if (strategy.getGameState() != Strategy.STATE_PENALTY_DEFENCE && strategy.getGameState() != Strategy.STATE_PENALTY_OFFENCE) {
                    normalModeButton.setSelected(true);
                } else if (strategy.getGameState() == Strategy.STATE_PENALTY_DEFENCE) {
                    defPenModeButton.setSelected(true);
                } else if (strategy.getGameState() == Strategy.STATE_PENALTY_OFFENCE) {
                    offPenModeButton.setSelected(true);
                }
            }
            
            // state switch
            if (movement != null) {
                runningStateItem = new JCheckBox("Running", (movement.isRunning()) ? true : false);
            } else {
                runningStateItem = null;
            }
            
            // label for showing strategy status
            strategyLbl = new JLabel();	// TODO fix placement
            
            // colour panel
            JLabel colorLbl= new JLabel("Our Color:", JLabel.LEADING);
            JPanel colorPanel = new JPanel();
            colorPanel.add(colorLbl);
            colorPanel.add(colorBlueButton);
            colorPanel.add(colorYellowButton);
            
            // side panel
            JLabel sideLbl= new JLabel("Our Side:", JLabel.LEADING);
            JPanel sidePanel = new JPanel();
            sidePanel.add(sideLbl);
            sidePanel.add(sideLeftButton);
            sidePanel.add(sideRightButton);
            
            // gameplay mode panel
            JLabel modeLbl = new JLabel("Gameplay Mode", JLabel.LEADING);
            JPanel modePanel = new JPanel(new GridLayout(0, 1));
            modePanel.add(modeLbl);
            modePanel.add(normalModeButton);
            modePanel.add(defPenModeButton);
            modePanel.add(offPenModeButton);
            
            // add them to the frame
            add(colorPanel);
            add(sidePanel);
            add(new JSeparator());
            add(modePanel);
            add(new JSeparator());
            if (runningStateItem != null) {
                add(runningStateItem);
            }
            add(new JSeparator());
            add(strategyLbl);
            
            // add item actions
            sideLeftButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.setSideLeft();
                }
            });
            sideRightButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.setSideRight();
                }
            });
            colorBlueButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.setColorBlue();
                    if(movement!=null)movement.setRobotColor(Strategy.BLUE);
                }
            });
            colorYellowButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.setColorYellow();
                    if(movement!=null)movement.setRobotColor(Strategy.YELLOW);
                }
            });
            normalModeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.startNormalMode();
                }
            });
            defPenModeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.setState_penalty_defence();
                    movement.setInPenaltyMode();
                }
            });
            offPenModeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    strategy.setState_penalty_offence();
                    movement.setInPenaltyMode();
                }
            });
            if (runningStateItem != null) {
                runningStateItem.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (runningStateItem.isSelected()) {
                            movement.setRunning(true);
                        } else if (!runningStateItem.isSelected()) {
                            movement.setRunning(false);
                        }
                    }
                });
            }
        }
        
        private void updateControlPanel() {
            if (strategy != null) {
                // update the label
                String text = "";
                if (strategy.getGameState() == Strategy.STATE_OFFENCE) {
                    text = "Offence";
                } else if (strategy.getGameState() == Strategy.STATE_DEFENCE) {
                    text = "Defence";
                } else if (strategy.getGameState() == Strategy.STATE_PENALTY_OFFENCE) {
                    text = "Penalty Offence";
                } else if (strategy.getGameState() == Strategy.STATE_PENALTY_DEFENCE) {
                    text = "Penalty Defence";
                } else if (strategy.getGameState() == Strategy.STATE_MARKING_OPPONENT) {
                    text = "Marking Opponent";
                }
                strategyLbl.setText(text);

                // update the strategy mode
                if (strategy.getGameState() != Strategy.STATE_PENALTY_DEFENCE || strategy.getGameState() != Strategy.STATE_PENALTY_OFFENCE) {
                    normalModeButton.setSelected(true);
                }
            }
        }
    }

    class ThreshPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public ThreshPanel() {
            setPreferredSize(new Dimension(SIDEPANEL_WIDTH, getHeight()));
            initComponents();
        }

        private void initComponents() {
            // initialise all buttons here
        }
    }
}
