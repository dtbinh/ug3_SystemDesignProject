package movement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Timer;

public class TimerKeyListener
  implements KeyListener, ActionListener {
  private final Timer timer;
  
  private boolean released = false;
  private KeyEvent releaseEvent;
  
  public TimerKeyListener() {
    /* Just a millisecond is necessary
     * to detect a final key release.
     */
    timer = new Timer(1, this);
  }
  
  public void keyPressed(KeyEvent e) {
    released = false;
    /* This key pressed event indicates
     * that the recent key release event
     * was no real, final key release,
     * so we have to stop the timer.
     */
    timer.stop();
  }
  
  public void keyReleased(KeyEvent e) {
    if (!released) {
      /* Store the current key release
       * event, as it is sent finally
       * by our timer.
       */
      releaseEvent = e;
      timer.restart();
    }
  }
  
  public void keyTyped(KeyEvent e) {
  }
  
  public void actionPerformed(ActionEvent e) {
    /* When the timer sends its action event
     * we know that no key press event has
     * followed the last key release event,
     * so we resend the recently stored
     * key release event; but now we set
     * released to true to indicate that
     * we've got the final key release.
     */
    released = true;
    timer.stop();
    keyReleased(releaseEvent);
  }

  /**
   * Do we have a real final key release?
   * @return True, if the KeyEvent obtained
   * via keyReleased() is a real final key
   * release.
   */
  protected boolean getReleased() {
    return released;
  }
}
