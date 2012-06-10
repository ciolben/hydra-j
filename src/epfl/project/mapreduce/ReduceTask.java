package epfl.project.mapreduce;

import epfl.project.common.*;
import epfl.project.faults.RedundantWorkers;
import epfl.project.faults.WorkersTimeout;
import epfl.project.nodes.Master;
import epfl.project.nodes.ThreadManager;
import epfl.project.nodes.Worker;
import epfl.project.scheduler.TaskDescription;
import epfl.project.sense.MRMemory;
import epfl.project.sense.Prediction;
import epfl.project.sense.Reporter;

/**
 *
 * @author Loic
 */
public final class ReduceTask<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> extends MapReduceTask {

    //*****shared var that can disable all tasks directly*****
    public static boolean enable = true;
    //*****shared var that can disable all tasks directly*****
    
    private Configurator configurator;
    private TaskDescription taskDescription;
    private Reporter reporter;
    private AbstractDataCollectorSet dataCollectorSet;
    private final Object lock;
    private int taskID;
    private ReduceDataCollectorSet<KR, VR> reduceDataCollectorSet;

    private WorkersTimeout<K, V, KR, VR> workersTimeout;
    private RedundantWorkers<K, V, KR, VR> redundantWorker;
    private boolean error = false;
    
    public ReduceTask(Configurator<K, V, KR, VR> configurator,
            TaskDescription taskDescription,
            ReduceDataCollectorSet<KR, VR> reduceDataCollectorSet,
            Reporter reporter,
            int taskID,
            AbstractDataCollectorSet<K, V> dataCollectorSet,
            final Object lock) {

        this.configurator = configurator;
        this.reporter = reporter;
        this.taskDescription = taskDescription;
        this.taskID = taskID;
        this.lock = lock;
        this.dataCollectorSet = dataCollectorSet;
        this.reduceDataCollectorSet = reduceDataCollectorSet;

    	this.setUncaughtExceptionHandler(this);


    }

    private void startReduceTask() {

        /*
         * Probe config : 1 : counter (Integer)
         */
        int counter = 0;

        ThreadManager.preparePool();
        
        Tuple<Integer, Double> t = taskDescription.getRedundantErrorDetector();
        if (t != null) {
        	redundantWorker = new  RedundantWorkers<>(taskDescription, reduceDataCollectorSet);
            redundantWorker.setUncaughtExceptionHandler(this);
            redundantWorker.start();
        }

        if (redundantWorker == null) {
            workersTimeout = new WorkersTimeout<>(configurator,  (AbstractDataCollectorSet) reduceDataCollectorSet , reporter, taskDescription);
        	workersTimeout.setUncaughtExceptionHandler(this);
        	workersTimeout.start();
        }
        
        MRMemory mrmem = MRMemory.getInstance();
        for (int i = 0; i < taskDescription.getPartitioner().
                getMaxIndexPartition() && enable; i++) {
        	
//        	if(i % 3 == 0){
//        		Scanner scanner = new Scanner(System.in);
//        	
//     	    System.out.println("ready to go to next 3 worker:");
//         	scanner.next();
//        	}
            //probe.incrementRecord((byte) 1, false);
            counter++;
            if (configurator.isEnableProbes()) {
                mrmem.setReducerNumber(counter);
            }
            
            ReduceDataCollector<KR, VR> reduceDataCollector
                    = new ReduceDataCollector<>(redundantWorker != null);
            if (configurator.isEnableProbes()) {
                reduceDataCollector.setEnableProbe(true);
            } else {
                reduceDataCollector.setEnableProbe(false);
            }
            try {
            	Worker<K, V, KR, VR> worker = new Worker<>(
                            taskDescription.getReducer(),
                            reduceDataCollector,
                            dataCollectorSet,
                            taskID,
                            taskDescription.writeResultTodisk(),
                            configurator.isEnableProbes());
                
            	if(redundantWorker == null){
                   	ThreadManager.addRunnable(worker);
                	   workersTimeout.add(worker);
                } else {
                    	redundantWorker.add(worker);

                }
                
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            //check if there is enough memory available in order to kepp generating new jobs
           OutOfMemory.stopAddingNewJob();//that's a lock
        }

        try {
        	 if(redundantWorker == null){
         		workersTimeout.end();
         	} else {
         		redundantWorker.end();
         	}
                 
            if (configurator.isWriteToDiskIfneededEnabled()) {
                OutOfMemory.blokingClose();
            }
            
            ThreadManager.stopPool();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        mrmem.setReducerNumber(0);
        MRMemory.getInstance().setMaxWorkerNumber(counter);
        if(error){ return;}
        reporter.print(counter
                + " Reduce workers did their job | interrupted : " + !enable);

        if (taskDescription.writeResultTodisk()) {
        	reduceDataCollectorSet.mergeFinalResult();
        }
        Prediction.setLastReducerNumber(counter);
    }

    public static synchronized void disable() {
        enable = false;
    }

    @Override
    public void run() {
        startReduceTask();
        synchronized (lock) {
            lock.notify();
        }
        enable = true;
    }
    
    @Override
    public void uncaughtException(Thread th, Throwable ex) {
        error = true;
        enable = false;
      if (th instanceof ReduceTask){
        try {
            if (redundantWorker != null) {
            	redundantWorker.end();
            	
            } else if (workersTimeout != null){
				workersTimeout.enableStop();
            }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      }
       
        Master.getExceptionHandler().uncaughtException(th, ex);
    }
    
}
