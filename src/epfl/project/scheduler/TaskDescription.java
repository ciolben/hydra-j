package epfl.project.scheduler;

import epfl.project.algorithm.CombinerAlgorithm;
import epfl.project.algorithm.PartitionerAlgorithm;
import epfl.project.algorithm.SpliterAlgorithm;
import epfl.project.common.*;
import epfl.project.mapreduce.Mapper;
import epfl.project.mapreduce.Reducer;
import java.io.File;

/**
 * class : represents the encapsulation of a task with its attributes.
 *
 */
public class TaskDescription<K extends Comparable<K>, V, KR extends Comparable<KR>, VR> {

    private String name;
    private int id;
    private TaskCategory category;
    private Mapper<K, V> mapper                             = null;
    private Reducer<K, V, KR, VR> reducer                   = null;
    private CombinerAlgorithm<K, V> combiner                = null;
    private SpliterAlgorithm spliter                        = null;
    private PartitionerAlgorithm<K, V> partioner            = null;
    private File  input                                     = null;
    private File output                                     = null;
    private Tuple<Integer, Double> redundantWorker          = null;
    private int dataCollector                               = 0;
    private boolean writetodisk                             = false;
    private int sizeForFixedSizeArrayCollector = 0;
    private boolean stopOnRedundantError = true;

    /**
     * Constructor of a task's description.
     *
     * @param name the name of the task (optional)
     * @param category mandatory : Map or Reduce task.
     */
    public TaskDescription(String name, TaskCategory category) {
        this.name = name;
        this.category = category;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Called internally.
     * @param id 
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the category
     */
    public TaskCategory getCategory() {
        return category;
    }

    /**
     * @return the combiner
     */
    public CombinerAlgorithm getCombiner() {
        if (getCategory() == TaskCategory.valueOf("MAP")) {
            return combiner;
        }
        return null;
    }

    /**
     * @param combiner the combiner to set
     */
    public void setCombiner(CombinerAlgorithm combiner) {
        this.combiner = combiner;
    }

    /**
     * @return the splitter
     */
    public SpliterAlgorithm getSpliter() {
        return spliter;
    }

    /**
     * @param splitter the splitter to set
     */
    public void setSpliter(SpliterAlgorithm spliter) {
        this.spliter = spliter;
    }

    /**
     * @return the partioner
     */
    public PartitionerAlgorithm getPartitioner() {
        return partioner;
    }

    /**
     * @param partionner the partioner to set
     */
    public void setPartitioner(PartitionerAlgorithm partioner) {
        this.partioner = partioner;
    }

    /**
     * @return the input
     */
    public File getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(File input) {
        this.input = input;
    }

    /**
     * @return the output
     */
    public File getOutput() {
        return output;
    }

    /**
     * The file where the result will be written
     *
     * @param output the output to set
     * @throws TaskException
     */
    public void setOutput(File output) throws TaskException {
        if (output instanceof File) {
            this.output = output;
            writetodisk = true;
        } else {
            throw new TaskException("invalid output");
        }
    }

    /**
     * @return the mapper
     */
    public Mapper<K, V> getMapper() {
        return mapper;
    }

    /**
     * @param mapper the mapper to set
     */
    public void setMapper(Mapper<K, V> mapper) {
        this.mapper = mapper;
    }

    /**
     * @return the reducer
     */
    public Reducer<K, V, KR, VR> getReducer() {
        return reducer;
    }

    /**
     * @param reducer the reducer to set
     */
    public void setReducer(Reducer<K, V, KR, VR> reducer) {
        this.reducer = reducer;
    }


    /**
	 * let enable the error detection by computing multiple times the same 
         * job and checking if a certain percentage have the same result then accept
	 * if not return an error
	 * @param redundancy have to be bigger than 1
	 * @param minPercentage have to be bigger than 0 and less or equal to 100
	 * @param stop is the choice what to do in case of an error detected 
	 * 			true=exit when first error detected
	 * 			false=keep doing computation, the result will be only partial result, but reliable 
	 * @return true if enabled successfully false if already enabled
	 */
    public void enableRedundantErrorDetector(int redundancy, double minPercentage, boolean stop) {
    	if (minPercentage <= 100 && minPercentage > 0 && redundancy > 1){
    		redundantWorker = new Tuple<>(redundancy, minPercentage);
    	    stopOnRedundantError = stop;
    	} else {
    		System.err.println("Bad redundant parameters, redundancy disabled!");
    	}
    	}
    
    /**
     * 
     * @return  a tuple<redundancy, minpercentage> if redundancy errors check is enabled null otherwise
     */
    public Tuple<Integer, Double>  getRedundantErrorDetector(){
    	return redundantWorker;
    	
    }
    
    public boolean  stopOnRedundantError(){
    	return stopOnRedundantError;
    	
}
    
    public boolean writeResultTodisk(){
    	return writetodisk; 
    }
    
    

    /**
     * Set the dataCollector Object. The API provide a default collector based
     * on a hashmap of arraylist. Different kind of collector are predefined and
     * can be used in function of the kind of work provided to the framework.
     *
     * <ol start="0">
     * <li>HasMap of ArrayList</li>
     * <li>HashMap of TreeMap</li>
     * <li>HashMap of HashMap</li>
     * <li>Fixed Size Array, must use
     * the setSizeForFixedSizeArrayCollector(int size) method to choose the size
     * of the array</li>
     * <li>Common (the same for all thread) Fixed Size Array, must
     * use the setSizeForFixedSizeArrayCollector(int size) method to choose the
     * size of the array</li>
     * </ol>
     *
     * @param dataCollector
     */
    public void setDataCollector(int dataCollector) {
        this.dataCollector = dataCollector;
    }

    /**
     * Set the size of the fixed size dataCollector :
     * DataCollectorFixedSizeArray, DataCollectorCommonFixedSizeArray
     *
     * @param size
     */
    public void setSizeForFixedSizeArrayCollector(int size) {
        sizeForFixedSizeArrayCollector = size;
    }

    /**
     * Get an Integer that gives information of what kind of data collector the
     * user will use
     */
    public int getDataCollector() {
        return dataCollector;
    }
    
    /**
     * Choice between different data collector :
     * <ol start="0">
     * <li>HasMap of ArrayList</li>
     * <li>HashMap of TreeMap</li>
     * <li>HashMap of HashMap</li>
     * <li>Fixed Size Array, must use
     * the setSizeForFixedSizeArrayCollector(int size) method to choose the size
     * of the array</li>
     * <li>Common (the same for all thread) Fixed Size Array, must
     * use the setSizeForFixedSizeArrayCollector(int size) method to choose the
     * size of the array</li>
     * </ol>
     *
     * @return
     */
    public synchronized AbstractDataCollector<K, V> createCollector(TaskDescription taskDescription) {
        boolean isRedundantEnable = getRedundantErrorDetector() != null;
        switch (dataCollector) {
            case 0:
                return new DataCollectorArrayList<>(taskDescription
                        .getPartitioner(), taskDescription.getCombiner(),
                        isRedundantEnable);
            case 1:
                return new DataCollectorTreeMap<>(taskDescription
                        .getPartitioner(), taskDescription.getCombiner(),
                        isRedundantEnable);
            case 2:
                return new DataCollectorHashMap<>(taskDescription
                        .getPartitioner(), taskDescription.getCombiner(),
                        isRedundantEnable);
            case 3:
                return new DataCollectorFixedSizeArray<>(taskDescription
                        .getPartitioner(), taskDescription.getCombiner(),
                        sizeForFixedSizeArrayCollector, isRedundantEnable);
            case 4:
                return new DataCollectorCommonFixedSizeArray<>(taskDescription
                        .getPartitioner(), sizeForFixedSizeArrayCollector,
                        isRedundantEnable);
            default:
                return new DataCollectorArrayList<>(taskDescription
                        .getPartitioner(), taskDescription.getCombiner(),
                        isRedundantEnable);
        }
    }
}
