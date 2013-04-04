package Testing;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

@SuppressWarnings("serial")
public class KeyInputWindow extends javax.swing.JFrame {

    /** Creates new form KeyInputWindow */
    public KeyInputWindow() {



        initComponents();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jLabel1.setText("Custom Input - Click here");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(118, 118, 118)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(218, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    private void formKeyPressed(java.awt.event.KeyEvent evt) {                                
//        System.out.println(evt.getKeyCode());
        if (evt.getKeyChar() == 65535)
            return;
        
        switch (evt.getKeyCode())
        {
            case KeyEvent.VK_LEFT:
                sendThis = 'L';
                break;
            case KeyEvent.VK_RIGHT:
                sendThis = 'R';
                break;

            case KeyEvent.VK_UP:
                sendThis = 'f';
                break;
            case KeyEvent.VK_DOWN:
                sendThis = 'b';
                break;

            case KeyEvent.VK_PAGE_UP:
                sendThis = 'k';
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KeyInputWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                sendThis = 'u';
                break;
            case KeyEvent.VK_PAGE_DOWN:
                sendThis = 'k';
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KeyInputWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                sendThis = 'd';
                break;
            default:
                sendThis = evt.getKeyChar();
        }
        System.out.println((char) sendThis + "  " + sendThis);
    }                               

    private void formMouseClicked(java.awt.event.MouseEvent evt) {
        
    }

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {
try {
            sendThis = Integer.parseInt(JOptionPane.showInputDialog("Enter a number to send"));
            System.out.println(sendThis);
        } catch (Exception e)
        {

        }
    }

    /**
    * @param args the command line arguments
    */

    int sendThis;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                KeyInputWindow x = new KeyInputWindow();
                x.setVisible(true);
                x.gameThread();
            }
        });
    }





    public void gameThread()
    {
        Thread t = new Thread()
        {

            @Override
            public void run() {
                NXTComm nxtComm;
                try {
                    nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
                    NXTInfo info = new NXTInfo(NXTCommFactory.BLUETOOTH,"null", "00:16:53:07:76:B0");

                    nxtComm.open(info);

                    System.out.println("Connected: Please enter commands");
                    OutputStream os = nxtComm.getOutputStream();
                    InputStream is = nxtComm.getInputStream();

                    while (true)
                    {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(KeyInputWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if (sendThis == 'o')
                            continue;
                        //sendThis = System.in.read();

                        System.out.println(sendThis);
                        System.out.println((int) sendThis);

                        byte[] bytes = ByteBuffer.allocate(4).putInt(sendThis).array();

                        os.write(bytes);
                        os.flush();
                        
                        if (sendThis == 'q')
                            break;

                        sendThis = 'o';

                        
                        System.out.println((char) is.read());

                    }

                    is.close();
                    os.close();
                    nxtComm.close();
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(KeyInputWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NXTCommException ex) {
                }
            }};

        t.start();
    }

    

    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel1;
    // End of variables declaration

}
