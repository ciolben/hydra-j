package epfl.project.threadpoolcomparison;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Inefficient implementation of a multi threaded wordcount
 * This implementation is based on the idea that all thread will use the same data collector
 * so we must use synchronization when we put and get data from the data collector
 * @author Nicolas
 *
 */

public class WordCount2 {
	public static void main(String[] args) {	
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
        
        SpliterWordCount2 spliter;
        if (args.length == 2) {
            spliter = new SpliterWordCount2(new File(args[1]));
        } else {
            spliter = new SpliterWordCount2(new File("word_100MB.txt"));
        }
        
        String stringData = "";
        BufferWordCount2 collector = new BufferWordCount2();
        
        try {
			while((stringData = spliter.nextChunk()) != null) {
				threadList.put(new WorkerWordCount2(stringData, collector));
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
        
		try {
			writeResult("Result.txt", collector.getCollector());
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Finish : " + (System.currentTimeMillis() - time));
	}
	
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

class SpliterWordCount2{

    private boolean closed = false;
    BufferedReader bufferR;

    public SpliterWordCount2(File file) {
        try {
            bufferR = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

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

class BufferWordCount2 {
	HashMap<String, Integer> collector;
	
	public BufferWordCount2(){
		collector = new HashMap<String, Integer>();
	}
	
	public synchronized void addElem(String key) {
		Integer counter = collector.get(key);
    	if (counter == null) {
    		counter = 0;
    	}
    	collector.put(key, counter + 1);
	}
	
	public synchronized HashMap<String, Integer> getCollector(){
		return collector;
	}
}

class WorkerWordCount2 extends Thread{
	private String text;
	private BufferWordCount2 collector;

	public WorkerWordCount2(String text, BufferWordCount2 collector) {
		this.text = text;
		this.collector = collector;
	}
	
	public void run(){
        if (text.length() > 0) {
            String[] words = text.split("\\W");

            for (String string : words) {
                if (!string.isEmpty()) {
                	collector.addElem(string);
                }
            }
        }
	}
}