package epfl.project.threadpoolcomparison;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of a mutli threaded wordcount
 * @author Nicolas
 *
 */
public class WordCount {
	public static void main(String[] args) {	
		//used to compute the execution time
		long time = System.currentTimeMillis();

		final int keepaliveidlethread = 50;
		final int cores = Runtime.getRuntime().availableProcessors();
		
		BlockingQueue<Runnable> threadList;
		ThreadPoolExecutor threadPool;
		
        threadList = new LinkedBlockingQueue<>(cores);
        threadPool = new ThreadPoolExecutor(
            cores,
            cores,
            keepaliveidlethread,
            TimeUnit.SECONDS,
            threadList);
        threadPool.prestartAllCoreThreads();
        
        SpliterWordCount spliter;
        if (args.length == 2) {
            spliter = new SpliterWordCount(new File(args[1]));
        } else {
            spliter = new SpliterWordCount(new File("word_100MB.txt"));
        }
        
        String stringData = "";
        ArrayList<HashMap<String, Integer>> collectorArray = new ArrayList<HashMap<String, Integer>>();
		//run the thread that will count the word
        try {
			while((stringData = spliter.nextChunk()) != null) {
				//each thread has his own data collector
				HashMap<String, Integer> collector = new HashMap<String, Integer>();
				collectorArray.add(collector);
				threadList.put(new WorkerWordCount(stringData, collector));
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
		HashMap<String, Integer> mergeResult = merge(collectorArray);
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
	public static HashMap<String, Integer> merge(ArrayList<HashMap<String, Integer>> collectorArray){
		HashMap<String, Integer> finalCollector = new HashMap<String, Integer>();
		
		for(HashMap<String, Integer> collector : collectorArray) {
			for (String word : collector.keySet()) {
				Integer counter = finalCollector.get(word);
				if (counter == null) {
					counter = 0;
				}
				finalCollector.put(word, counter + collector.get(word));
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
	public static void writeResult(String fileName, HashMap<String, Integer> collector) throws IOException {
		File f = new File("ThreadPool Result");
		f.mkdir();
		BufferedWriter bw = new BufferedWriter(new FileWriter("ThreadPool Result/"+fileName));
		for(String word : collector.keySet()) {
			bw.write("( " + word + " , " + collector.get(word) + " )\n");
		}
		bw.close();
	}
}

/**
 * split the text in small part
 * @author Nicolas
 *
 */
class SpliterWordCount{

    private boolean closed = false;
    BufferedReader bufferR;

    public SpliterWordCount(File file) {
        try {
            bufferR = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * return the next part to compute
     * @return
     * @throws IOException
     */
    public String nextChunk() throws IOException {
        if (closed) {
            return null;
        }

        int numberLineRead = 5000;
        StringBuilder text = new StringBuilder();
        String line;
        for (int i = 0; i < numberLineRead && !closed; i++) {
            line = bufferR.readLine();
            if (line == null) {
                closed = true;
                bufferR.close();

                return text.toString();
            }
            text.append(line).append(" ");

        }

        return text.toString();
    }
}

/**
 * Thread that count the number of word (for exemple: dog : 5, house : 14, etc.)
 * @author Nicolas
 *
 */
class WorkerWordCount extends Thread{
	private String text;
	private HashMap<String, Integer> collector;

	public WorkerWordCount(String text, HashMap<String, Integer> collector) {
		this.text = text;
		this.collector = collector;
	}
	
	public void run(){

        if (text.length() > 0) {
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
        }
	}
}