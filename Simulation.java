import java.awt.image.BufferedImage;
// 352x288
public class Simulation {
  public static void main(String[] args) throws InterruptedException {
    String imgPath = args[0];
    int quant = Integer.parseInt(args[1]);
    int mode = Integer.parseInt(args[2]);
    int latency = Integer.parseInt(args[3]);
    Client client = new Client();
    Decoder decoder =  new Decoder(quant, client);
    Encoder encoder = new Encoder(quant);
    Server server = new Server();

    server.readImage(imgPath);
    BufferedImage image = server.getImage();
    client.setOriginalImage(image);
    server.send(encoder, mode, latency, decoder);
  }
}
