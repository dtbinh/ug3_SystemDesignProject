package vision.old;
//package vision;
//
//import java.util.EventObject;
//import javax.swing.*;
//import javax.swing.GroupLayout.Alignment;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.event.EventListenerList;
//
///**
// *
// * @author Matt
// */
//public class ThreshPanel extends JPanel implements ChangeListener {
//
//    public static final int RGB = 0;
//    public static final int HSV = 1;
//
//    public ThreshPanel(Thresholder vision, int colorSpace) {
//        vFrame = vision;
//        cSpace = colorSpace;
//        initComponents(colorSpace);
//    }
//
//    private void initComponents(int colorSpace) {
//        listenerList = new EventListenerList();
//
//        addConnectionEvtListener(vFrame);
//
//        GroupLayout layout = new GroupLayout(this);
//        setLayout(layout);
//        if(colorSpace==RGB) {
//            redLow = new JSlider(JSlider.VERTICAL, 0, 255, 0);
//            greenLow = new JSlider(JSlider.VERTICAL, 0, 255, 0);
//            blueLow = new JSlider(JSlider.VERTICAL, 0, 255, 0);
//            redHigh = new JSlider(JSlider.VERTICAL, 0, 255, 255);
//            greenHigh = new JSlider(JSlider.VERTICAL, 0, 255, 255);
//            blueHigh = new JSlider(JSlider.VERTICAL, 0, 255, 255);
//            rHighLabel = new JLabel("255");
//            gHighLabel = new JLabel("255");
//            bHighLabel = new JLabel("255");
//            r = new JLabel("R", SwingConstants.CENTER);
//            g = new JLabel("G", SwingConstants.CENTER);
//            b = new JLabel("B", SwingConstants.CENTER);
//        }
//        else if(colorSpace==HSV) {
//            /* Converting RGB into HSV
//             * R = Hue, G = Saturation, B = Value
//             */
//            redLow = new JSlider(JSlider.VERTICAL, 0, 360, 0);
//            greenLow = new JSlider(JSlider.VERTICAL, 0, 100, 0);
//            blueLow = new JSlider(JSlider.VERTICAL, 0, 100, 0);
//            redHigh = new JSlider(JSlider.VERTICAL, 0, 360, 360);
//            greenHigh = new JSlider(JSlider.VERTICAL, 0, 100, 100);
//            blueHigh = new JSlider(JSlider.VERTICAL, 0, 100, 100);
//            rHighLabel = new JLabel("360");
//            gHighLabel = new JLabel("100");
//            bHighLabel = new JLabel("100");
//            r = new JLabel("H", SwingConstants.CENTER);
//            g = new JLabel("S", SwingConstants.CENTER);
//            b = new JLabel("V", SwingConstants.CENTER);
//        }
//        rLowLabel = new JLabel("0");
//        gLowLabel = new JLabel("0");
//        bLowLabel = new JLabel("0");
//        dataEvent = new IncomingDataEvent(this, new int[] {redLow.getValue(),
//                                                           greenLow.getValue(),
//                                                           blueLow.getValue(),
//                                                           redHigh.getValue(),
//                                                           greenHigh.getValue(),
//                                                           blueHigh.getValue(),});
//        JLabel lThres = new JLabel("Low Thres:", SwingConstants.CENTER);
//        JLabel hThres = new JLabel("HighThres:", SwingConstants.CENTER);
//        redLow.addChangeListener(this);
//        greenLow.addChangeListener(this);
//        blueLow.addChangeListener(this);
//        redHigh.addChangeListener(this);
//        greenHigh.addChangeListener(this);
//        blueHigh.addChangeListener(this);
//
//        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
//
//        hGroup.addGroup(layout.createParallelGroup().
//            addComponent(redLow).addComponent(rLowLabel)
//            .addComponent(lThres).addComponent(redHigh).addComponent(rHighLabel)
//            .addComponent(hThres).addComponent(r));
//        hGroup.addGroup(layout.createParallelGroup().
//            addComponent(greenLow).addComponent(gLowLabel).
//            addComponent(lThres).addComponent(greenHigh).addComponent(gHighLabel)
//            .addComponent(hThres).addComponent(g));
//        hGroup.addGroup(layout.createParallelGroup().
//           addComponent(blueLow).addComponent(bLowLabel)
//           .addComponent(lThres).addComponent(blueHigh).addComponent(bHighLabel)
//            .addComponent(hThres).addComponent(b));
//        layout.setHorizontalGroup(hGroup);
//
//       // Create a sequential group for the vertical axis.
//       GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
//
//   // The sequential group contains two parallel groups that align
//   // the contents along the baseline. The first parallel group contains
//   // the first label and text field, and the second parallel group contains
//   // the second label and text field. By using a sequential group
//   // the labels and text fields are positioned vertically after one another.
//       vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(r).addComponent(g).addComponent(b));
//       vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(hThres));
//       vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(rHighLabel).addComponent(gHighLabel).addComponent(bHighLabel));
//       vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(redHigh).addComponent(greenHigh).addComponent(blueHigh));
//       vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(lThres));
//       vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(rLowLabel).addComponent(gLowLabel).addComponent(bLowLabel));
//        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
//                addComponent(redLow).addComponent(greenLow).addComponent(blueLow));
//       layout.setVerticalGroup(vGroup);
//    }
//
//
//
//    private JSlider redLow, greenLow, blueLow, redHigh, greenHigh, blueHigh;
//    private EventListenerList listenerList;
//    private Thresholder vFrame;
//    private IncomingDataEvent dataEvent;
//    JLabel rLowLabel, gLowLabel, bLowLabel, rHighLabel, gHighLabel, bHighLabel, r, g, b;
//    private int cSpace;
//
//    public void addConnectionEvtListener(Thresholder listener) {
//		listenerList.add(Thresholder.class, listener);
//	}
//
//    protected void sendData(IncomingDataEvent evt, int colorSpace) {
//		for(Thresholder listener : listenerList.getListeners(Thresholder.class)) {
//                    listener.updateThresholds(evt, colorSpace);
//		}
//	}
//
//    public void stateChanged(ChangeEvent e) {
//        dataEvent.setThreshold(redLow.getValue(),0);
//        dataEvent.setThreshold(greenLow.getValue(),1);
//        dataEvent.setThreshold(blueLow.getValue(),2);
//        dataEvent.setThreshold(redHigh.getValue(),3);
//        dataEvent.setThreshold(greenHigh.getValue(),4);
//        dataEvent.setThreshold(blueHigh.getValue(),5);
//        rLowLabel.setText(String.valueOf(redLow.getValue()));
//        gLowLabel.setText(String.valueOf(greenLow.getValue()));
//        bLowLabel.setText(String.valueOf(blueLow.getValue()));
//
//        rHighLabel.setText(String.valueOf(redHigh.getValue()));
//        gHighLabel.setText(String.valueOf(greenHigh.getValue()));
//        bHighLabel.setText(String.valueOf(blueHigh.getValue()));
//        sendData(dataEvent, cSpace);
//    }
//
//}
//
//
///**
// * Event that is reported when transfer is about to begin
// * @author icy
// */
//class IncomingDataEvent extends EventObject {
//	public int[] thresholds = new int[6];
//        public int redLow;
//
//	public IncomingDataEvent(Object source, int[] thresholds) {
//		super(source);
//		this.thresholds = thresholds;
//	}
//
//
//	public int[] getThresholds() {
//		return thresholds;
//	}
//
//        public void setThresholds(int[] values) {
//            thresholds = values;
//            System.out.println("updated thres.");
//        }
//
//        public void setThreshold(int value, int index) {
//            thresholds[index]=value;
//        }
//
//}
