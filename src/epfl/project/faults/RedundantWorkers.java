package epfl.project.faults;

import epfl.project.common.AbstractDataCollectorSet;
import epfl.project.common.OutOfMemory;
import epfl.project.common.Tuple;
import epfl.project.nodes.ThreadManager;
import epfl.project.nodes.Worker;
import epfl.project.scheduler.TaskDescription;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedundantWorkers<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> extends Thread {

    private final int WAIT_TIME = 500;
    final double PERCENT;//percentual of the result that have to be correct in order to accept the result
    final int RUN_JOB_TIMES;
    final int MIN_WORKERS_SAME_RESULT;
    private ConcurrentLinkedQueue<ArrayList<Worker<K, V, KR, VR>>> jobsWorkersList = new ConcurrentLinkedQueue<>();
    private AbstractDataCollectorSet dataCollectorSet;
    private boolean stop = false;
    private boolean end = false;
    private final Object lock = new Object();
    private TaskDescription taskDescription;
    

    public RedundantWorkers(TaskDescription<K, V, KR, VR> taskDescription, AbstractDataCollectorSet dataCollectorSet ) {
        Tuple<Integer, Double> t = taskDescription.getRedundantErrorDetector();
        this.taskDescription = taskDescription;
    	PERCENT =  t.getValue();
        RUN_JOB_TIMES =t.getKey();
        MIN_WORKERS_SAME_RESULT = (int) Math.ceil(RUN_JOB_TIMES * PERCENT / 100);
        this.dataCollectorSet = dataCollectorSet;


        //	this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        while (!stop || !jobsWorkersList.isEmpty()) {
            if (ThreadManager.isStop() || OutOfMemory.madeError()){
            	throw new Error("con't finish doing te job, the cause is mostly to be researched in another error");
            }
            try {
                synchronized (lock) {

                    if (!stop) {//if i'm waiting the end no reason to keep this thread waiting so much 
                        lock.wait(WAIT_TIME);
                    } else {
                        lock.wait(WAIT_TIME / 5);
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (ArrayList<Worker<K, V, KR, VR>> job : jobsWorkersList) {
                //System.out.println(check(job));
                check(job);
            }


        }


        //TODO stock somewhere these stats data before end
        synchronized (lock) {
            end = true;
            lock.notifyAll();
        }
    }

    private boolean check(ArrayList<Worker<K, V, KR, VR>> jobWorkers) {
        ConcurrentLinkedQueue<Worker<K, V, KR, VR>> doneWorkers = new ConcurrentLinkedQueue<>();
        //ArrayList <Worker<K, V,KR, VR>> doneWorkers = new  ArrayList<>();

        for (Worker<K, V, KR, VR> worker : jobWorkers) {
            if (worker.getRunnedTime() != null) {
                doneWorkers.add(worker);
            }
        }

        int size = doneWorkers.size();
        if (size >= MIN_WORKERS_SAME_RESULT) {
            all:
            for (Worker<K, V, KR, VR> worker : doneWorkers) {
                int sameResult = 1;//because clearly it is equal to himself
                for (Worker<K, V, KR, VR> w : doneWorkers) {
                    if (worker != w) {
                        if (Arrays.equals(worker.getCollector().getHash(), w.getCollector().getHash())) {
                            sameResult++;
                        }
                        if (sameResult >= MIN_WORKERS_SAME_RESULT) {
                            dataCollectorSet.addCollector(worker.getCollector());
                            jobWorkers.remove(worker);
                            doneWorkers.clear();
                            break all;
                        }
                    }
                }
            }
            if (size != 1 && !doneWorkers.isEmpty()) {
                if (taskDescription.stopOnRedundantError()) {
                    throw new Error("Inconsistency Found in redundant computing");
                } else {
                    System.err.println("Inconsistency Found, will continue doing the job, but ONLY PARTIAL RESULT will be available at the end");
                }

            }
            for (Worker<K, V, KR, VR> worker : jobWorkers) {
                ThreadManager.removeRunnable(worker);
                worker.killworker();  //I can do that because i've already removed a worker from the list: jobWorkers.remove(worker);
                worker.clearCollector();//useless already done in killworker

            }
            jobWorkers.clear();
            jobsWorkersList.remove(jobWorkers);
            return true;
        }

        return false;
    }

    public void add(Worker<K, V, KR, VR> worker) {
        if (worker == null) {
            return;
        }
        ArrayList<Worker<K, V, KR, VR>> workers = null;

        if (taskDescription != null) {
            workers = worker.clone(RUN_JOB_TIMES, taskDescription);

        }
        if (workers == null) {
            return;
        }
        for (Worker<K, V, KR, VR> w : workers) {
            try {
                ThreadManager.addRunnable(w);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

        jobsWorkersList.add(workers);




    }

    public synchronized boolean end() throws InterruptedException {
        stop = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        if (!end) {
            this.join();
        }
        return end;
    }
}
