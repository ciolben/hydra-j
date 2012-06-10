package epfl.monitor.gui;

/**
 *
 * @author Loic
 */
public enum TreeNames {
    General("General", "root/General"), JVMMemory("Memory", "JVM/Memory"),
    JVMThread("Threads", "JVM/Threads"), WorkerCount("Workers", "MapReduce/Worker Numbers"),
    WorkerGlobal("Average Map/Reduce Time", "MapReduce/Times"), Overview("Overview", "MapReduce/Overview"),
    JVMCPU("CPU", "JVM/CPU");
    
    public final String name;
    public final String t_name;
    private TreeNames(String name, String t_name) {
        this.name = name;
        this.t_name = t_name;
    }
}
