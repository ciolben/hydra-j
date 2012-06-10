package epfl.project.sense;

import epfl.project.common.OutOfMemory;
import epfl.project.controlinterface.console.Console;
import epfl.project.nodes.Master;
import epfl.project.nodes.ThreadManager;
import java.lang.management.MemoryMXBean;

/**
 * Prediction.java (UTF-8)
 *
 * class : provide a bunch of methods for prediction in a mapreduce context.
 * 
 * 17 mai 2012
 * @author Loic
 */
public class Prediction {   
    
    public static boolean predictionPlanned = false;
    public static Master callbackMaster = null;
    public static Console console;
    public static final Object endLock = new Object();
    public static long justStartedMemory = 0;
    public static boolean predictionMustBeUsed = false;

    public static boolean isInMapTask = true;
    
    private static int maxIndex = -1;
    private static long lastMapTime = 0L;
    private static long lastReduceTime = 0L;
    private static long lastMapReduceTime = 0L;
    private static int lastMapperNumber = 0;
    private static int lastReducerNumber = 0;
    
    private static float estKVAvg = 0f;
    private static long estMemoryThreshold = 0L;
    private static int estTotalTime = 0;
    private static float estDerivate = 0f;
    
    private static long currentMemory = 0L;
    private static long maxMemory = 0;
    private static long lastMaxMemTime = 0;
    private static int lastMapperCounter = 0;
    private static float EPSILON = 100_000f;
    
//    public static ArrayList<Float> buffer = new ArrayList<>(5000);
    /**
     * Run a piece of user defined mapreduce to determine the best configuration.
     */
    public static void warmupMapReduce(int max_percentage, int max_sec) {
        final int WTIMEOUT_MS = 1000;
        final int WARM_PERC = max_percentage;
        final int MAXC_SEC = max_sec;
        final int MAXT_MS = 10_000;
        MemoryMXBean memoryInfo;
        int counter = 0;
        int perc = 0;
        memoryInfo = MRMemory.getInstance().getMemoryInfo();
        MRStats mrStats = MRStats.getInstance();
        predictionPlanned = true;
        print("warmup start.");
        mrStats.constructOverview(callbackMaster.getInstalledConfigurator());
        
        //try to clean the memory
        print("cleaning memory...");
        Object oWait = new Object();
        int waitTimes = 0;
        synchronized (oWait) {
            do {
                System.gc();
                waitTimes++;
                try {
                    oWait.wait(100); //arbitrary number
                } catch (InterruptedException ex) {
                }
            } while (waitTimes < 25); //arbitrary number
        }
        print("clean ok");
        
        long memAfter = 0L;
        long timeBefore = System.currentTimeMillis();
        long timeAfter = 0L;
        
        console.interpretCmd("start enableprobes");
        
        synchronized (endLock) {
            try {
                long memBefore = memoryInfo.getHeapMemoryUsage().getUsed(); //more precise here
                while (counter < MAXC_SEC && (perc = mrStats.getProgression(0, 0))
                        < WARM_PERC) {
                   endLock.wait(WTIMEOUT_MS); 
                   counter++;
                   print(perc + " % (" + counter + "sec) ");
                }
                print(perc + " % (" + counter + "sec) ");
                print("compiling results.");
                
                timeAfter = System.currentTimeMillis();
                
                callbackMaster.close(false);
                endLock.wait(MAXT_MS);
                
                memAfter = memoryInfo.getHeapMemoryUsage().getUsed();
                               
                //-estimate the maxPartitionIndex
                estimateMaxIndex(false);
                //-estimate the memory consumption (linear law)
                estimateMemoryThresholdPerc(memAfter - memBefore, perc);
                //-estimated total time (linear law)
                estimateTotalTime(timeAfter - timeBefore, perc);
                
                //we can update the derivate for memory
                estDerivate = (float) estMemoryThreshold / (float) estTotalTime;
                print("ESTIMATED MEM DERIVATE : " + estDerivate);
                
                MRControl.getInstance().notifyPredictionResultsAvailable();
            } catch (InterruptedException ex) {
                print("warmup error : " + ex.getMessage());
                return;
            } finally {
                predictionPlanned = false;
            }
        }
        
        if (estMemoryThreshold > memoryInfo.getHeapMemoryUsage().getMax()) {
            predictionMustBeUsed = true;
        } else {
            predictionMustBeUsed = false;
            //we adjust the threshold
            float ratio = (float) estMemoryThreshold
                    / (float) MRMemory.getInstance().getMaxWorkerNumber();
            // + 1 because of the queue
            long newThreshold = (int) ratio * (ThreadManager.getPoolSize() + 1);
            OutOfMemory.setThreshold(memoryInfo.getHeapMemoryUsage().getMax() - newThreshold);
        }
        print("warmup end.");
    }
    
    public static void estimateMaxIndex(boolean forceDoNotUpdate) {
        float kvAvg = getKeyValuePerWorker(forceDoNotUpdate);
        estKVAvg = kvAvg;
        int maxIndex0 = Math.round(kvAvg);
        if (!forceDoNotUpdate) print("avgKV : " + maxIndex0);

        int cores = Runtime.getRuntime().availableProcessors();
        
        if (maxIndex0 <= 1 || cores <= 1) {
            maxIndex = 10; //we have no information, so it will be 10
        } else {
            maxIndex = cores * (int) (Math.log(estKVAvg) / Math.log(cores));
        }
        if (!forceDoNotUpdate) print("estimated size of the pool of partition : " + maxIndex);
    }
    
    public static void estimateMemoryThresholdPerc(long memDelta, int perc) {
        if (perc == 0) {
            //if the perc is 0, we reset the estMemory to the old one.
            estMemoryThreshold = OutOfMemory.getThreshold();
        } else {
            estMemoryThreshold = (memDelta * 100) / perc + justStartedMemory;
            print("estimated memory threshold : " + estMemoryThreshold);
        }
    }
    
    public static void estimateTotalTime(long timeDelta, int perc) {
        estTotalTime = (int) (timeDelta * 100) / perc;
        print("estimated time to execute the job : " + estTotalTime);
    }
    
    /**
     * Run a mapreduce instance based on the user defined mapreduce to estimate
     * the average number of key/value pair per worker.
     * @return 
     */
    public static float getKeyValuePerWorker(boolean forceDoNotUpdate) {
        if (!predictionPlanned && !forceDoNotUpdate) {
            predictionPlanned = true;
            console.interpretCmd("start enableprobes");
            synchronized (endLock) {
                try {
                    endLock.wait();
                } catch (InterruptedException ex) {
                    print("getKeyValuePerWorker error : " + ex.getMessage());
                    return 0f;
                } finally {
                    predictionPlanned = false;
                }
            }
        }
        long kv = (long) MRStats.getInstance().getKVProbe().getRecord((byte)0);
        int workers = MRMemory.getInstance().getMaxWorkerNumber();
        float res = workers == 0 ? 0f : (float)kv / (float)workers;
        if (!forceDoNotUpdate) print("kv : " + kv + " - workers : " + workers);
        return res; 
    }
    
    
    public static void resetPrediction() {
        resetEstimatedMaxIndex();
        predictionPlanned = false;     
        lastMapTime = 0L;
        lastReduceTime = 0L;
        lastMapReduceTime = 0L;
        lastMapperNumber = 0;
        lastReducerNumber = 0;
        maxMemory = 0L;
        estMemoryThreshold = 0L;
        estKVAvg = 0f;
        estTotalTime = 0;
        estDerivate = 0;
        predictionMustBeUsed = false;
    }
    
    private static void print(String message) {
        console.print("prediction : ".concat(message));
    }
    
    public static int getEstimatedMaxIndex() {
        return maxIndex;
    }
    
    public static void setEstimatedMaxIndex(int value) {
        maxIndex = value;
    }
    
    public static void resetEstimatedMaxIndex() {
        maxIndex = -1;
    }
  
    public static long getEstimatedMemoryThreshold() {
        return estMemoryThreshold;
    }
    
    public static float getEstimatedKVAvg() {
        return estKVAvg;
    }
    
    public static int getEstimatedTotalTime() {
        return estTotalTime;
    }

    /**
     * @return the lastMapTime
     */
    public static long getLastMapTime() {
        return lastMapTime;
    }

    /**
     * @param aLastMapTime the lastMapTime to set
     */
    public static void setLastMapTime(long aLastMapTime) {
        lastMapTime = aLastMapTime;
    }

    /**
     * @return the lastReduceTime
     */
    public static long getLastReduceTime() {
        return lastReduceTime;
    }

    /**
     * @param aLastReduceTime the lastReduceTime to set
     */
    public static void setLastReduceTime(long aLastReduceTime) {
        lastReduceTime = aLastReduceTime;
    }

    /**
     * @return the lastMapReduceTime
     */
    public static long getLastMapReduceTime() {
        return lastMapReduceTime;
    }

    /**
     * @param aLastMapReduceTime the lastMapReduceTime to set
     */
    public static void setLastMapReduceTime(long aLastMapReduceTime) {
        lastMapReduceTime = aLastMapReduceTime;
    }

    /**
     * @return the lastMapperNumber
     */
    public static int getLastMapperNumber() {
        return lastMapperNumber;
    }

    /**
     * @param aLastMapperNumber the lastMapperNumber to set
     */
    public static void setLastMapperNumber(int aLastMapperNumber) {
        lastMapperNumber = aLastMapperNumber;
    }

    /**
     * @return the lastReducerNumber
     */
    public static int getLastReducerNumber() {
        return lastReducerNumber;
    }

    /**
     * @param aLastReducerNumber the lastReducerNumber to set
     */
    public static void setLastReducerNumber(int aLastReducerNumber) {
        lastReducerNumber = aLastReducerNumber;
    }
    
    /**
     * Only retain the max value.
     * @param memory the memory usage
     */
    public static void setMaxMemoryUsage(long memory) {
        
        if (memory > maxMemory) {
            maxMemory = memory;
        }
         
        if (predictionMustBeUsed && isInMapTask) {
            if (memory > currentMemory) {
                long ctime = System.currentTimeMillis();
                long timeD = ctime - lastMaxMemTime;
                lastMaxMemTime = ctime;
                long memD = memory - currentMemory;
                float derivate = (float) memD / (float) timeD;
//                buffer.add(derivate);
                if (estDerivate <= derivate - EPSILON) {
                    OutOfMemory.write(true);
                } else {
                    OutOfMemory.write(false);
                }
            } else {
                OutOfMemory.write(false);
            }
        }
        currentMemory = memory;
    }
    
    
    /**
     * Return the max memory usage recorded.
     */
    public static long getMaxMemoryUsage() {
        return maxMemory;
    }
    
    public static void resetMaxMemoryUsage() {
        maxMemory = 0;
    }
}
