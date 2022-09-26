import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Color;

/** Encoder responsible for compressing rgb formats
 * this class uses BufferedImage class and primitive classes
 * like arrays to represent the images
 * 
 * Encoder process: 
 *  recieve from server -> break images into r, g, b channels -> transform(dct)
 *  -> quantize -> store as a collection (each channel) -> send to decoder
 * 
 * many helper methods are defined at the bottom of the file
 */
public class Encoder {
  int height;
  int width;
  double quantization;
  List<double[][]> redList;
  List<double[][]> greenList;
  List<double[][]> blueList;

  public Encoder(int quant) {
    this.quantization = Math.pow(2, quant);
  }

  public void receive(BufferedImage image, int mode, int latency, Decoder decoder) throws InterruptedException {
    double[][] red, green, blue;
    breakImage(image);
    for (int i = 0; i < redList.size(); i++) {
      red = redList.get(i);
      green = greenList.get(i);
      blue = blueList.get(i);
      red = encode(red);
      green = encode(green);
      blue = encode(blue);
      redList.set(i, red);
      greenList.set(i, green);
      blueList.set(i, blue);
    }

    send(decoder, redList, greenList, blueList, mode, latency);
  }
  
  /** Helper methods */

  private void breakImage(BufferedImage image) {
		List<double[][]> redList = new ArrayList<>();
		List<double[][]> greenList = new ArrayList<>();
		List<double[][]> blueList = new ArrayList<>();
		for (int i = 0; i < image.getHeight(); i = i + 8) {
			for (int j = 0; j < image.getWidth(); j = j + 8) {
				double[][] red = new double[8][8];
				double[][] blue = new double[8][8];
				double[][] green = new double[8][8];
				for (int k = 0, x = i; k < 8; x++, k++) {
					for (int l = 0, y = j; l < 8; y++, l++) {
						int rgb = image.getRGB(y, x);
						Color color = new Color(rgb);
						int r = color.getRed();
						int g = color.getGreen();
            int b = color.getBlue();
						red[k][l] = r;
						green[k][l] = g;
						blue[k][l] = b;
					}
				}
				redList.add(red);
				greenList.add(green);
				blueList.add(blue);
			}
		}
		this.redList = redList;
		this.greenList = greenList;
		this.blueList = blueList;
	}

  private double[][] transformer(double[][] f) {
    double[] c = new double[8];
    for (int i = 1; i < 8; i++) {
      c[i] = 1;
    }
    double[][] cosines = new double[8][8];
    for (int i = 0; i < 8; i++) {
      for(int j = 0; j < 8; j++){
        cosines[i][j] = Math.cos(((2 * i + 1) / (16.0)) * j * Math.PI);
      }
    }
    c[0] = 1 / Math.sqrt(2.0);
    double[][] F = new double[8][8];
    for (int u = 0; u < 8; u++) {
      for (int v = 0; v < 8; v++) {
        double sum = 0.0;
        for (int i = 0; i < 8; i++) {
          for (int j = 0; j < 8; j++) {
            double num = f[i][j] - 128;
            sum += num * cosines[i][u]
                * cosines[j][v];
          }
        }
        sum *= ((c[u] * c[v]) / 4.0);
        F[u][v] = Math.round(sum);
      }
    }
    return F;
  }
  private double[][] quantizer(double[][] block) {
    for (int i = 0; i < block.length; i++) {
      for (int j = 0; j < block[0].length; j++) {
        block[i][j] = (int) (block[i][j] / this.quantization);
      }
    }
    return block;
  }

  private double[][] encode(double[][] block) {
    block = transformer(block);
    block = quantizer(block);
    return block;
  }


  private void send(Decoder decoder, List<double[][]> red, List<double[][]> green, List<double[][]> blue, int mode, int latency) throws InterruptedException{
    decoder.recieve(red, green, blue, mode, latency);
  }


}
