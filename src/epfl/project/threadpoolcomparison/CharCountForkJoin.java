package epfl.project.threadpoolcomparison;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * CharCount using fork and join
 * @author Nicolas
 *
 */
public class CharCountForkJoin {
	public static void main(String[] args) {
		//used to compute the execution time
		double time = System.currentTimeMillis();

		//all thread has his own data collector and put it in this buffer
		BufferCharCount buffer = new BufferCharCount();
                
                SpliterCharCountForkJoin spliter;
                if (args.length == 2) {
                    spliter = new SpliterCharCountForkJoin(
				new File(args[1]));
                } else {
                    spliter = new SpliterCharCountForkJoin(
				new File("word_100MB.txt"));
                }
		
		WorkerCharCountForkJoin worker = new WorkerCharCountForkJoin(
				spliter.nextChunk(), spliter, buffer);

		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime()
				.availableProcessors());
		pool.invoke(worker);

		// merge data collector of all thread
		HashMap<Character, Integer> resultHashMap = merge(buffer.get());
		try {
			writeResult("result.txt", resultHashMap);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Finish : " + (System.currentTimeMillis() - time));
	}
	
	/**
	 * write the result in "ForkJoin Result/"fileName""
	 * @param fileName
	 * @param collector
	 * @throws IOException
	 */
	public static void writeResult(String fileName,
			HashMap<Character, Integer> collector) throws IOException {
		File f = new File("ForkJoin Result");
		f.mkdir();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"ForkJoin Result/" + fileName));
		for (char character : collector.keySet()) {
			bw.write("( " + character + " , " + collector.get(character)
					+ " )\n");
		}
		bw.close();
	}

	/**
	 * merge the collector of all thread
	 * 
	 * @param listCollector
	 * @return
	 */
	public static HashMap<Character, Integer> merge(
			ArrayList<HashMap<Character, Integer>> listCollector) {
		HashMap<Character, Integer> mergeResult = new HashMap<Character, Integer>();
		for (HashMap<Character, Integer> collector : listCollector) {
			for (char character : collector.keySet()) {
				Integer counter = mergeResult.get(character);
				if (counter == null) {
					mergeResult.put(character, collector.get(character));
				} else {
					mergeResult.put(character,
							counter + collector.get(character));
				}
			}
		}

		return mergeResult;
	}
}

/**
 * Class that will count the number of character using fork/join (for exemple: a : 5, b : 6, etc.)
 * @author Nicolas
 *
 */
@SuppressWarnings("serial")
class WorkerCharCountForkJoin extends RecursiveAction {
	private char[] text;
	private SpliterCharCountForkJoin spliter;
	private BufferCharCount buffer;

	public WorkerCharCountForkJoin(char[] text,
			SpliterCharCountForkJoin spliter, BufferCharCount buffer) {
		this.text = text.clone();
		this.spliter = spliter;
		this.buffer = buffer;
	}

	protected void compute() {
		char[] childText = spliter.nextChunk();

		// no more text to read
		if (childText == null) {
			HashMap<Character, Integer> collector = new HashMap<Character, Integer>();

			if (text != null && text.length > 0) {
				int size = text.length;

				for (int i = 0; i < size; i++) {
					char character = text[i];
					Integer counter = collector.get(character);
					if (counter == null) {
						counter = 0;
					}

					collector.put(character, counter + 1);
				}
				buffer.add(collector);
			}
			return;
		}

		invokeAll(new WorkerCharCountForkJoin(childText, spliter, buffer),
				new WorkerCharCountForkJoin(text, spliter, buffer));
	}
}

/**
 * Split the text. The nextChunk method gives the part of the text that the
 * current Thread must compute
 * 
 * @author Nicolas
 * 
 */
class SpliterCharCountForkJoin {

	private boolean closed = false;
	BufferedReader bufferR;
	char[] charRead;

	public SpliterCharCountForkJoin(File file) {
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
	 */
	public synchronized char[] nextChunk() {
		if (closed) {
			return null;
		}

		int read;
		try {
			read = bufferR.read(charRead);

			if (read == -1) {
				closed = true;
				bufferR.close();
				return null;
			}
			return Arrays.copyOfRange(charRead, 0, read);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

/**
 * contain the data collector of all thread
 * 
 * @author Nicolas
 * 
 */
class BufferCharCount {
	private ArrayList<HashMap<Character, Integer>> buffer;

	public BufferCharCount() {
		buffer = new ArrayList<HashMap<Character, Integer>>();
	}

	public synchronized void add(HashMap<Character, Integer> hashmap) {
		buffer.add(hashmap);
	}

	public ArrayList<HashMap<Character, Integer>> get() {
		return buffer;
	}
}