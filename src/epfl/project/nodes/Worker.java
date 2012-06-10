package epfl.project.nodes;

import epfl.project.common.*;
import epfl.project.mapreduce.Mapper;
import epfl.project.mapreduce.Reducer;
import epfl.project.scheduler.TaskDescription;
import epfl.project.sense.MRStats;
import epfl.project.sense.Reporter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * *
 *
 * class : Worker
 *
 * This is the node that map or reduce data in the internal network. They are
 * managed by the Master node.
 *
 */
public class Worker<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> implements Runnable {

    private long startTime = 0;
    private long endTime = 0;
    private long runnedtime = 0;
    private boolean cloned = false;
    private Thread thread;
    private Mapper<K, V> mapper = null;
    private Reducer<K, V, KR, VR> reducer = null;
    private DataWrapper mappant;
    private ArrayList<Tuple<K, V>> reducant = null;
    private AbstractDataCollector<K, V> mapperCollector;
    private Reporter console;
    private int assignedTaskID;
    private ReduceDataCollector<KR, VR> reduceCollector;
    private AbstractDataCollectorSet<K, V> dataCollectorSet;
    private ReentrantLock lock = new ReentrantLock();
    private boolean writeResultTodisk;
    private int timeoutId = 0;
    private boolean activateProbes;
    private boolean killed = false;

    /**
     * *
     * Constructor : this worker will be a mapper.
     *
     * @param mapper The mapper algorithm.
     * @param data The data to map.
     * @param collector Where to put the output.
     * @param console The console to print messages.
     * @param combiner the combiner to apply on our list of intermediate key
     * value pair
     */
    public Worker(Mapper<K, V> mapper, DataWrapper data, AbstractDataCollector<K, V> collector,
            Reporter console,
            int assignedTaskID, boolean activateProbes) {
        this.activateProbes = activateProbes;
        this.mapper = mapper;
        this.mapperCollector = collector;
        this.console = console;
        mappant = data;
        this.assignedTaskID = assignedTaskID;
    }

    /**
     * *
     * Constructor : this worker will be a reducer, it will read data from hdd.
     *
     * @param reducer The mapper algorithm.
     * @param reduceCollector Where to put the output.
     * @param id identification
     */
    public Worker(Reducer<K, V, KR, VR> reducer,
            ReduceDataCollector<KR, VR> reduceCollector,
            AbstractDataCollectorSet<K, V> dataCollectorSet,
            int taskID, boolean writeResultTodisk, boolean activateProbes) {
        this.activateProbes = activateProbes;
        this.reducer = reducer;
        this.assignedTaskID = taskID;
        this.dataCollectorSet = dataCollectorSet;
        this.reduceCollector = reduceCollector;
        this.writeResultTodisk = writeResultTodisk;
    }

    //method used to clone
    private Worker(Reducer<K, V, KR, VR> reducer,
            ReduceDataCollector<KR, VR> reduceCollector,
            ArrayList<Tuple<K, V>> reducant, int taskID,
            boolean writeResultTodisk, boolean activateProbes) {
        this.activateProbes = activateProbes;
        this.reducer = reducer;
        this.reducant = reducant;
        this.assignedTaskID = taskID;
        this.reduceCollector = reduceCollector;
        this.writeResultTodisk = writeResultTodisk;
    }

    @Override
    public void run() {

        //can throw an exception (null pointer) : master has to wait
        //until all workers have finished.
        //console.print("worker : " + this.getId() + " started.", 4);
        thread = Thread.currentThread();
        startTime = System.currentTimeMillis();
        thread.setPriority(Thread.NORM_PRIORITY);

        //example if i want to force a thread problem (2 time for a certain job..)
//		if( id  == 10 || id  == 15 || id  == 25){
//			return;
//		}

        if (mapper != null) {

            mapper.map(mappant, mapperCollector);

            end();

        } else if (reducer != null) {
            lock.lock();

            if (reducant == null) {
                reducant = dataCollectorSet.getPartitionForTask();
            }

            if (reducant == null || reducant.isEmpty()) {//can be null if no more partitions are available
                lock.unlock();
                end();
                return;
            }
            lock.unlock();

            HashMap<K, ArrayList<V>> grouped = groupForReduce(reducant);
            for (K key : grouped.keySet()) {
                reducer.reduce(key, grouped.get(key), reduceCollector);
            }


            /////////
            //optional, not usefull
            //Collections.sort(listReduceTupleToWrite);
            ///////

            if (writeResultTodisk) {
                reduceCollector.writeReducerPartialResult(assignedTaskID);
            }

            grouped.clear();
            grouped = null;

            end();
        } else {
            console.print(this.hashCode() + " : fatal error.");
        }

        //console.print("worker : " + this.getId() + " stopped.", 4);
    }

    /**
     * cleaning code, destroy the hashmap of the collector
     */
    public void clearCollector() {
        killed = true;
        if (mapperCollector != null) {
            mapperCollector.clearCollector();
            mapperCollector = null;
        }
        if (reduceCollector != null) {
            reduceCollector.clearCollector();
            reduceCollector = null;
        }

    }

    /**
     * Group the Tuple with the same key together
     *
     * @param list
     * @return
     */
    private HashMap<K, ArrayList<V>> groupForReduce(ArrayList<Tuple<K, V>> list) {
        HashMap<K, ArrayList<V>> grouped = new HashMap<>();

        ArrayList<V> valueList;
        for (Tuple<K, V> tuple : list) {
            K key = tuple.getKey();
            V value = tuple.getValue();
            valueList = grouped.get(key);
            if (valueList == null) {
                valueList = new ArrayList<>();
                grouped.put(key, valueList);
            }
            valueList.add(value);

        }

        return grouped;
    }

    private synchronized void end() {
        thread.setPriority(Thread.NORM_PRIORITY);
        thread = null;
        endTime = System.currentTimeMillis();

        //***********STAT***********
        if (mapper != null) {
            if (activateProbes) {
                MRStats.getInstance().addMapTime(assignedTaskID, getRunnedTime());
                long previousNumber = (long) MRStats.getInstance().getKVProbe().getRecord((byte) 0);
//                System.out.println(previousNumber);
                MRStats.getInstance().getKVProbe().addRecord((byte) 0,
                        previousNumber + mapperCollector.getKvCounter());
            }
        } else {
            if (activateProbes) {
                MRStats.getInstance().addReduceTime(assignedTaskID, getRunnedTime());
            }
        }
        //**************************
    }

    /**
     * return the collector containing intermediate key value pairs
     *
     * @return
     */
    public AbstractDataCollector<K, V> getCollector() {
        if (mapperCollector != null) {
            return mapperCollector;
        }
        if (reduceCollector != null) {
            return (AbstractDataCollector<K, V>) reduceCollector;
        }
        return null;
    }

    public long getStartTime() {
        return startTime;
    }

//	public long getEndTime(){
//		return endTime;
//	}
    public synchronized Long getRunnedTime() {
        if (runnedtime != 0) {
            return runnedtime;
        } else if (endTime == 0) {
            return null;
        } else {
            runnedtime = endTime - startTime;
        }
        return runnedtime;
    }

    public int getTaskID() {
        return assignedTaskID;
    }

    public boolean killworker() {
        killed = true;
        clearCollector();
        mapper = null;
        reducer = null;
        mappant = null;
        reducant = null;
        mapperCollector = null;
        console = null;

        return false;

    }

    public Worker<K, V, KR, VR> clone(TaskDescription<K, V, KR, VR> taskDescription, int id) {
        if (cloned) {
            return null;
        }
        if (taskDescription != null) {
            if (timeoutId <= 0) {
                timeoutId = id;
            }
            cloned = true;
            Worker worker = null;
            if (mapper != null && taskDescription != null) {

                AbstractDataCollector<K, V> dataCollector = taskDescription.createCollector(taskDescription);
                worker = new Worker<>(mapper, mappant, dataCollector, console,
                        assignedTaskID, activateProbes);
                worker.setTimeoutId(timeoutId);

            } else if (reducer != null) {
                ReduceDataCollector<KR, VR> reCollector = new ReduceDataCollector<>(taskDescription.getRedundantErrorDetector() != null);

                lock.lock();
                if (reducant == null) {
                    reducant = dataCollectorSet.getPartitionForTask();
                }
                lock.unlock();

                worker = new Worker<>(reducer, reCollector,
                        reducant,
                        assignedTaskID, writeResultTodisk, activateProbes);
                worker.setTimeoutId(timeoutId);
            } else {

                //don't have to happen but maybe because of reducant==NULL can become possible
                return null;
            }
            return worker;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Worker<K, V, KR, VR>> clone(int times, TaskDescription<K, V, KR, VR> taskDescription) {
        ArrayList<Worker<K, V, KR, VR>> array = new ArrayList<>(times);
        array.add(this);
        if (taskDescription != null) {
            for (int i = 1; i < times; i++) {
                if (mapper != null && taskDescription != null) {
                    AbstractDataCollector<K, V> dataCollector = taskDescription.createCollector(taskDescription);
                    array.add(new Worker<K, V, KR, VR>(mapper,
                            mappant, dataCollector, console,
                            assignedTaskID, activateProbes));
                } else if (reducer != null && taskDescription != null) {
                    lock.lock();
                    if (reducant == null) {
                        reducant = dataCollectorSet.getPartitionForTask();
                    }
                    lock.unlock();

                    ReduceDataCollector<KR, VR> reCollector = new ReduceDataCollector<>(taskDescription.getRedundantErrorDetector() != null);



                    array.add(new Worker<>(reducer, reCollector, reducant,
                            assignedTaskID,
                            writeResultTodisk, activateProbes));

                } else {
                    //don't have to happen but maybe because of reducant==NULL can become possible
                    return null;
                }
            }
            return array;
        } else {
            //don't have to happen but 
            return null;
        }
    }

    public boolean wasKilled() {
        return killed;
    }

    protected void setTimeoutId(int id) {
        timeoutId = id;
    }

    public int getTimeoutId() {
        return timeoutId;
    }
}
