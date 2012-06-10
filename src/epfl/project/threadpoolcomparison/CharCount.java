package epfl.project.threadpoolcomparison;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of a mutli threaded charcount
 * 
 * @author Nicolas
 * 
 */
public class CharCount {
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
                
                SpliterCharCount spliter;
                if (args.length == 2) {
                    spliter = new SpliterCharCount(new File(
				args[1]));
                } else {
                    spliter = new SpliterCharCount(new File(
				"word_100MB.txt"));
                }

		char[] stringData;
		ArrayList<HashMap<Character, Integer>> collectorArray = new ArrayList<HashMap<Character, Integer>>();
		//run the thread that will count the character
		try {
			while ((stringData = spliter.nextChunk()) != null) {
				//each thread has his own data collector
				HashMap<Character, Integer> collector = new HashMap<Character, Integer>();
				collectorArray.add(collector);
				threadList.put(new WorkerCharCount(stringData.clone(), collector));
			}
		} catch (IOException e) {
			e.printStackTrace();
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
		HashMap<Character, Integer> mergeResult = merge(collectorArray);
		try {
			writeResult("Result.txt", mergeResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish : " + (System.currentTimeMillis() - time));
	}

	/**
	 * merge data collector
	 * @param collectorArray
	 * @return
	 */
	public static HashMap<Character, Integer> merge(
			ArrayList<HashMap<Character, Integer>> collectorArray) {
		HashMap<Character, Integer> finalCollector = new HashMap<Character, Integer>();

		for (HashMap<Character, Integer> collector : collectorArray) {
			for (Character character : collector.keySet()) {
				Integer counter = finalCollector.get(character);
				if (counter == null) {
					counter = 0;
				}
				finalCollector.put(character,
						counter + collector.get(character));
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
			HashMap<Character, Integer> collector) throws IOException {
		File f = new File("ThreadPool Result");
		f.mkdir();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"ThreadPool Result/" + fileName));
		for (Character character : collector.keySet()) {
			bw.write("( " + character + " , " + collector.get(character)
					+ " )\n");
		}
		bw.close();
	}
}

/**
 * split the text in small part
 * @author Nicolas
 *
 */
class SpliterCharCount {

	private boolean closed = false;
	BufferedReader bufferR;
	char[] charRead;

	public SpliterCharCount(File file) {
		try {
			bufferR = new BufferedReader(new FileReader(file));
			charRead = new char[250000];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the next part to compute
	 * @return
	 * @throws IOException
	 */
	public char[] nextChunk() throws IOException {

		if (closed) {
			return null;
		}

		int read;

		read = bufferR.read(charRead);

		if (read == -1) {
			closed = true;
			bufferR.close();
			return null;
		}
		return Arrays.copyOfRange(charRead, 0, read);
	}

}

/**
 * Thread that count the number of character (for exemple: a : 5, b : 6, etc.)
 * @author Nicolas
 *
 */
class WorkerCharCount extends Thread {
	private char[] text;
	private HashMap<Character, Integer> collector;

	public WorkerCharCount(char[] text, HashMap<Character, Integer> collector) {
		this.text = text;
		this.collector = collector;
	}

	public void run() {
		int size = text.length;
		for (int i = 0; i < size; i++) {
			char character = text[i];
			Integer counter = collector.get(character);
			if (counter == null) {
				counter = 0;
			}
			collector.put(character, counter + 1);
		}
	}
}