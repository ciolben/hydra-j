package epfl.project.common;

import epfl.project.nodes.ThreadManager;
import epfl.project.scheduler.TaskDescription;
import epfl.project.scheduler.TaskScheduler;
import java.io.InputStream;
import java.io.PrintStream;

/**
 *
 * Class Configurator<br><br>
 *
 * The configurator is used to configure the mapreduce process.
 * <br><br>
 * Other methods are already provided with default behavior. Redefining them can
 * be useful in some particular context.
 *<br><br>
 * <b>Generic arguments: </b><br>
 * <code>K</code> = Type for the intermediate key (output of the map
 * function) <br>
 * <code>V</code> = Type for the intermediate value (output of the map function) <br>
 * <code>KR</code> = Type for the final key (output of the reduce function) <br>
 * <code>VR</code> = Type for the final value (output of the reduce function) <br>
 */
public class Configurator<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> {

    private String versionInfo = "version of mapreduce : "
            + "1.0\nBy Loic, Nicolas,"
            + " Erico";
    private InputStream inputStream                     = null;
    private PrintStream printStream                     = null;
    private boolean useConsole                          = false;
    private boolean useGUI                              = false;
    private boolean writeToDiskIfneeded                 = true;
    private boolean enableProbes                        = false;
    private int restartMasterTimes                      = 0;
    private TaskScheduler taskScheduler                 = new TaskScheduler();    

    
    public Configurator() {
	ThreadManager.setConfigurator(this);
  }
    
    /**
     * @return the taskScheduler
     */
    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    /**
     * Add a task to the scheduler.
     *
     * @param order the order of the task. Can be any positive number.
     * @param taskDescription the description of the map or reduce task.
     * @return <italic> true </italic> if no error while registering the task
     * description.
     */
    public boolean addTask(int order, TaskDescription taskDescription) {
        return taskScheduler.addTask(order, taskDescription);
    }

    public void nameRound(int order, String name) {
        taskScheduler.nameRound(order, name);
    }

    /**
     * 
     * Set the input stream object for collecting input queries. <b>if the
     * console is used</b>. By default, the InputStream is the System.in
     * InputStream. To turn on the interactive console capabilities, use
     * <italic> setConsoleActive</italic> method.
     *
     * @param inputStream
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     *
     * If the console is active, an interactive console will run allowing user
     * to write commands from it. User can set input and output objects via this
     * configurator, too.
     *
     * @param active
     */
    public void setConsoleActive(boolean active) {
        useConsole = active;
    }

    /**
     * 
     * Set the print stream object for printing outputs coming from the console
     * or, in case the console is not used, general outputs. By default,
     * System.out is used as PrintStream.
     *
     * @param printStream
     */
    public void setPrintWriter(PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     *
     * A GUI will show up, enabling the user to control the program with a
     * graphical environment. If setConsoleActive is true, setting this property
     * to true will override setConcoleActive to false, and printStream and
     * outputStream too.
     *
     * @param active
     */
    public void setGUIActive(boolean active) {
        useGUI = active;
    }

    /**
     * 
     * Get the print stream object.
     *
     * @return The output stream.
     */
    public PrintStream getPrintStream() {
        return printStream;
    }

    /**
     * 
     * Get the input stream object.
     *
     * @return The input stream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 
     * If user want to use the console, the returned value is true.
     *
     * @return true or false.
     */
    public boolean isConsoleActive() {
        return useConsole;
    }

    public boolean isGUIActive() {
        return useGUI;
    }

    /**
     * 
     * Returns version info.
     *
     * @return Text representing version info.
     */
    public String getVersionInfo() {
        return versionInfo;
    }

    /**
     * set the number of time i restart the master in case of some exception, default value 0;
     * @param times
     */
    public void setRestartMaster(int times){
    	restartMasterTimes = times;
    }
    
    public int getRestartMaster() {
    	return restartMasterTimes;
    }

    /**
     * force everything to stay in memory, by default if running out of memory
     * it tries instead to write to disk some data
     */
    public void disableWriteToDiskIfneeded() {
    	writeToDiskIfneeded = false;
    }
    
    public boolean isWriteToDiskIfneededEnabled(){
    	return writeToDiskIfneeded;
    }

    /**
     * @return the enableProbes
     */
    public boolean isEnableProbes() {
        return enableProbes;
    }

    /**
     * @param enableProbes the enableProbes to set
     */
    public void setEnableProbes(boolean enableProbes) {
        this.enableProbes = enableProbes;
    }
}
