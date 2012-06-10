package epfl.project.mapreduce;

import epfl.project.algorithm.SpliterAlgorithmReduce;
import epfl.project.common.*;
import epfl.project.faults.RedundantWorkers;
import epfl.project.faults.WorkersTimeout;
import epfl.project.nodes.Master;
import epfl.project.nodes.ThreadManager;
import epfl.project.nodes.Worker;
import epfl.project.scheduler.TaskDescription;
import epfl.project.sense.MRMemory;
import epfl.project.sense.MRStats;
import epfl.project.sense.Prediction;
import epfl.project.sense.Reporter;
import java.io.IOException;

/**
 *
 * @author Loic
 */
public final class MapTask<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> extends MapReduceTask {

    //*****shared var that can disable all tasks directly*****
    public static boolean enable = true;
    //*****shared var that can disable all tasks directly*****
    private Configurator<K, V, KR, VR> configurator;
    private TaskDescription<K, V, KR, VR> taskDescription;
    private Reporter reporter;
    private DataCollectorSet<K, V> dataCollectorSet;
    private final Object lock;
    private int taskID;
    private int roundNumber;
    private WorkersTimeout<K, V, KR, VR> workersTimeout;
    private RedundantWorkers<K, V, KR, VR> redundantWorker;
    private AbstractDataCollectorSet sourceData;
    private boolean error = false;

    public MapTask(Configurator<K, V, KR, VR> configurator,
            TaskDescription<K, V, KR, VR> taskDescription,
            Reporter reporter,
            DataCollectorSet<K, V> dataCollectorSet,
            int roundNumber,
            AbstractDataCollectorSet sourceData, final Object lock) {

        this.configurator = configurator;
        this.taskDescription = taskDescription;
        this.reporter = reporter;
        this.dataCollectorSet = dataCollectorSet;
        this.lock = lock;
        this.roundNumber = roundNumber;
        this.sourceData = sourceData;
        this.taskID = taskDescription.getId();

        this.setUncaughtExceptionHandler(this);

    }

    public void startMapTask() {
        /*
         * Probe config : 0 : counter (Integer)
         */
        int counter = 0;

        ThreadManager.preparePool();
        
        Tuple<Integer, Double> t = taskDescription.getRedundantErrorDetector();
        if (t != null) {
            redundantWorker = new RedundantWorkers<>(taskDescription,
                    dataCollectorSet);
            redundantWorker.setUncaughtExceptionHandler(this);
            redundantWorker.start();
        }

        if (redundantWorker == null) {
            workersTimeout = new WorkersTimeout<>(configurator, dataCollectorSet,
                    reporter, taskDescription);
            workersTimeout.setUncaughtExceptionHandler(this);
            workersTimeout.start();
        }

        DataWrapper datachunk;

        if (taskDescription.getSpliter() instanceof SpliterAlgorithmReduce) {
            ((SpliterAlgorithmReduce) taskDescription.getSpliter())
                    .lastReduceResult = sourceData.getPartitionForTask();
            ((SpliterAlgorithmReduce) taskDescription.getSpliter())
                    .sourceData = sourceData;
        }

        int temporaryPercentage;
        boolean hasComputedTime = false;
        long startRead = System.currentTimeMillis();
        int overheadLower = 0;
        int passCounter = 0;
        final int PASSTEST = 10; //Empirically estimated.
        
        MRMemory mrmem = MRMemory.getInstance();
        try {
            while ((datachunk = taskDescription.getSpliter().nextChunk())
                    != null && enable) {
                /*
                 * Estimate the number of pass before calling the method
                 * addProgressionPercentage so that this method is called
                 * approximately each 1 second, lowering the overhead.
                 */
                passCounter++;
                if (!hasComputedTime) {
                    if (passCounter == PASSTEST) {
                        hasComputedTime = true;
                        passCounter = 0;
                        long diff = System.currentTimeMillis() - startRead;
                        //1000 gives the half of a second => 2000 is necessary
                        int timeDelta = diff != 0 ? (int) (2000L / diff) : 100;
                        overheadLower = timeDelta * PASSTEST;
                    }
                } else if (passCounter == overheadLower) {
                    passCounter = 0;
                    /*
                     * Saving the need to call the method if it is zero, meaning
                     * that the user has probably not defined the value.
                     */
                    temporaryPercentage = datachunk.getProgressPercentage();
                    if (temporaryPercentage != 0) {
                        MRStats.getInstance().addProgressionPercentage(
                                roundNumber, taskID, temporaryPercentage);
                    }
                }

                if (!enable) {
                    break;
                }

                try {
                    AbstractDataCollector<K, V> dataCollector = taskDescription
                            .createCollector(taskDescription);
                    
                    dataCollector.setEnableProbe(configurator.isEnableProbes());
                    
                    Worker<K, V, KR, VR> worker = new Worker<>(taskDescription.getMapper(),
                            datachunk.clone(),
                            dataCollector,
                            reporter,
                            taskID,
                            configurator.isEnableProbes());

                    counter++;
                    if (configurator.isEnableProbes()) mrmem.setMapperNumber(counter);

                    if (redundantWorker == null) {
                        ThreadManager.addRunnable(worker);
                        workersTimeout.add(worker);
                    } else {
                        redundantWorker.add(worker);

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /*
                 * Check if there is enough memory available in order to keep
                 * generating new jobs
                 */
                OutOfMemory.stopAddingNewJob();//that's a lock


                }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            if (redundantWorker == null) {
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
        
        mrmem.setMapperNumber(0);
        MRMemory.getInstance().setMaxWorkerNumber(counter);
        if(error){ return;}
        reporter.print(counter
                + " Map workers did their job | interrupted : " + !enable);
        Prediction.setLastMapperNumber(counter);
    }

    public static synchronized void disable() {
        enable = false;
    }

    @Override
    public void run() {
        startMapTask();
            synchronized (lock) {
                lock.notify();
            }
        enable = true;
    }

    @Override
    public void uncaughtException(Thread th, Throwable ex) {
        error = true;
        enable = false;
      
        if (th instanceof MapTask){
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
