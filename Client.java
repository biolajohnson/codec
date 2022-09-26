import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**  Client class for displaying images recieved from a decoder
 * it recieves an original image and a JPEG decoded image
 * it has a JFrame and 2 JLabels has instance variables
 * it is also responsible for refreshing the frame every time it recieves an image matrix
 */

public class Client {
  BufferedImage image;
  BufferedImage originalImage;
  int imageX = 0;
  int imageY = 0;
  JLabel label;
  JLabel label2;
  JFrame frame;
  int[][] imageMatrix;
  
  public Client() {
    this.image =  new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);
    this.originalImage =  new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);
    this.imageMatrix = new int[288][352];
    
  }

  public void setOriginalImage(BufferedImage oImage) {
    this.originalImage = oImage;
    showImage();
  }

  public void receive(int[][] image) {
    this.imageMatrix = image;
    setImage();
    refresh();
  }

  private void refresh() {
    frame.repaint();
  }

  public void setImage() {
    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        image.setRGB(j, i, imageMatrix[i][j]);
      }
    }
  }
    
  private void showImage() {
    frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		label = new JLabel(new ImageIcon(image));
		label2 = new JLabel(new ImageIcon(originalImage));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.ipadx = 200;
        c.ipady = 150;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
        frame.getContentPane().add(label2, c);
    
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		
		    frame.getContentPane().add(label, c);

		frame.pack();
    frame.setVisible(true);

	}
}
