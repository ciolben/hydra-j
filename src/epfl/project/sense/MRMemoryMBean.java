/*
 * MapReduce API @ 2012
 */
package epfl.project.sense;

/**
 *
 * @author Loic
 */
public interface MRMemoryMBean {

    /**
     * Get the number of active workers
     */
    public Integer getMapperCount();
    public Integer getReducerCount();
}
