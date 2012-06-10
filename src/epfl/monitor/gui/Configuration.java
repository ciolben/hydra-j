package epfl.monitor.gui;

import java.io.Serializable;

/**
 * Configuration.java (UTF-8)
 *
 * 26 mai 2012
 *
 * @author Loic
 */
public class Configuration implements Serializable {

    private String configName;
    private int maxPartitionIndex;
    private long memoryThreshold;

    public Configuration(String configName, int maxPartitionIndex,
            long memoryThreshold) {
        this.configName = configName;
        this.maxPartitionIndex = maxPartitionIndex;
        this.memoryThreshold = memoryThreshold;
    }

    @Override
    public String toString() {
        return getConfigName();
    }

    /**
     * @return the configName
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * @return the maxPartitionIndex
     */
    public int getMaxPartitionIndex() {
        return maxPartitionIndex;
    }

    /**
     * @return the memoryThreshold
     */
    public long getMemoryThreshold() {
        return memoryThreshold;
    }

    /**
     * @param configName the configName to set
     */
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /**
     * @param maxPartitionIndex the maxPartitionIndex to set
     */
    public void setMaxPartitionIndex(int maxPartitionIndex) {
        this.maxPartitionIndex = maxPartitionIndex;
    }

    /**
     * @param memoryThreshold the memoryThreshold to set
     */
    public void setMemoryThreshold(long memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }
}
