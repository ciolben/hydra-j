package epfl.monitor.gui;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * TestConfigurations.java (UTF-8)
 *
 * 26 mai 2012
 * @author Loic
 */
public class TestConfigurations implements Serializable {
    private ArrayList<Configuration> configurations;
    
    public TestConfigurations() {
        configurations = new ArrayList<>();
    }
    
    public void addConfiguration(String configName, int maxPartitionIndex,
            long memoryThreshold) {
        configurations.add(new Configuration(configName, maxPartitionIndex,
                memoryThreshold));
    }
    
    public ArrayList<Configuration> getConfigurations() {
        return configurations;
    }
}
