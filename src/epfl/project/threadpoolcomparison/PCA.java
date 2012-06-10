package epfl.project.threadpoolcomparison;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of a mutli threaded PCA
 * PCA has two phase :
 * 1) compute the mean vector
 * 2) compute the covariance matrix
 * @author Nicolas
 *
 */
public class PCA {
    static int ROW = 100;
    static int COLUMN = 100;
    static String fileName = "PCA100x100";
    static double[][] matrix = new double[ROW][COLUMN];
    static double[] meanValue = new double[ROW];

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
        
        // first phase of PCA, compute the mean vector
        threadPool.prestartAllCoreThreads();
        
        long timeToSubstractStart = System.currentTimeMillis();
        
        if (args.length == 4) {
            ROW = Integer.parseInt(args[2]);
            COLUMN = Integer.parseInt(args[3]);
            matrix = new double[ROW][COLUMN];
            meanValue = new double[ROW];
            fileName = args[1];
        }
        
        SpliterPCAMean spliterMean = new SpliterPCAMean(ROW, fileName, matrix);
        //we read a file inside the SpliterPCAMean, that must not count into the running time
        long timeToSubstract = System.currentTimeMillis() - timeToSubstractStart;
        time -= timeToSubstract;
        
        Integer rowData;
		//run the thread that will compute the mean vector
        try {
			while((rowData = spliterMean.nextChunk()) != null) {
				//each thread has the same data collector (meanValue)
				//because, each thread will produce a result in a different index of the array
				threadList.put(new WorkerPCAMean(rowData, meanValue, matrix, COLUMN));
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
			writeResult("Result.txt", meanValue);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//second phace of PCA, compute the covariance matrix
		threadList.clear();
		threadPool = new ThreadPoolExecutor(
	            cores,
	            cores,
	            keepaliveidlethread,
	            TimeUnit.SECONDS,
	            threadList);
		threadPool.prestartAllCoreThreads();
		SpliterPCACovariance spliterCovariance = new SpliterPCACovariance(ROW, meanValue);
		double[] covarianceCollector = new double[ROW * ROW];
		double[] rowsData;
		//run the thread that will compute the covariance matrix
        try {
			while((rowsData = spliterCovariance.nextChunk()) != null) {
				//each thread has the same data collector
				//because, each thread will produce a result in a different index of the matrix
				threadList.put(new WorkerPCACovariance(rowsData.clone(), covarianceCollector, ROW, COLUMN, matrix));
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
			writeResult("Result.txt", covarianceCollector);
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Finish : " + (System.currentTimeMillis() - time));
	}
	
	/**
	 * write the result in "ThreadPool Result/"fileName""
	 * @param fileName
	 * @param collector
	 * @throws IOException
	 */
	public static void writeResult(String fileName, double[] collector) throws IOException {
		File f = new File("ThreadPool Result");
		f.mkdir();
		BufferedWriter bw = new BufferedWriter(new FileWriter("ThreadPool Result/"+fileName, true));

		int sizeMean = collector.length;
		for(int i = 0; i < sizeMean; i++) {
			double value = collector[i];
			if (value != 0) {
				bw.write("( " + i + " , " + collector[i] + " )\n");
			}
		}
		bw.close();
	}
}

/*
 * Spliters for the two phase of the PCA algorithm
 */

/**
 * give the row to use to the thread
 * @author Nicolas
 *
 */
class SpliterPCAMean {
    private int rowTotal;
    private boolean closed = false;
    private int numRowForMapper = 0;
    private double[][] matrix;

    public SpliterPCAMean(int rowTotal, String fileName, double[][] matrix) {
    	this.rowTotal = rowTotal;
    	this.matrix = matrix;
        readMatrix(fileName);
    }

    /**
     * return one row (index)
     * @return
     * @throws IOException
     */
    public Integer nextChunk() throws IOException {
    	//each thread will have one row
        if (closed) {
            return null;
        }

        if (numRowForMapper < rowTotal) {
            numRowForMapper++;
            return numRowForMapper - 1;
        } else {
            closed = true;
            return null;
        }

    }

    /**
     * read a matrix from a file
     * @param fileName
     */
    public void readMatrix(String fileName) {
        try {
            //added try-catch with resources (new in java 7)
            //http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
            try (BufferedReader b = new BufferedReader(new FileReader(fileName))) {
                String line = "";
                String[] array;
                int i = 0;
                int sizeArray;
                while ((line = b.readLine()) != null) {
                    array = line.split(" ");
                    sizeArray = array.length;
                    for (int j = 0; j < sizeArray; j++) {
                        matrix[i][j] = Double.valueOf(array[j]);
                    }
                    i++;
                }
                b.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * give two rows to use to the thread
 * @author Nicolas
 *
 */
class SpliterPCACovariance {
	private int rowTotal;
    private boolean closed = false;
    private int numRow1ForMapper = 0;
    private int numRow2ForMapper = 0;
    private double[] meanValue;

    public SpliterPCACovariance(int rowTotal, double[] meanValue) {
    	this.rowTotal = rowTotal;
    	this.meanValue = meanValue;
    }

    /**
     * return two row (array containing two index)
     * @return
     * @throws IOException
     */
    public double[] nextChunk() throws IOException {
    	//each thread will have 2 row
        if (closed) {
            return null;
        }

        if (numRow1ForMapper < rowTotal && numRow2ForMapper < rowTotal) {
            double[] numRow = {numRow1ForMapper,meanValue[numRow1ForMapper], numRow2ForMapper, meanValue[numRow1ForMapper]};

            numRow2ForMapper++;
            if (numRow2ForMapper == rowTotal) {
                numRow1ForMapper++;
                numRow2ForMapper = numRow1ForMapper;
            }
            return numRow;
        } else {
            closed = true;
            return null;
        }

    }

    /**
     * read the mean vector that was written in a file (first pahse of PCA)
     */
    public void readMean() {
        try {
            try (BufferedReader b = new BufferedReader(new FileReader("ThreadPool Result/Result.txt"))) {
                String line = "";
                String[] array;

                while ((line = b.readLine()) != null) {
                    array = line.split(" ");
                    int index = Integer.valueOf(array[1]);
                    meanValue[index] = Double.valueOf(array[3]);

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

// the threads for the two phase of PCA

/**
 * Thread that compute the mean vector
 * @author Nicolas
 *
 */
class WorkerPCAMean extends Thread{
	private int row;
	private double[] collector;
	private double[][] matrix;
	private int columnTotal;

	public WorkerPCAMean(int row, double[] collector, double[][] matrix, int columnTotal) {
		this.row = row;
		this.collector = collector;
		this.matrix = matrix;
		this.columnTotal = columnTotal;
	}
	
	public void run(){

        double mean = 0.0;
        for (int i = 0; i < columnTotal; i++) {
            mean += matrix[row][i];
        }
        mean = mean / columnTotal;
        collector[row] = mean;
	}
}

/**
 * Thread that compute the covariance matrix
 * @author Nicolas
 *
 */
class WorkerPCACovariance extends Thread{
	private double[] rows;
	private double[] collector;
	private int rowTotal;
	private int columnTotal;
	private double[][] matrix;

	public WorkerPCACovariance(double[] rows, double[] collector, int rowTotal,
			int columnTotal, double[][] matrix) {
		this.rows = rows;
		this.collector = collector;
		this.rowTotal = rowTotal;
		this.columnTotal = columnTotal;
		this.matrix = matrix;
	}
	
	public void run(){
        int numRow1ForMapper = (int)rows[0];
        int numRow2ForMapper = (int)rows[2];
        double mean1 = rows[1];
        double mean2 = rows[3];
        
        double cov = 0;
        for (int i = 0; i < columnTotal; i++) {
            cov += (matrix[numRow1ForMapper][i] - mean1) * (matrix[numRow2ForMapper][i] - mean2);
        }
        cov = cov / (double) (columnTotal - 1);
        
        collector[numRow1ForMapper * rowTotal + numRow2ForMapper] =  cov;
	}
}



