package epfl.project.common;

import epfl.project.nodes.Master;
import epfl.project.nodes.ThreadManager;
import epfl.project.sense.MRMemory;
import epfl.project.sense.Prediction;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.MemoryMXBean;

public class OutOfMemory<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> extends Thread implements UncaughtExceptionHandler {

    /**
     * @return the threshold
     */
    public static long getThreshold() {
        return threshold;
    }
    private static long threshold = 0L;
    private static boolean stopNewWorker = false;
    private MRMemory mrmmemory;
    private MemoryMXBean memoryInfo;
    private static Object locker = new Object();
    private static boolean stop = false;
    private static OutOfMemory thisinstance;
    private AbstractDataCollectorSet sourceCollectorSet;
    private AbstractDataCollectorSet destinationCollectorSet;
    private static boolean startWriteEnable = false;
    private static boolean error;
    private static boolean wait = true;

    public OutOfMemory() {
        error = false;
        stop = false;
        stopNewWorker = false;
        startWriteEnable = false;
        wait = true;
        mrmmemory = MRMemory.getInstance();
        memoryInfo = mrmmemory.getMemoryInfo();
        this.setUncaughtExceptionHandler(this);
        threshold = memoryInfo.getHeapMemoryUsage().getMax();

        if (threshold > 0) {
            threshold = (threshold / 10) * 7;

            //	mrmmemory.addThresholdNotificationHandler(this, 100663296);
        } else {
            stop = true;
            System.err.println("can't detect available memory therefore can't prevent out of memory");
            return;//I can't provide any service if i don't know how much big is the amount of memory i have
        }
        thisinstance = this;
    }

    public static void setThreshold(long value) {
        threshold = value;
    }

    /**
     * start/stop writing to the disk without blocking the running task (if the
     * threshold is not reached).
     *
     * @param boolean
     */
    public static void write(boolean value) {
        startWriteEnable = value;
        if (!value) {
            wait = true;
        }
    }

    public static void blokingClose() {
        if (thisinstance != null) {
            OutOfMemory.close();
            try {
                if (thisinstance != null) {
                    thisinstance.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSourceCollectorSet(AbstractDataCollectorSet collector) {
        sourceCollectorSet = collector;
        // if it's already there do nothing if not  remove the 
        //one that's not in this new task (i can also remove everything is not that bad idea)
        //1) add datacollectors
        //2) finish remove datacollector
        //3) add new datacollector
    }

    public void setDestinationCollectorSet(AbstractDataCollectorSet collector) {
        destinationCollectorSet = collector;
        // if it's already there do nothing if not  remove the 
        //one that's not in this new task (i can also remove everything is not that bad idea)
        //1) add datacollectors
        //2) finish remove datacollector
        //3) add new datacollector
    }

    public void handleNotificationNormal() {
        //TODO stat a pool of thread depending on how it is bad the situation
    }

    //medium remove some thread from threapool and add some thread that write a partition 
    //if when finish still need more memory but not enough for high -> start again
    public void handleNotificationMedium() {
        //TODO stat a pool of thread depending on how it is bad the situation
    }

    //high stop the threadpool and write some partition based on +- number of left worker
    public void handleNotificationHigth() {
        //TODO stat a pool of thread depending on how it is bad the situation
    }

    public static void stopAddingNewJob() {
        //boolean true enter to lock unlock -> maybe i can check this also each time i add a touple
        if (stopNewWorker) {
            synchronized (locker) {
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void close() {
        synchronized (locker) {
            stop = true;
            locker.notifyAll();
        }
    }

    @Override
    public void run() {
        int counter = 100;
        boolean destinationCollectorWrite = false;
        boolean sourceCollectorWrite = false;

        while (!stop) {
            if (wait) {
                synchronized (locker) {

                    try {
                        locker.wait(400);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }

            if ((thresholdReached() && wait == true)
                    || ((threshold - threshold / 8)
                    <= memoryInfo.getHeapMemoryUsage().getUsed() && wait == false)) {
                stopNewWorker = true;
                ThreadManager.pause();
                destinationCollectorWrite = destinationCollectorSet.writeToDisk();
                if (!destinationCollectorWrite) {

                    if (sourceCollectorSet != null) {
                        sourceCollectorWrite = sourceCollectorSet.writeToDisk();


                        if (!sourceCollectorWrite) {
                            ThreadManager.continueifPaused();

                        }
                    } else {
                        ThreadManager.continueifPaused();
                    }
                }
                if ((threshold - threshold / 8) <= memoryInfo.getHeapMemoryUsage().getUsed()) {
                    counter--;
                    if (counter <= 0) {
                        counter = 100;
                        ThreadManager.continueifPaused();
                        System.gc();
                    }
                    wait = false;
                } else {
                    ThreadManager.continueifPaused();
                    wait = true;
                }
                synchronized (locker) {
                    stopNewWorker = false;
                    locker.notifyAll();
                }

            } else if (startWriteEnable) {
                wait = false;
                destinationCollectorWrite = destinationCollectorSet.writeToDisk();
                if (!destinationCollectorWrite) {
                    if (sourceCollectorSet != null) {
                        sourceCollectorWrite = sourceCollectorSet.writeToDisk();
                    }
                }

            } else {
                wait = true;
                ThreadManager.continueifPaused();

//				System.out.print("..");
            }
        }
        synchronized (locker) {
            stopNewWorker = false;
            locker.notifyAll();
        }
    }

    private boolean thresholdReached() {
        long memusage = memoryInfo.getHeapMemoryUsage().getUsed();
        Prediction.setMaxMemoryUsage(memusage);
        return (threshold <= memusage);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        error = true;
        stopNewWorker = false;
        synchronized (locker) {
            stopNewWorker = false;
            locker.notifyAll();
        }
        deleteRecursiveTaskfilesAndDirectory(new File(AbstractDataCollectorSet.getDirName()));
        try {
            ThreadManager.stopPool();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        Master.getExceptionHandler().uncaughtException(t, e);
    }

    public static boolean madeError() {
        return error;
    }

    private boolean deleteRecursiveTaskfilesAndDirectory(File path) {
        if (!path.exists()) {
            return false;
        }
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursiveTaskfilesAndDirectory(f);
            }
        }
        return ret && path.delete();
    }
}
