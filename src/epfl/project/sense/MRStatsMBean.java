package epfl.project.sense;

/**
 * MRStatsMBean.java (UTF-8)
 *
 * 3 mai 2012
 * @author Loic
 */
public interface MRStatsMBean {

    /**
     * get the average time for all the map functions in the current round.
     * @return long
     */
    public long getAverageMapTime();

    /**
     * get the average the map functions have taken until now.
     * @return long
     */
    public long getAllRoundsAverageMapTime();

    /**
     * get the average time for all the reduce functions in the current round.
     * @return long
     */
    public long getAverageReduceTime();

    /**
     * get the average the reduce functions have taken until now.
     * @return long
     */
    public long getAllRoundsAverageMapTime();

    /**
     * Construct the overview tree of the mapreduce job. If it is already
     * constructed, the result will be the actual state of the mapreduce job.
     * @return OverviewList
     */
    public OverviewList getOverviewList();

    /**
     * Get the progression percentage.
     * @param roundID the round id
     * @param taskID the task id
     * @return int
     */
    public int getProgression(Integer roundID, Integer taskID);

    /**
     * Gather the cpu load.
     * @return the load of the cpu in percent
     */
    public int getCpuLoad();
    
    /**
     * Clear the average worker times.
     */
    public void clearTimes();
}
