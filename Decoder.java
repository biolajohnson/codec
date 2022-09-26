import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

/** Decoder responsible for decoding JPEG compressed image stream
 * The mode of delivery is dependent on arguments passed from the encoder
 * the available modes are
 * - baseline
 * - spectral progressive
 * - bit succession progressive
 * 
 * The decoding progress is: get block -> dequantize -> inverse transform -> send to client
 * many helper methods are defined at the bottom of this file
  */

public class Decoder {
  int height = 288;
  int width = 352;
  int quantization;
  int numsBlocks;
  int[][] image;
  Client client;
  List<double[][]> redList;
  List<double[][]> greenList;
  List<double[][]> blueList;
  List<double[][]> theReds = new ArrayList<>();
  List<double[][]> theGreens = new ArrayList<>();
  List<double[][]> theBlues = new ArrayList<>();
  List<int[][]> imageList = new ArrayList<>();
  List<int[]> spectralIndexList = new ArrayList<>();


  public Decoder(int numBits, Client client) {
    this.quantization = (int) Math.pow(2, numBits);
    this.client = client;
    this.image = new int[288][352];
    this.spectralIndexList = matrixIndices(new int[8][8]);
    initLists(theReds);
    initLists(theGreens);
    initLists(theBlues);
  }

  private void bitsProgressive(int latency) throws InterruptedException {
    double[][] red, green, blue, redy, greeny, bluey, redc, greenc, bluec;
    for (int iter = 32; iter >= 1; iter--) {
      for (int idx = 0; idx < numsBlocks; idx++) {
        red = redList.get(idx);
        green = greenList.get(idx);
        blue = blueList.get(idx);

        redy = decodeBlock(red);
        greeny = decodeBlock(green);
        bluey = decodeBlock(blue);

        redc = blockBitsShift(redy, iter);
        greenc = blockBitsShift(greeny, iter);
        bluec = blockBitsShift(bluey, iter);

       
        int xb = 352 / 8;
        int x1 = (idx % xb) * 8;
        int y1 = (idx / xb) * 8;

        int[][] block = combine(redc, greenc, bluec);
        for (int i = 0; i < 8; i++) {
          for (int j = 0; j < 8; j++) {
            image[y1 + i][x1 + j] = block[i][j];
          }
        }
      }
      send();
      Network.delay(latency);
    }


  }


  private void spectral(int latency) throws InterruptedException {
    for (int x = 0; x < 8; x++) {
      for (int y = 0; y < 8; y++) {
        double[][] redy, greeny, bluey, redx, greenx, bluex, redc, greenc, bluec;
        for (int idx = 0; idx < numsBlocks; idx++) {
          redy = redList.get(idx);
          greeny = greenList.get(idx);
          bluey = blueList.get(idx);

          redx = theReds.get(idx);
          greenx = theGreens.get(idx);
          bluex = theBlues.get(idx);

          redx[x][y] = redy[x][y];
          greenx[x][y] = greeny[x][y];
          bluex[x][y] = bluey[x][y];

          theReds.set(idx, redx);
          theGreens.set(idx, greenx);
          theBlues.set(idx, bluex);

          redc = decodeBlock(redx);
          greenc = decodeBlock(greenx);
          bluec = decodeBlock(bluex);
          int xb = 352 / 8;
          int x1 = (idx % xb) * 8;
          int y1 = (idx / xb) * 8;

          int[][] block = combine(redc, greenc, bluec);
          for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
              image[y1 + i][x1 + j] = block[i][j];
            }
          }
        }
        send();
        Network.delay(latency);
      }
    }
  }


  private void baseline(int latency) throws InterruptedException {
    double[][] red, green, blue;
    for (int idx = 0; idx < numsBlocks; idx++) {
      red = redList.get(idx);
      green = greenList.get(idx);
      blue = blueList.get(idx);
      red = decodeBlock(red);
      green = decodeBlock(green);
      blue = decodeBlock(blue);
      int xb = 352 / 8;
      int x = (idx % xb) * 8;
      int y = (idx / xb) * 8;

      int[][] block = combine(red, green, blue);
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          image[y + i][x + j] = block[i][j];
        }
      }
      send();
      Network.delay(latency);
    }
  }
  

  private void send() {
    client.receive(image);
  }

  public void recieve(List<double[][]> redList, List<double[][]> greenList, List<double[][]> blueList, int mode,
      int latency) throws InterruptedException {
    this.redList = redList;
    this.greenList = greenList;
    this.blueList = blueList;
    this.numsBlocks = redList.size();
    if (mode == 1) {
      baseline(latency);
    } else if (mode == 2) {
      spectral(latency);
    } else if (mode == 3) {
      bitsProgressive(latency);
    }
  }
  
  /** Helper methods */

  private int[][] combine(double[][] red, double[][] green, double[][] blue) {
    int[][] imageBlock = new int[8][8];
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        int r = (int) red[i][j];
        int g = (int) green[i][j];
        int b = (int) blue[i][j];
        r = Math.max(r, 0);
        r = Math.min(r, 255);
        g = Math.max(g, 0);
        g = Math.min(g, 255);
        b = Math.max(b, 0);
        b = Math.min(b, 255);
        Color color = new Color(r, g, b);
        imageBlock[i][j] = color.getRGB();
      }
    }
    return imageBlock;
  }

  private double[][] decodeBlock(double[][] block) {
    double[][] newBlock = new double[8][8];
    newBlock = dequantizer(block);
    newBlock = inverseTransformer(newBlock);
    return newBlock;
  }
  
  private double[][] dequantizer(double[][] block) {
    double[][] newBlock = new double[block.length][block.length];
    for (int i = 0; i < block.length; i++) {
      for (int j = 0; j < block.length; j++) {
        newBlock[i][j] = block[i][j] * this.quantization;
      }
    }
    return newBlock;
  }
  
  private double[][] inverseTransformer(double[][] F) {
    double[] c = new double[8];
    for (int i = 1; i < 8; i++) {
      c[i] = 1;
    }
    double[][] cosines = new double[8][8];
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        cosines[i][j] =  Math.cos(((2 * i + 1) / (16.0)) * j * Math.PI);
      }
    }
    c[0] = 1 / Math.sqrt(2.0);
    double[][] f = new double[8][8];
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        double sum = 0.0;
        for (int u = 0; u < 8; u++) {
          for (int v = 0; v < 8; v++) {
            sum += F[u][v] * ((c[u] * c[v]) / 4.0) * cosines[i][u]
                * cosines[j][v];
          }
        }
        
        f[i][j] = Math.round(sum) + 128;
      }
    }
    return f;
  }
    
  


  private void initLists(List<double[][]> list) {
    for (int k = 0; k < 1584; k++) {
      double[][] arr = new double[8][8];
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          arr[i][j] = 0;
        }
      }
      list.add(arr);
    }
  }


  private static int shift(int iter, int i) {
    int res = 0;
    int position = 32;
    while (position >= iter) {
      int mask = 1 << (position);
      res += i & mask;
      position--;
    }
    return res;
  }

  private static List<int[]> matrixIndices(int[][] matrix) {
    if (matrix == null || matrix.length == 0) {
      return null;
    }

    int m = matrix.length;
    int n = matrix[0].length;
    int[] result = new int[n * m];
    int t = 0;
    List<int[]> list = new ArrayList<>();

    for (int i = 0; i < n + m - 1; i++) {
      if (i % 2 == 1) {
        // down left
        int x = i < n ? 0 : i - n + 1;
        int y = i < n ? i : n - 1;
        while (x < m && y >= 0) {
          list.add(new int[] { x, y });
          result[t++] = matrix[x++][y--];

        }
      } else {
        // up right
        int x = i < m ? i : m - 1;
        int y = i < m ? 0 : i - m + 1;
        while (x >= 0 && y < n) {
          list.add(new int[] { x, y });
          result[t++] = matrix[x--][y++];

        }
      }
    }
    return list;
  }

  private static double[][] blockBitsShift(double[][] block, int iter) {
    double[][] newBlock = new double[block.length][block[0].length];
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        int num = (int) block[i][j];
        newBlock[i][j] = shift(iter, num);
      }
    }
    return newBlock;
  }
}
