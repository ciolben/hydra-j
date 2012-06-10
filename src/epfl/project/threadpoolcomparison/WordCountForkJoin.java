package epfl.project.threadpoolcomparison;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

//http://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
//http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/RecursiveTask.html
/**
 * WordCount using fork and join
 * 
 * @author Nicolas
 */
public class WordCountForkJoin {
	public static void main(String[] args) {
		//used to compute the execution time
		double time = System.currentTimeMillis();
		
		//all thread has his own data collector and put it in this buffer
		Buffer buffer = new Buffer();
                
                SpliterWordCountForkJoin spliter;
                if (args.length == 2) {
                    spliter = new SpliterWordCountForkJoin(
				new File(args[1]));
                } else {
                    spliter = new SpliterWordCountForkJoin(
				new File("word_100MB.txt"));
                }
		 
		WorkerWordCountForkJoin worker = new WorkerWordCountForkJoin(
				spliter.nextChunk(), spliter, buffer);
		
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime()
				.availableProcessors());
		pool.invoke(worker);
		
		// merge data collector of all thread
		HashMap<String, Integer> resultHashMap = merge(buffer.get());
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
			HashMap<String, Integer> collector) throws IOException {
		File f = new File("ForkJoin Result");
		f.mkdir();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"ForkJoin Result/" + fileName));
		for (String word : collector.keySet()) {
			bw.write("( " + word + " , " + collector.get(word) + " )\n");
		}
		bw.close();
	}

	/**
	 * merge the collector of all thread
	 * @param listCollector
	 * @return
	 */
	public static HashMap<String, Integer> merge(
			ArrayList<HashMap<String, Integer>> listCollector) {
		HashMap<String, Integer> mergeResult = new HashMap<String, Integer>();
		for (HashMap<String, Integer> collector : listCollector) {
			for (String word : collector.keySet()) {
				Integer counter = mergeResult.get(word);
				if (counter == null) {
					mergeResult.put(word, collector.get(word));
				} else {
					mergeResult.put(word, counter + collector.get(word));
				}
			}
		}

		return mergeResult;
	}
}

/**
 * Class that will count the number of word using fork/join (for exemple: dog : 5, house : 14, etc.)
 * @author Nicolas
 *
 */
@SuppressWarnings("serial")
class WorkerWordCountForkJoin extends RecursiveAction {
	private String text;
	private SpliterWordCountForkJoin spliter;
	private Buffer buffer;

	public WorkerWordCountForkJoin(String text,
			SpliterWordCountForkJoin spliter, Buffer buffer) {
		this.text = text;
		this.spliter = spliter;
		this.buffer = buffer;
	}

	protected void compute() {
		String childText = spliter.nextChunk();

		// no more text to read
		if (childText == null) {

			HashMap<String, Integer> collector = new HashMap<String, Integer>();

			if (text != null && text.length() > 0) {
				String[] words = text.split("\\W");

				for (String string : words) {
					if (!string.isEmpty()) {
						Integer counter = collector.get(string);
						if (counter == null) {
							counter = 0;
						}

						collector.put(string, counter + 1);
					}
				}
				buffer.add(collector);
			}
			return;
		}

		invokeAll(new WorkerWordCountForkJoin(childText, spliter, buffer),
				new WorkerWordCountForkJoin(text, spliter, buffer));
	}
}

/**
 * Split the text. The nextChunk method gives the part of the text that the current Thread must compute
 * @author Nicolas
 *
 */
class SpliterWordCountForkJoin {

	private boolean closed = false;
	BufferedReader bufferR;

	public SpliterWordCountForkJoin(File file) {
		try {
			bufferR = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * return the next part to compute
	 * @return
	 */
	public synchronized String nextChunk() {
		if (closed) {
			return null;
		}
		
		int numberLineRead = 5000;
		StringBuilder text = new StringBuilder();
		String line;
		try {
			for (int i = 0; i < numberLineRead; i++) {

				line = bufferR.readLine();

				if (line == null) {
					closed = true;
					bufferR.close();

					return text.toString();
				}
				text.append(line).append(" ");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text.toString();
	}
}

/**
 * contain the data collector of all thread
 * @author Nicolas
 *
 */
class Buffer {
	private ArrayList<HashMap<String, Integer>> buffer;

	public Buffer() {
		buffer = new ArrayList<HashMap<String, Integer>>();
	}

	public synchronized void add(HashMap<String, Integer> hashmap) {
		buffer.add(hashmap);
	}

	public ArrayList<HashMap<String, Integer>> get() {
		return buffer;
	}
}


//////////////////////////////////////////////////////
// other method, but very slow, too much hashmap operation
//////////////////////////////////////////////////////

// public static void main(String[] args) {
// double time = System.currentTimeMillis();
// SpliterWordCountForkJoin spliter = new SpliterWordCountForkJoin(new
// File("word_100MB.txt"));
// WorkerWordCountForkJoin worker = new
// WorkerWordCountForkJoin(spliter.nextChunk(), spliter);
// ForkJoinPool pool = new
// ForkJoinPool(Runtime.getRuntime().availableProcessors());
// pool.invoke(worker);
// try {
// writeResult("result.txt", worker.result);
// } catch (IOException e) {
// e.printStackTrace();
// }
// System.out.println("Finish : " + (System.currentTimeMillis() - time));
// }
// public static void writeResult(String fileName, HashMap<String, Integer>
// collector) throws IOException {
// File f = new File("ForkJoin Result");
// f.mkdir();
// BufferedWriter bw = new BufferedWriter(new
// FileWriter("ForkJoin Result/"+fileName));
// for(String word : collector.keySet()) {
// bw.write("( " + word + " , " + collector.get(word) + " )\n");
// }
// bw.close();
// }
// }
//
// class WorkerWordCountForkJoin extends RecursiveTask<HashMap<String,
// Integer>>{
// private int depth;
// private String text;
// private static final int MAX_DEPTH = 500;
// private SpliterWordCountForkJoin spliter;
// public HashMap<String, Integer> result;
//
// public WorkerWordCountForkJoin(String text, SpliterWordCountForkJoin spliter)
// {
// this.depth = depth;
// this.text = text;
// this.spliter = spliter;
// }
//
// protected HashMap<String, Integer> compute(){
// String childText = spliter.nextChunk();
// //no more text to read
// if (childText == null) {
// HashMap<String, Integer> collector = new HashMap<String, Integer>();
//
// if (text != null && text.length() > 0) {
// String[] words = text.split("\\W");
//
// for (String string : words) {
// if (!string.isEmpty()) {
// Integer counter = collector.get(string);
// if (counter == null) {
// counter = 0;
// }
//
// collector.put(string, counter + 1);
// }
// }
// }
// return collector;
// }
//
// WorkerWordCountForkJoin worker1 = new WorkerWordCountForkJoin(childText,
// spliter);
// worker1.fork();
// WorkerWordCountForkJoin worker2 = new WorkerWordCountForkJoin(text, spliter);
// HashMap<String, Integer> w2Result = worker2.compute();
// HashMap<String, Integer> w1Result = worker1.join();
//
// result = merge( w2Result, w1Result);
// return result;
// }
//
// private HashMap<String, Integer> merge(HashMap<String, Integer> hashmap1,
// HashMap<String, Integer> hashmap2) {
// HashMap<String, Integer> mergeResult = new HashMap<String, Integer>();
//
// for(String word : hashmap1.keySet()) {
// mergeResult.put(word, hashmap1.get(word));
// }
// for(String word : hashmap2.keySet()) {
// Integer counter = mergeResult.get(word);
// if (counter == null) {
// counter = 0;
// }
// mergeResult.put(word, hashmap2.get(word) + counter);
// }
//
// return mergeResult;
// }
//
// }
// class SpliterWordCountForkJoin{
//
// private boolean closed = false;
// BufferedReader bufferR;
//
// public SpliterWordCountForkJoin(File file) {
// try {
// bufferR = new BufferedReader(new FileReader(file));
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// }
// }
//
// public synchronized String nextChunk() {
//
// if (closed) {
// return null;
// }
//
// int numberLineRead = 5000;
// StringBuilder text = new StringBuilder();
// String line;
// try {
// for (int i = 0; i < numberLineRead; i++) {
//
// line = bufferR.readLine();
//
// if (line == null) {
// closed = true;
// bufferR.close();
//
// return text.toString();
// }
// text.append(line).append(" ");
//
// }
// } catch (IOException e) {
// e.printStackTrace();
// }
//
// return text.toString();
//
// }