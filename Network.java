import java.util.concurrent.TimeUnit;

/** Network simulation
 * This class is responsible for the delays during the the display
 *  (the delay between successive decoding of blocks)
 */
public class Network {
  
  public static void delay(int time) throws InterruptedException{
    TimeUnit.MILLISECONDS.sleep(time);
  }
}
