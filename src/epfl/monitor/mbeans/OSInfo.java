package epfl.monitor.mbeans;

import java.lang.management.OperatingSystemMXBean;

/**
 * OSDecorator.java (UTF-8)
 *
 * Must be created one time to avoid repetitive call to the distant JVM with
 * slow syscall queries.
 * 24 avr. 2012
 * @author Loic
 */
public class OSInfo {
    private String arch;
    private String name;
    private String version;
    private int availableProc;
    private OperatingSystemMXBean osBean;
    
    public OSInfo(OperatingSystemMXBean osBean) {
        this.osBean = osBean;
        arch = osBean.getArch();
        name = osBean.getName();
        version = osBean.getVersion();
        availableProc = osBean.getAvailableProcessors();
    }
    
    public String getName() {
        return name;
    }
    public String getArch() {
        return arch;
    }
    public String getVersion() {
        return version;
    }
    public int getAvailableProcessors() {
        return availableProc;
    }
    /**
     * Get the system load average for the last minute. <b>Can be expensive to 
     * compute for the distant JVM.</b>
     * @return the system load.
     */
    public double getSystemLoadAverage() {
        return osBean.getSystemLoadAverage();
    }
}
