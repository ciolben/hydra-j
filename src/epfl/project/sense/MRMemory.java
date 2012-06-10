/*
 * MapReduce API @ 2012
 */

package epfl.project.sense;

import epfl.project.common.OutOfMemory;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.util.ArrayList;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

/**
 * MRMemoryMBean.java (UTF-8)
 *
 * MBean register : type=mrmemory
 * 
 * 16 avr. 2012
 * @author Loic
 */
public class MRMemory implements MRMemoryMBean {
    private static Probe memProbe       = null;
    private static MRMemory mrm         = null;
    
    private MemoryMXBean memoryMXBean   = null;
    private ArrayList<ThresholdNotificationHandler> handlers;
    private int maxWorker = 0;
    private OutOfMemory memManager;
    private int mapperNumber = 0;
    private int reducerNumber = 0;

    private MRMemory () {
        //the probe is only created when the object is instanciated the
        //first time.
        if (memProbe == null) {
            memProbe = new Probe((byte)2);
            memProbe.addRecord((byte)0, 0); //mapper number
            memProbe.addRecord((byte)1, 0); //reducer number
        }
        
        handlers = new ArrayList<>(1);
    }
    
    /**
     * Create or return an instance of this object (Singleton).
     * @return the reference of this MRMemory object.
     */
    public static synchronized MRMemory getInstance() {
        if (mrm == null) {
            mrm = new MRMemory();
        }
        return mrm;
    }
    
    public void setMemoryManager(OutOfMemory manager) {
        memManager = manager;
    }
    
    public void setMemoryThreshold(long threshold) {
        if (memManager == null) return;
        memManager.setThreshold(threshold);
    }
    
    /**
     * Get the number of active mappers (MBean pattern)
     */
    @Override
    public Integer getMapperCount() {
        return mapperNumber;
    }
    
    /**
     * Get the number of active reducers (MBean pattern)
     */
    @Override
    public Integer getReducerCount() {
        return reducerNumber;
//        return (Integer) memProbe.getRecord((byte)1);
    }
    
    /**
     * Get the memory info about the heap of the JVM.
     * <br><br>
     * <b>Javadoc extract:</b><br>
     * The Java virtual machine has a heap that is the runtime data area from
     * which memory for all class instances and arrays are allocated. It is 
     * created at the Java virtual machine start-up. Heap memory for objects 
     * is reclaimed by an automatic memory management system which is known 
     * as a garbage collector. <br><br>
     * 
     * <b>Note : </b> it is useless to call this method more than one time
     * after the object is obtained. It refreshes heap info itself.
     * 
     * @return the MXBean containing the heap memory info.
     */
    public MemoryMXBean getMemoryInfo() {
        if (memoryMXBean == null) memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean;
    }
    
    /**
     * Add a notification handler that handles memory threshold exceedance.<br>
     * <br>
     * The handler can be called <b>many times</b> in a short while for the
     * same event.
     * @param handler
     * @param threshold 
     */
    public void addThresholdNotificationHandler(final ThresholdNotificationHandler
            handler, long threshold) {
        if (handlers.contains(handler)) return;
        handlers.add(handler);
        
        //http://docs.oracle.com/javase/7/docs/api/java/lang/management/MemoryPoolMXBean.html
       
        class ThresholdListener implements NotificationListener {
            @Override
            public void handleNotification(Notification notification, Object handback)  {
                if (notification.getType().equals(
                        MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    handler.handleNotification();
                }
            }
       }

       //Register MyListener with MemoryMXBean
       MemoryMXBean mbean = getMemoryInfo();
       NotificationEmitter emitter = (NotificationEmitter) mbean;
       emitter.addNotificationListener(new ThresholdListener(), null, null);

       //Assume this pool supports a usage threshold.
       for (java.lang.management.MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
           pool.setUsageThreshold(threshold);
       }
    }
    
    /**
     * @return the probe
     */
    public static Probe getProbe() {
        return memProbe;
    }
    
    public void setMapperNumber(int number) {
        mapperNumber = number;
    }
    
    public void setReducerNumber(int number) {
        reducerNumber = number;
    }
    
    /**
     * Use &#8249; 0 to reset the counter.
     * @param number as an integer.
     */
    public void setMaxWorkerNumber(int number) {
        if (number < 0) maxWorker = 0;
        else if (maxWorker > number) return;
        maxWorker = number;
    }
    
    public int getMaxWorkerNumber() {
        return maxWorker;
    }
}
