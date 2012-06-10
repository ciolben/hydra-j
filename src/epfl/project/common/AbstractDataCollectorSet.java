package epfl.project.common;

import epfl.project.scheduler.TaskDescription;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;


/**
 *  Abstract class that must be implemented by all datacollector SET class
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractDataCollectorSet<K extends Comparable<K>, V> {

	protected static final String DIRNAME = "data";
	protected TaskDescription taskDescription;
    
    public AbstractDataCollectorSet(final TaskDescription taskDescription) {
        this.taskDescription = taskDescription;
        
    }

    public final static String getDirName(){
    	return DIRNAME;
    }

    /**
     * add a new collector, from a worker that finished his job
     *
     * 
     * @param workerID
     * @param newCollector
     */
   abstract public void addCollector( AbstractDataCollector<K, V> newCollector);


    
    /**
     * return the result of this specific task, must ask in a loop until receiving a null, in order to get all data inside the collector. (null = all results retrieved)
     * @return
     */
    abstract public  ArrayList<Tuple<K, V>> getPartitionForTask();
    
    
    protected  boolean deleteRecursiveTaskfilesAndDirectory(File path) throws FileNotFoundException{
        if (!path.exists()) return false;
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursiveTaskfilesAndDirectory(f);
            }
        }
        return ret && path.delete();
    }
        
    
  	 abstract public boolean writeToDisk();

}
