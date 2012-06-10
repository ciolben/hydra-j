package epfl.project.sense;

import epfl.project.common.Configurator;
import epfl.project.scheduler.TaskDescription;
import epfl.project.scheduler.TaskScheduler;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * MRStats.java (UTF-8)
 *
 * 3 mai 2012
 * @author Loic
 */
public class MRStats extends NotificationBroadcasterSupport implements
        MRStatsMBean, Serializable {

    //Each index is the corresponding round number (from 0 to inf.)
    //Each entry is a hashmap for [task ID] <-> [time to complete per worker]
    private transient HashMap<Integer, ArrayList<Long>> mapperTimes;
    private transient HashMap<Integer, ArrayList<Long>> reducerTimes;
    
    private transient Probe kvProbe;
    private transient OverviewList overlist;
    private CPUInfo cpuinfo = new CPUInfo();
    
    private transient static MRStats mrStats  = null;
    
    private transient int sequenceNumber = 0;
    
    /** Private constructor */
    private MRStats() {
        mapperTimes = new HashMap<>();
        reducerTimes = new HashMap<>();
    }
    
    /**
     * An instance of this object is returned. (Singleton)
     * @return the instance of MRStats.
     */
    public static synchronized MRStats getInstance() {
        if (mrStats == null) mrStats = new MRStats();
        return mrStats;
    }
    
    /**
     * Update the progression for the task in the specified round. The overview
     * list has to be created prior calling this method.
     * @param round the round
     * @param taskid the id of the task
     * @param perc  the percentage from 0 to 100 (not checked).
     */
    public void addProgressionPercentage(int round, int taskid, int perc) {
        if (overlist == null) return;
        overlist.updateTaskProgress(round, taskid, perc);
    }
    
    /**
     * get the average time for all the map functions till now.
     * (MBean)
     * @return long
     */
    @Override
    public long getAverageMapTime() {
        if (mapperTimes.isEmpty()) return 0L;
        return computeAverage(mapperTimes);
    }

    /**
     * get the average the map functions have taken until now. (MBean)
     * @return long
     */
    @Override
    public long getAllRoundsAverageMapTime() {
        return computeAverage(mapperTimes);
    }

    /**
     * get the average time for all the reduce functions till now.
     * (MBean)
     * @return long
     */
    @Override
    public long getAverageReduceTime() {
        if (reducerTimes.isEmpty()) return 0L;
        return computeAverage(reducerTimes);
    }

    /**
     * get the average the reduce functions have taken until now. (MBean)
     * @return long
     */
    @Override
    public long getAllRoundsAverageMapTime() {
        return computeAverage(reducerTimes);
    }

    /**
     * Add a map time into the stats data base.
     * @param tid the task to consider.
     * @param time the time it takes to accomplish the map function.
     * 
     * @throws IndexOutOfBoundsException
     */
    public void addMapTime(int tid, long time) {
        if (tid > mapperTimes.size() - 1) {
            mapperTimes.put(tid, new ArrayList<Long>());
        }
        mapperTimes.get(tid).add(time);
    }
    
    /**
     * Add a reduce time into the stats data base.
     * @param tid the task to consider.
     * @param time the time it takes to accomplish the reduce function.
     * 
     * @throws IndexOutOfBoundsException
     */
    public void addReduceTime(int tid, long time) {
        if (tid > reducerTimes.size() - 1) {
            reducerTimes.put(tid, new ArrayList<Long>());
        }
        reducerTimes.get(tid).add(time);
    }
    
    /**
     * Clear all the data related to mapper/reducer times.
     */
    @Override
    public void clearTimes() {
        mapperTimes.clear();
        reducerTimes.clear();
    }
    
    /**
     * Get the probe for key value counting. <br/>
     * 0 : the number of kv (long)
     * @return the probe.
     */
    public synchronized Probe getKVProbe() {
        if (kvProbe == null) { 
            kvProbe = new Probe((byte)1);
            kvProbe.addRecord((byte)0, new Long(0));
        }
        return kvProbe;
    }
    
    @Override
    public int getCpuLoad() {
        return cpuinfo.gather();
    }
    
    /**
     * Notify all the registered client that the round <i>x</i> ended.
     * @param roundNumber the number of the round
     * @param time the time it tooks to terminates the round
     */
    public void notifyRoundEnded(int roundNumber, long time) {
        Notification notification = new Notification(
                "MRStats.roundended",
                this,
                sequenceNumber,
                System.currentTimeMillis(),
                //format :[round number]|[timie]
                //Integer.toString(roundNumber).concat(":").concat(Long.toString(time)));
                roundNumber + ":" + time);
        sequenceNumber++;
        sendNotification(notification);
        //only work for simple mapreduce
        String [] node = overlist.getNextTaskFor(this, roundNumber);
        overlist.updateTaskProgress(roundNumber, Integer.parseInt(node[2]), 100);
        overlist.getNextTaskFor(this, roundNumber);
    }
    
    /**
     * Notify all the registered client that the master has finished.
     */
    public void notifyAllRoundEnded(long time) {
        Notification notification = new Notification(
                "MRStats.allroundended",
                this,
                sequenceNumber,
                System.currentTimeMillis(),
                Long.toString(time));
        sequenceNumber++;
        sendNotification(notification);
    }
    
    public void notifyStart() {
        Notification notification = new Notification(
                "MRStats.start",
                this,
                sequenceNumber,
                System.currentTimeMillis(),
                "");
        sequenceNumber++;
        sendNotification(notification);
    }
    
    private synchronized long computeAverage(HashMap<Integer, ArrayList<Long>> hmap) {
        if (hmap.isEmpty()) return 0;
        long timeSum = 0L;
        int number = 0;
        Collection<ArrayList<Long>> lists = hmap.values();
        for (ArrayList<Long> list : lists) {
            for (Long l : list) {
                timeSum += l;
            }
            number += list.size();
        }
        return timeSum / number;
    }

    /**
     * Construct the overview data structure corresponding to the configurator.
     * @param configurator the configurator of the mapreduce job.
     */
    public void constructOverview(Configurator configurator) {
        //construct the overviewlist
        overlist = new OverviewList();
        TaskScheduler scheduler  = configurator.getTaskScheduler();
        for (Integer round : scheduler.getSortedList().keySet()) {
            overlist.addRound(scheduler.getNameForRound(round),
                    round);
            for (TaskDescription task : scheduler.getSortedList().get(round)) {
                overlist.addTask(task.getName(), task.getId());
            }
        }
    }
    
    public void resetProgressions() {
        overlist.resetProgressions();
        getKVProbe().addRecord((byte)0, 0L);
    }
    
    /**
     * Construct the overview tree of the mapreduce job first !. If it is already
     * constructed, the result will be the actual state of the mapreduce job.(MBean)
     * @return OverviewList <code>null</code> if the list has not been built yet.
     */
    @Override
    public OverviewList getOverviewList() {
        return overlist;
    }

    /**
     * get the progression percentage for a task. (MBean) <br/>
     * Overview list must have been constructed prior to calling this method.
     * @param roundID the round id
     * @param taskID the task id
     * @return int the progression percentage
     */
    @Override
    public int getProgression(Integer roundID, Integer taskID) {
        if (overlist == null) return 0;
        String [] node;
        //rewind the loop
        while (overlist.getNextTaskFor(this, roundID) != null) {}
        //search
        int p = 0;
        while ((node = overlist.getNextTaskFor(this, roundID)) != null) {
            if (Integer.valueOf(node[2]).intValue() == taskID.intValue()) {
                //return Integer.valueOf(node[3]);
                p = Integer.valueOf(node[3]);
            }
        }
        overlist.printProgressions();
        return p;
    }
    
    class CPUInfo implements Serializable {
         ProcCpu procCpu = getProcCpu();
        Sigar sigar = getSigar();
        int pid = getPid();
        private boolean error = false;
        
        private int getPid() {
            try {
                return Integer.parseInt(ManagementFactory.getRuntimeMXBean()
                        .getName().split("@")[0]);
            } catch (Exception ex) {
                error = true;
                return -1;
            }
        }

        private Sigar getSigar() {
            try {
                return new Sigar();
            } catch (Exception ex) {
                error = true;
                return null;
            }
        }

        private ProcCpu getProcCpu() {
            try {
                return new ProcCpu();
            } catch (Exception ex) {
                error = true;
                return null;
            }
        }
            
        public CPUInfo () {
        }
        
        /*
         * Get the cpu load (in percent)
         */
        public int gather() {
            if (error) return 0;
            try {
                procCpu.gather(sigar, pid);
            } catch (SigarException ex) {
                return 0;
            }
            return (int) (procCpu.getPercent() * 100.0);
        }
    }
}
