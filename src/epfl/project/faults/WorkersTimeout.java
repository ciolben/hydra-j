package epfl.project.faults;

import epfl.project.common.AbstractDataCollectorSet;
import epfl.project.common.Configurator;
import epfl.project.common.OutOfMemory;
import epfl.project.nodes.ThreadManager;
import epfl.project.nodes.Worker;
import epfl.project.scheduler.TaskDescription;
import epfl.project.sense.Reporter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkersTimeout<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> extends Thread {

    private final int TIMEOUTLAW_MULTIPLIER = 50;
    private final int MIN_JOBS_DONE = 1;//how many jobs have to be finished before starting checking for timeout
    private ConcurrentLinkedQueue<Worker<K, V, KR, VR>> workerList;
    private HashMap<Integer, ArrayList<Worker<K, V, KR, VR>>> multipleWorkers = new HashMap<>();
    private ConcurrentLinkedQueue<Worker<K, V, KR, VR>> temp = new ConcurrentLinkedQueue<>();
    private boolean stop = false;
    private boolean end = false;
    private final Object lock = new Object();
    private AbstractDataCollectorSet<K, V> dataCollectorSet;
    private TaskDescription<K, V, KR, VR> taskDescription;
    private Configurator<K, V, KR, VR> configurator;
    private boolean mapper;
    private double averageRunningTime = 0;
    private long totalRunnningTime = 0;
    private int totalFinishedJobs = 0;
    private int timeoutID = 1;

    public WorkersTimeout(Configurator<K, V, KR, VR> configurator,
            AbstractDataCollectorSet<K, V> dataCollectorSet,
            Reporter reporter,
            TaskDescription<K, V, KR, VR> taskDescription) {
        this.dataCollectorSet = dataCollectorSet;
        this.taskDescription = taskDescription;
        this.configurator = configurator;

        //	this.setPriority(Thread.MIN_PRIORITY);

        mapper = true;
        workerList = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        while (!stop || !workerList.isEmpty()) {

            int pause = 200;
            if (averageRunningTime < Integer.MAX_VALUE && averageRunningTime > 0L && totalFinishedJobs > MIN_JOBS_DONE) {
                pause = (int) averageRunningTime+1;

            }
            synchronized (lock) {
                try {
                    lock.wait(pause);
                    if (ThreadManager.isStop() || OutOfMemory.madeError()){
                        System.out.println("isstop : " + ThreadManager.isStop() + " madeerror " +
                                OutOfMemory.madeError());
                    	throw new Error("can't finish doing the job, the cause is mostly to be researched in another error");
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(WorkersTimeout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


            Worker<K, V, KR, VR> headW;
            temp = new ConcurrentLinkedQueue<>();
            while ((headW = workerList.poll()) != null) {
                //worker have not start to do his job yet
                long now = System.currentTimeMillis();

                if (headW.getStartTime() == 0) {
                    temp.add(headW);

                    //worker finished to do his job	
                } else if (headW.getRunnedTime() != null) {
                    removemultipleWorkers(headW);

                    totalRunnningTime += headW.getRunnedTime();
                    totalFinishedJobs++;
                    dataCollectorSet.addCollector(headW.getCollector());

                    //worker takes to much time in order to finish his job
                } else if (totalFinishedJobs >= MIN_JOBS_DONE
                        && headW.getStartTime() > 0
                        && averageRunningTime > 0
                        && (now - (averageRunningTime * TIMEOUTLAW_MULTIPLIER)) >= headW.getStartTime()) {

                    //re-put headW in the list:
                    //add to multiple worker (reserved id for example more than RESERVED_IDS)
                    try {
                        Worker<K, V, KR, VR> newWorker = null;

                        newWorker = headW.clone(taskDescription, timeoutID);

                        
                        timeoutID++;
                        if (newWorker != null) {
                            int id = headW.getTimeoutId();
                            if (!multipleWorkers.containsKey(id)) {
                                multipleWorkers.put(id, new ArrayList<Worker<K, V, KR, VR>>());
                                multipleWorkers.get(id).add(headW);
                            }

                            multipleWorkers.get(id).add(newWorker);

                            // TODO put to head of queue if possible
                            ThreadManager.addRunnable(newWorker);
                            temp.add(newWorker);
                            temp.add(headW);//only in this case is the first time i'm cloning this worker 

                        } else {
                            headW.killworker();//i've already tried to clone 1 time and i let this worker  working but now is time to stop it
                            //therefore i don't have to add this worker again to the queue, is more probable
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {  //worker is NOT taking to much time in order to finish his job, is doing well!

                    temp.add(headW);
                }
            }
            workerList.addAll(temp);
            averageRunningTime = totalRunnningTime / (totalFinishedJobs + 0.0);
        }
//        TODO stock somewhere these stats data before end


//        dataCollectorSet.writeToDisk();
//        Scanner scanner = new Scanner(System.in);
//	    System.out.println("write:");
//    	scanner.next();
//       dataCollectorSet.writeToDisk();



        synchronized (lock) {

            end = true;
            lock.notifyAll();
        }
    }

    public boolean end() throws InterruptedException {
        stop = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        if (!end) {
            join();
        }
        return end;
    }

    public void add(Worker w) {
        workerList.add(w);
    }

    private void removemultipleWorkers(Worker<K, V, KR, VR> worker) {

        int id = worker.getTimeoutId();
        ArrayList<Worker<K, V, KR, VR>> workers = multipleWorkers.remove(id);

        if (workers == null || workers.isEmpty()) {
            return;
        }
        for (Worker<K, V, KR, VR> w : workers) {
            if (w != worker) {
                w.clearCollector();
                //dataCollectorSet.removeSameJobWorkerCollector(w.getTaskID(), w.getID());//TODO deprecated remove the method



                //TODO kill (killworker not working) kill all w and delete it
                ThreadManager.removeRunnable(w);
                w.killworker();

            }


            //cleanup 
            temp.remove(w);
            workerList.remove(w);

            if (w != worker) {
                w = null;
            }

        }
        workers.clear();
        workers = null;
    }

	public void enableStop() {
		stop = true;
		workerList.clear();
		
	}
}
