package epfl.project.nodes;

import epfl.project.common.Configurator;
import epfl.project.sense.Probe;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Loic
 */
public class ThreadManager {
    private static final int keepaliveidlethread = 50;
    private static final int AMP = 3;
    private static final int cores = Runtime.getRuntime().availableProcessors();
    private static BlockingQueue<Runnable> threadList;
    private static ThreadPoolExecutor threadPool;
    private static boolean isPoolBusy = false;
    private static Probe tmanagerProbe = updateProbe();
    private static Configurator config;
    private static ThreadFactory factory = new ThreadFactory(){
    	private Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread th, Throwable ex) {            	
            }
    	};
        public Thread newThread(Runnable arg0) {
        	Thread t = Executors.defaultThreadFactory().newThread(arg0);
			t.setUncaughtExceptionHandler(exceptionHandler);
        	return t;
        }};

    private ThreadManager() {
        /*Cannot be instantiated*/
    }
    
    public static void manage() {
        /*change the attributes depending on the need*/
    }
    
    public static void setConfigurator(Configurator c){
    	config = c;
    }
    
    public static boolean preparePool() {
        if (isPoolBusy) {
        	return false;
        }
        if(config != null && !config.isWriteToDiskIfneededEnabled()){
            threadList = new LinkedBlockingQueue<>(cores * AMP);

        } else {
        threadList = new LinkedBlockingQueue<>(1);
        }
        threadPool = new ThreadPoolExecutor(
            cores,
            cores,
            keepaliveidlethread,
            TimeUnit.SECONDS,
            threadList);
        threadPool.setThreadFactory(factory);
        threadPool.prestartAllCoreThreads();
        isPoolBusy = true;
        return true;
    }
    
    public static void addRunnable(Runnable thread) throws InterruptedException {
        threadList.put(thread);
    	if (threadPool == null){
    		throw new Error("Threadpool was shutdown too early, this can be an answer to another error");
    }
    }
    public static boolean isStop(){
    	return !isPoolBusy && threadList== null && threadPool == null;
    }

    /**
     * This method blocks until all tasks are terminated.
     * @throws InterruptedException 
     */
    public synchronized static void stopPool() throws InterruptedException {
        if (threadPool == null){
            return;
        }
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        threadPool = null;
        isPoolBusy = false;
        threadList.clear();
        threadList = null;
    }
    
    //TODO have to check if it is really useful, especially the threadpool.remove, if is still on the list it make sense do delete it if not bah
    public static void removeRunnable(Runnable thread) {
    	threadPool.remove(thread);
    	threadList.remove(thread);
    }
    
    public static long getCompletedTaskCount() {
        if (threadPool == null) return 0;
        //doesn't give a good approximation, seems to be always 0
        return threadPool.getCompletedTaskCount();
    }
    
    public static Probe getAndUpdateProbe() {
        updateProbe();
        return tmanagerProbe;
    }
    
    public static void pause(){
        if (threadPool == null) return;
    	threadPool.shutdown();
    	
    }
    public synchronized static void continueifPaused(){
    	if (isPoolBusy && (threadPool.isShutdown() || threadPool.isTerminating())){
    		if (!threadPool.isTerminated()){
    			try {
    				threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			}
                        
    		}
                threadPool = new ThreadPoolExecutor(
    			cores,
    			cores,
    			keepaliveidlethread,
    			TimeUnit.SECONDS,
    			threadList);
                threadPool.prestartAllCoreThreads();
    	}

    	
    }
    
    public static int decreasePoolSize () {
    	if (isPoolBusy && !threadPool.isShutdown() && !threadPool.isTerminating()){
    		int max = threadPool.getMaximumPoolSize();
    		if (max > 1){
    			threadPool.setCorePoolSize(max -1 );
    			threadPool.setMaximumPoolSize(max-1);
    			return max -1;
    		} else {
    			pause();
    			return 0;
    		}
    	}
    	return -1;
    }
    
    public static int increasePoolSize () {
    	if (isPoolBusy){
    		if (threadPool.isShutdown() || !threadPool.isTerminating()){
    			continueifPaused();
    			return threadPool.getMaximumPoolSize();
    		} else {
    			int max = threadPool.getMaximumPoolSize();
        		if (max < cores){
        			threadPool.setCorePoolSize(max + 1 );
        			threadPool.setMaximumPoolSize(max + 1);
        			return max  + 1;
        		} else {
        			pause();
        			return 0;
        		}
    		}
    	}
    	return -1;
    }
    
    private static Probe updateProbe() {
        if (tmanagerProbe == null) tmanagerProbe = new Probe((byte)3);
        if (threadPool == null) return tmanagerProbe;
        /* 0 : ActiveCount
         * 1 : TaskCount
         * 2 : CompletedTaskCount
         */
        tmanagerProbe.addRecord((byte)0, threadPool.getActiveCount());
        tmanagerProbe.addRecord((byte)1, threadPool.getTaskCount());
        tmanagerProbe.addRecord((byte)2, threadPool.getCompletedTaskCount());
        return tmanagerProbe;
    }
    
    public static int getPoolSize() {
        return cores;
    }
}
