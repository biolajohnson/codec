import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Server responsible for reading image that the client wishes to decode. 
 * This class is responsible for reading the file and storing the information into a BufferedImage type. 
 * It passes this bufferedImage to the encoder to begin the encoding process.
 */
public class Server {
	BufferedImage image;
	List<double[][]> redList;
	List<double[][]> greenList;
	List<double[][]> blueList;

	public Server() {
	}

	public void readImage(String imgPath) {
		BufferedImage img = new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);
		int width = img.getWidth();
		int height = img.getHeight();
		try {
			int frameLength = width * height * 3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind + height * width];
					byte b = bytes[ind + height * width * 2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);	
					img.setRGB(x, y, pix);
					ind++;
				}
			}
			raf.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.image = img;
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void send(Encoder encoder, int mode, int latency, Decoder decoder) throws InterruptedException {
		encoder.receive(image, mode, latency, decoder);
	}
}

