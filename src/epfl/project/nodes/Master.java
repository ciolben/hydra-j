package epfl.project.nodes;

import epfl.project.algorithm.PartitionerAlgorithm;
import epfl.project.common.*;
import epfl.project.controlinterface.console.Console;
import epfl.project.controlinterface.gui.ControlGUI;
import epfl.project.mapreduce.MapReduceTask;
import epfl.project.mapreduce.MapTask;
import epfl.project.mapreduce.ReduceTask;
import epfl.project.scheduler.TaskCategory;
import epfl.project.scheduler.TaskDescription;
import epfl.project.scheduler.TaskScheduler;
import epfl.project.sense.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * class : Master
 *
 * This is the Master node of the internal network. The Master launches the
 * Workers and manage the work progress.
 *
 */
public class Master<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> extends Thread {

    private final Configurator<K, V, KR, VR> configurator;
    public static Reporter reporter; //temporary hack for test script
    private AbstractDataCollectorSet<K, V> dataCollectorSet;
    private boolean enable = true;
    private int restartTimes;
    private static Thread.UncaughtExceptionHandler exceptionHandler;
    private boolean error = false;
	private OutOfMemory preventOutOfMemory;

    public Master(Configurator<K, V, KR, VR> configurator) {
        this.configurator = configurator; //final field

        if (Prediction.predictionMustBeUsed) {
            configurator.setEnableProbes(true);
        }
        
        if (configurator.isGUIActive()) {
            //if we use the gui, then we get back it's own console.
            reporter = new ControlGUI(this);
            MRControl.getInstance().setConsole(((ControlGUI) reporter).getGUIConsole());
        } else {
            //else, we create our own console.
            reporter = new Console(this, configurator.getInputStream(),
                    configurator.getPrintStream());
            if (configurator.isConsoleActive()) {
                ((Console) reporter).addVersionInfo(configurator.getVersionInfo());
                new Thread((Console) reporter).start();
            }
            MRControl.getInstance().setConsole((Console) reporter);
        }
        
        minit(reporter);
        restartTimes = 0;
    }

    //Used by clone method
    private Master(Configurator<K, V, KR, VR> configurator, Reporter reporter, int restartTimes) {
        this.configurator = configurator; //final field
        this.restartTimes = restartTimes;
        minit(reporter);
    }

    private void minit(final Reporter reporter) {
    	//*****************************************
        exceptionHandler = new Thread.UncaughtExceptionHandler() {
    		private boolean alreadycalled = false;
            @Override
            public void uncaughtException(Thread th, Throwable ex) {
            	ex.printStackTrace();
            	if (alreadycalled){
            		return;
            	}
            	alreadycalled = true;
                error = true;
                enable = false;
                try {
                    ThreadManager.stopPool();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                    restartTimes++;
                    final Console cs = configurator.isGUIActive()
                                    ? ((ControlGUI) reporter).getGUIConsole()
                                    : (Console) reporter;
            	if (configurator.getRestartMaster() >=  restartTimes ){
            		Runnable restart = new Runnable() {

                        @Override
                        public void run() {
                            cs.interpretCmd("stop notclone join");
                            System.err.println("Restarting for the "+ restartTimes+ " time the Master");
                            cs.interpretCmd("start");
                        }               
                };
                Thread restartT = new Thread(restart);
                restartT.setPriority(MAX_PRIORITY);
                restartT.start();
                	
            	} else {
                    
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    cs.interpretCmd("quit");
                    System.out.println("Operation Aborted after "+ restartTimes+ " failure(s), check errors trace for more informations");
            }
            }
        };
        //******************************************

        this.reporter = reporter;
        Prediction.console = configurator.isGUIActive()
                ? ((ControlGUI) reporter).getGUIConsole(): (Console) reporter;
        Prediction.callbackMaster = this;
    }

    @Override
    public void run() {
        reporter.print("Master is up.");

        MRStats.getInstance().notifyStart();

        long time = System.currentTimeMillis();
        boolean wasMap;

        TaskScheduler scheduler = configurator.getTaskScheduler();

        List<TaskDescription> tasks;
        ArrayList<MapReduceTask> activeTask = new ArrayList<>();
        /*
         * Multi map reduce, simple implementation
         */
        MapTask mapTask = null;
        ReduceTask reduceTask = null;
        /*
         * **************************************
         */
        if(error){
        	return;
        }
        while ((tasks = scheduler.nextList()) != null) {
        	if(error){
            	return;
            }
        	if(configurator.isWriteToDiskIfneededEnabled()){
                preventOutOfMemory = new OutOfMemory();
                MRMemory.getInstance().setMemoryManager(preventOutOfMemory);
                preventOutOfMemory.start();
            }
        	   
            wasMap = false;
            long roundTime = System.currentTimeMillis();
            //launch all tasks of the round
            reporter.print("round : " + scheduler.getCurrentRound());

            final Object lock = new Object();
            int counter = 0;
            for (TaskDescription task : tasks) {

                PartitionerAlgorithm partitioner = task.getPartitioner();
                if (partitioner != null) {
                    int maxIndex = Prediction.getEstimatedMaxIndex();
                    if (maxIndex > 0) {
                        partitioner.redefineMaxIndex(maxIndex);
                    } else {
                        partitioner.redefineMaxIndex(32); //magic number
                    }
                }
                if (error) {
                    return;
                }

                if (task.getCategory() == TaskCategory.MAP) {
                    //simple map reduce
                    Prediction.isInMapTask = true;
                    wasMap = true;
                    //------------------
                    counter++;
                    DataCollectorSet collectorset = new DataCollectorSet(task);
                    mapTask = new MapTask(
                            configurator,
                            task,
                            reporter,
                            collectorset,
                            task.getId(),
                            dataCollectorSet,
                            lock);

                    if (configurator.isWriteToDiskIfneededEnabled()) {
                        preventOutOfMemory.setSourceCollectorSet(dataCollectorSet);
                        preventOutOfMemory.setDestinationCollectorSet(collectorset);
                    }

                    dataCollectorSet = collectorset;

                    activeTask.add(mapTask);
                    mapTask.start();
                } else {
                    //simple map reduce
                    Prediction.isInMapTask = false;
                    //-----------------
                    counter++;
                    ReduceDataCollectorSet collectorset = new ReduceDataCollectorSet(task);

                    reduceTask = new ReduceTask(
                            configurator,
                            task,
                            collectorset,
                            reporter,
                            task.getId(),
                            dataCollectorSet,//is not where it will stock data, a reducer will create it proper reduceCollectorSet inside himself
                            lock);

                    if (configurator.isWriteToDiskIfneededEnabled()) {
                        preventOutOfMemory.setSourceCollectorSet(dataCollectorSet);
                        preventOutOfMemory.setDestinationCollectorSet(collectorset);
                    }

                    dataCollectorSet = collectorset;

                    activeTask.add(reduceTask);
                    reduceTask.start();
                }
            }

            //master must wait till the end of the round before starting next one
            try {
                if (wasMap) {
                    mapTask.join();
                } else {
                    reduceTask.join();
                }
                if (error) {
                    return;
                }
            } catch (InterruptedException ex) {
            }

            //****************for extended multi mapreduce**********************
//            while (counter > 0) {
//                synchronized (lock) {
//                    //update counter if some tasks have finished during the
//                    //time outside the synchronized block, so we don't get stuck
//                    for (MapReduceTask task : (ArrayList<MapReduceTask>) activeTask.clone()) {
//                        if (task.getState() == Thread.State.TERMINATED) {
//                            counter--;
//                            activeTask.remove(task);
//                        }
//                    }
//                    if (counter <= 0) {
//                        break;
//                    }
//                    //lock-wait
//                    try {
//                        lock.wait();
//                        counter--;
//                    } catch (InterruptedException ex) {
//                        System.err.println(ex.getMessage());
//                    }
//                }
//            }
            //round ended
            roundTime = System.currentTimeMillis() - roundTime;
            reporter.print(roundTime);
            if (wasMap) {
                Prediction.setLastMapTime(roundTime);
            } else {
                Prediction.setLastReduceTime(roundTime);
            }
            //has to notify any jmx client with this infos : roundNoEnd, time
            MRStats.getInstance().notifyRoundEnded(scheduler.getCurrentRound(), roundTime);
            
            //roundTime = System.currentTimeMillis();
            activeTask.clear();

            if (Prediction.predictionPlanned) {
                synchronized (Prediction.endLock) {
                    Prediction.endLock.notify();
                }
            }
            if (error) {
                return;
            }
            if (!enable) {break;}
        }

        //end of all tasks
        long totalTime = System.currentTimeMillis() - time;
        reporter.print(totalTime);
        Prediction.setLastMapReduceTime(totalTime);
        scheduler.resetIterator();

        //has to notify any jmx client with this info : end
        MRStats.getInstance().notifyAllRoundEnded(totalTime);

        //if the gui nor the console was set active, we can quit.
        //here we know that the reporter is the console
        if (!configurator.isConsoleActive() && !configurator.isGUIActive()) {
            Console console = (Console) reporter;
            console.interpretCmd("quit");
        }
        //BeanServer.getInstance().stopServer();
    }

    /**
     * Close completely the master node.
     */
    public void close() {
        close(true);
    }

    /**
     * Close the master node.
     *
     * @param stopServer if the server must be also stopped.
     */
    public void close(boolean stopServer) {
        MapTask.disable();
        ReduceTask.disable();

        if (configurator.isWriteToDiskIfneededEnabled()){
        OutOfMemory.blokingClose();
        }
        if (stopServer) {
            BeanServer.getInstance().stopServer();
        }
    }

    @Override
    public Master<K, V, KR, VR> clone() {
        reporter.print("reseting...");
        List<TaskDescription> tasks;
        configurator.getTaskScheduler().resetIterator();
        while ((tasks = configurator.getTaskScheduler().nextList()) != null) {
            for (TaskDescription task : tasks) {
                if (task.getPartitioner() != null) {
                    task.getPartitioner().reset();
                }
                if (task.getSpliter() != null) {
                    task.getSpliter().reset();
                }
                if (task.getCombiner() != null) {
                    task.getCombiner().reset();
                }
            }
        }
        MapTask.enable = true;
        ReduceTask.enable = true;

        MRStats.getInstance().resetProgressions();
        MRMemory.getInstance().setMaxWorkerNumber(-1);

        Prediction.resetMaxMemoryUsage();
        
        reporter.print("reset done.");
        return new Master<>(configurator, reporter, restartTimes);
    }

    /**
     * This method is used by the prediction class.
     *
     * @return the configurator.
     */
    public Configurator getInstalledConfigurator() {
        return configurator;
    }

    public static Thread.UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

	public AbstractDataCollectorSet getResultDataCollectorSet() {
		return dataCollectorSet;
}
    }
