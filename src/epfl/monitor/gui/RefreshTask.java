package epfl.monitor.gui;

/**
 * RefreshTask.java (UTF-8)
 *
 * Make the graph refresh automatically by calling its method.
 * 
 * 24 avr. 2012
 * @author Loic
 */
public interface RefreshTask {
    
    /**
     * Called once the graph need to be updated.
     * @param graph the concerned graph.
     */
    public void refresh(GraphComponent graph);
}
