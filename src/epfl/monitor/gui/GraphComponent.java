package epfl.monitor.gui;

import java.awt.Component;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GraphComponent.java (UTF-8)
 *
 * 23 avr. 2012
 * @author Loic
 */
public abstract class GraphComponent {
    private String name;
    private boolean isLoaded    = false;
    private Object tabRef       = -1;
    private Timer task          = null;
    private RefreshTask rtask   = null;
    private boolean isScheduled = false;
    private int interval        = 1000;
    
    /**
     * Constructor of a GraphComponent.
     * @param name name is very important to be set to the corresponding path
     * in the tree, like "parentname/childname".
     */
    public GraphComponent(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    /**
     * Get the name of the graph.
     * @return the graph's name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Should return true if the graph is used somewhere. Typically, when
     * <code> setTabRef </code> is called, the graph is considered loaded.
     * @return <code> true </code> if the graph is loaded.
     */
    public boolean isLoaded() {
        return isLoaded;
    }
    
    /**
     * Inform the graph that it is not used anymore.
     */
    public void setUnloaded() {
        isLoaded = false;
    }
    
    /**
     * get the reference to the tab component.
     * @return 
     */
    public Object getTabRef() {
        return tabRef;
    }
    
    /**
     * set the persitent reference to a tab component.
     * @param tabRef the reference.
     */
    public void setTabRef(Object tabRef) {
        isLoaded = true;
        this.tabRef = tabRef;
    }
    
    /**
     * Set the refresh task for this graph.
     * @param refreshTask the refresh task.
     */
    public void setRefreshTask(final RefreshTask refreshTask) {
        task = new Timer();
        rtask = refreshTask;
    }
    
    /**
     * schedule a refresh task of the specified interval. The scheduled task
     * begin immediately after the call. If no task has been scheduled before,
     * then, obviously, the call has no effect.
     * @param interval the time in milliseconds. If the time is <i>negative</i>
     *  then the last (or default) interval will be used.
     */
    public void scheduleRefresh(int interval) {
        if (rtask == null) return;
        if (interval > 0) this.interval = interval;
        task = new Timer();
        isScheduled = true;
        task.schedule(new TimerTask() {

            @Override
            public void run() {
                rtask.refresh(GraphComponent.this);
            }
            
        }, 0, this.interval);
//        System.out.println(getName() + " is scheduled for " + this.interval);
    }
    
    /**
     * Cancel the refresh task. Once this call has happened,
     * <code>isScheduled</code> returns false.<br>
     * To resume a canceled refresh task, use <code>scheduleRefresh</code>.
     */
    public void cancelRefresh() {
        if (task != null) task.cancel();
        isScheduled = false;
    }
    
    /**
     * Get the status of the refresh task.
     * @return <code> true </code> if the graph has an active refreshing period.
     */
    public boolean isScheduled() {
        return isScheduled;
    }
    
    /**
     * Change the refresh interval.
     * @param interval the time in milliseconds. If the time is <i>negative</i>
     * then the last (or default) interval will be used.
     */
    public void changeRefreshInterval(int interval) {
        if (task == null) return;
        task.cancel();
        task = null;
        scheduleRefresh(interval);
    }
    
    /**
     * Returns the component to be drawn on the screen.
     * <br><b>Note : </b> this function can be hacked and can returns anything
     * but a graph if needed.
     * @return the chart.
     */
    public abstract Component getChart();
    
    /**
     * Standard method to update the graph. It allows two string parameters to be
     * passed and one number.
     * @param val1 first value.
     * @param val2 second value.
     * @param value the value to show in the graph.
     */
    public abstract void updateGraph(String val1, String val2, Number value);
}
