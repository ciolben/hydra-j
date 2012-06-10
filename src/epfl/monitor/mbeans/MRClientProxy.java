/*
 * MapReduce API @ 2012
 */

package epfl.monitor.mbeans;

import epfl.project.sense.OverviewList;
import java.io.IOException;
import java.lang.management.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import javax.management.NotificationListener;

/**
 * ClientProxy.java (UTF-8)
 *
 * 17 avr. 2012
 * @author Loic
 */
public class MRClientProxy extends BeanClient {
    private MemoryMXBean memoryBean     = null;
    private ThreadMXBean threadBean     = null;
    private RuntimeMXBean runtimeBean   = null;
    private OSInfo osInfo               = null;
    
    private String address              = "localhost";
    private int port                    = 9876;
    
    private ArrayList<ServerUnreachableEventHandler>
            serverUnreacheableHandlerList = new ArrayList<>(1);
    
    public MRClientProxy() {
        super(9876, "localhost");
    }
    
    public MRClientProxy(String address, int port) {
        super(port, address);
        if (port > 0) this.port = port;
        if (address != null && !address.isEmpty()) this.address = address;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getAddress() {
        return address;
    }
    
    /**Try to connect to the remote JVM. If a connection already exists, this
     * method does nothing.
     * @return <code>true</code> if client started successfully.
     */
    public boolean connect() {
        return super.startClient();
    }
    
    /**
     * Must be called to properly close the connection. If the connection is
     * already closed or doesn't exist, this method does nothing.<br>
     * In addition, all the handlers for the <i> unreachable server </i> event
     * will be notified.
     */
    public void disconnect() {
        super.stopClient();
        manageServerUnreachable(null);
    }
    
    public boolean isConnected() {
        return super.jmxc != null;
    }
    
    /**
     * Retrieve memory info of the distant JVM.
     * @param heap <code>true</code> for heap memory, <code>false</code>
     * for non-heap memory info.
     * @return The memory usage object. Null if there was an error.
     */
    public MemoryUsage getMemoryInfo(boolean heap) {
        if (memoryBean == null) {
            try {
                memoryBean = ManagementFactory.newPlatformMXBeanProxy
                (this.mbsc,
                ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
            } catch (IOException ex) {
                return null;
            }
        }
        try {
        if (heap) {
            return memoryBean.getHeapMemoryUsage();
        } else {
            return memoryBean.getNonHeapMemoryUsage();
        }
        } catch (Exception any) {
            memoryBean = null;
            return null;
        }
    }
    
    /**
     * Get all live thread info from the distant JVM.
     * @return an array containing info for each live thread. Can return <code>
     * null</code> in case of connection problems.
     */
    public ThreadInfo[] getThreadInfo() {
        if (threadBean == null) {
            try {
                threadBean = ManagementFactory.newPlatformMXBeanProxy
                (this.mbsc,
                ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
            } catch (IOException ex) {
                return null;
            }
        }
        try {
            return threadBean.getThreadInfo(threadBean.getAllThreadIds(), 0);
        } catch (IllegalArgumentException | SecurityException
                | UndeclaredThrowableException ex) {
            return null;
        }
    }
    
    /**
     * Get the the management interface for the runtime system of the distant
     * JVM.<br>
     * <b>Useful queries</b> :<br><code>
     * -getName() : Returns the name representing the running JVM. <br>
     * -getStartTime() : Returns the start time of the Java virtual machine in 
     * milliseconds. <br>
     * -getSystemProperties() : Returns a map of names and values of all
     * system properties. <br>
     * -getUptime() : Returns the uptime of the Java virtual machine in
     * milliseconds.</code>
     * @return the MXBean containing the info.
     */
    public RuntimeMXBean getRuntimeInfo() {
        if (runtimeBean == null) {
            try {
                runtimeBean = ManagementFactory.newPlatformMXBeanProxy
                (this.mbsc,
                ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
            } catch (IOException ex) {
                return null;
            }
        }
        return runtimeBean;
    }
    
    /**
     * Get operating system info of the distant JVM. Can obtain CPU load average
     * this way.
     * @return the packed OS info.
     */
    public OSInfo getOSInfo() {
        if (osInfo == null) {
            try {
                OperatingSystemMXBean osBean = ManagementFactory.newPlatformMXBeanProxy
                (this.mbsc,
                ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
                osInfo = new OSInfo(osBean);
            } catch (IOException ex) {
                return null;
            }
        }
        return osInfo;
    }
    
    /**
     * Get the number of mapper that are active now.
     * @return the number of mapper.
     */
    public int getMRMemory_MapperCount() {
        try {
            return super.getAttribute("type=mrmemory", "MapperCount");
        } catch (ServerUnreachableException ex) {
            System.err.println("ERROR : " + ex.getMessage());
            manageServerUnreachable(ex);
        }
        return 0;
    }
    
    /**
     * Get the number of reducer that are active now.
     * @return the number of reducer.
     */
    public int getMRMemory_ReducerCount() {
        try {
            return super.getAttribute("type=mrmemory", "ReducerCount");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0;
    }
    
    /**
     * 
     * @return 
     */
    public long getMRStats_AverageMapTime() {
        try {
            return super.getAttribute("type=mrstats", "AverageMapTime");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0L;
    }
    
    public long getMRStats_AverageReduceTime() {
        try {
            return super.getAttribute("type=mrstats", "AverageReduceTime");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0L;
    }
    
    public long getMRStats_AllAverageMapTime() {
        try {
            return super.getAttribute("type=mrstats", "AllAverageMapTime");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0L;
    }
    
    public long getMRStats_AllAverageReduceTime() {
        try {
            return super.getAttribute("type=mrstats", "AllAverageReduceTime");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0L;
    }
    
    public OverviewList getMRStats_OverviewList() {
        try {
            return super.getAttribute("type=mrstats", "OverviewList");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return null;
    }
    
    public int getMRStats_Progress(int round, int taskID) {
        try {
            return super.invokeMethod("type=mrstats", "getProgression", new Object [] {
            round,
            taskID});
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0;
    }
    
    public int getMRStats_CpuLoad() {
        try {
            return super.getAttribute("type=mrstats", "CpuLoad");
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
        return 0;
    }
    
     public void MRStats_clearTimes() {
        try {
            super.invokeMethod("type=mrstats", "clearTimes", new Object [] {});
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
    }

    public void MRCtrl_sendCommand(String command) {
        try {
            super.invokeMethod("type=mrcontrol", "sendCommand", new Object [] {
            command });
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
    }
    
    public void MRCtrl_connectionTest() {
         try {
            super.invokeMethod("type=mrcontrol", "connectionTest", new Object [] {
            });
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
    }
    
    public void addServerUnreachableEventHandler(ServerUnreachableEventHandler handler) {
        serverUnreacheableHandlerList.add(handler);
    }
    
    public void removeServerUnreachableEventHandler(ServerUnreachableEventHandler handler) {
        serverUnreacheableHandlerList.remove(handler);
    }
    
    private void manageServerUnreachable(ServerUnreachableException ex) {
        if (ex != null) System.out.println("warning : " + ex.getMessage());
        //need to notify event handlers
        for (ServerUnreachableEventHandler handler : serverUnreacheableHandlerList) {
            handler.handleEvent(this);
        }
    }
    
    @Override
    public void addNotificationListener(String oname,
            NotificationListener listener) {
        try {
            super.addNotificationListener(oname, listener);
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
    }
    
    @Override
    public void removeNotificationListener(String oname,
            NotificationListener listener) {
        try {
            super.removeNotificationListener(oname, listener);
        } catch (ServerUnreachableException ex) {
            manageServerUnreachable(ex);
        }
    }
}
