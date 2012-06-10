package epfl.project.threadpoolcomparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;


/**
 * Standard implementation of a mutli threaded histogram
 * @author Nicolas
 *
 */
public class Histogram {
	public static void main(String[] args) {
		//used to compute the execution time
		long time = System.currentTimeMillis();

		final int keepaliveidlethread = 50;
		final int cores = Runtime.getRuntime().availableProcessors();

		BlockingQueue<Runnable> threadList;
		ThreadPoolExecutor threadPool;

		threadList = new LinkedBlockingQueue<>(cores);
		threadPool = new ThreadPoolExecutor(cores, cores, keepaliveidlethread,
				TimeUnit.SECONDS, threadList);
		threadPool.prestartAllCoreThreads();

                SpliterHistogram spliter;
                if (args.length == 2) {
                    spliter = new SpliterHistogram(new File(
				args[1]));
                } else {
                    spliter = new SpliterHistogram(new File(
				"small.bmp"));
                }

		byte[] colorData;
		ArrayList<int[]> collectorArray = new ArrayList<>();
		//run the thread that will count each different color (for exemple, 0:124, 1:521, ... , 798:203)
		try {
			while ((colorData = spliter.nextChunk()) != null) {
				//each thread has his own data collector
				int[] collector = new int[768];
				collectorArray.add(collector);
				threadList.put(new WorkerHistogram(colorData.clone(), collector));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//at the end, we must merge data collector of all threads
		int[] mergeResult = merge(collectorArray);
		try {
			writeResult("Result.txt", mergeResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish : " + (System.currentTimeMillis() - time));
	}

	/**
	 *  merge data collector
	 * @param collectorArray
	 * @return
	 */
	public static int[] merge(
			ArrayList<int[]> collectorArray) {
		int[] finalCollector = new int[768];

		for (int[] collector : collectorArray) {
			int size = collector.length;
			for(int i = 0; i < size; i++) {
				finalCollector[i] = finalCollector[i] + collector[i];
			}
		}

		return finalCollector;
	}

	/**
	 * write the result in "ThreadPool Result/"fileName""
	 * @param fileName
	 * @param collector
	 * @throws IOException
	 */
	public static void writeResult(String fileName,
			int[] collector) throws IOException {
		File f = new File("ThreadPool Result");
		f.mkdir();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"ThreadPool Result/" + fileName));
		int size = collector.length;
		for(int i = 0; i < size; i++) {
			if (collector[i] != 0)
				bw.write("( " + i + " , " + collector[i]
					+ " )\n");
		}
		bw.close();
	}
}

/**
 * Thread that count each different color (for exemple, 0:124, 1:521, ... , 798:203)
 * @author Nicolas
 *
 */
class WorkerHistogram extends Thread {
	private byte[] color;
	private int[] collector;

	public WorkerHistogram(byte[] color, int[] collector) {
		this.color = color;
		this.collector = collector;
	}

	public void run() {
		int size = color.length;
        int red;
        int green;
        int blue;
		for (int i = 0; i < size; i = i + 3){
            red = byteToPixel(color[i]);
            green = byteToPixel(color[i + 1]);
            blue = byteToPixel(color[i + 2]);


			collector[blue] = collector[blue] + 1; // blue
            collector[green + 256] = collector[green + 256] + 1; //green
            collector[red + 512] =  collector[red + 512] + 1; //red
		}
	}
    public static int byteToPixel(byte pixelByte) {
        return ((int) (pixelByte) + 128);
    }
}

/**
 * split the image in small part
 * @author Nicolas
 *
 */
class SpliterHistogram {

    private boolean closed = false;
    int chunksize = 249999;
    private byte[] chunk = new byte[chunksize];
    private int counter1 = 0;
    private int counter2 = 0;
    private int width;
    private int height;
    private BufferedImage image;

    public SpliterHistogram(File file) {
        try {
            image = ImageIO.read(file);
            width = image.getWidth();
            height = image.getHeight();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * return the next part to compute
     * @return
     */
    public byte[] nextChunk() {

        if (closed) {
            return null;
        }

        int k = 0;
        int rgb;
        Color color;
        int red;
        int green;
        int blue;

        for (int i = counter1; i < width; i++) {
            for (int j = counter2; j < height; j++) {
                if (k >= chunksize) {
                    counter1 = i;
                    counter2 = j;

                    return chunk;
                }

                rgb = image.getRGB(i, j);

                color = new Color(rgb);
                red = color.getRed();
                green = color.getGreen();
                blue = color.getBlue();
                chunk[k] = pixelToByte(red);
                chunk[k + 1] = pixelToByte(green);
                chunk[k + 2] = pixelToByte(blue);

                k += 3;

            }
            counter2 = 0;
        }
        closed = true;
        image = null;
        if (k > 0) {
            return Arrays.copyOfRange(chunk, 0, k);
        } else {
            return null;
        }

    }
    public byte pixelToByte(int pixel) {
        return (byte) (pixel - 128);
    }

}