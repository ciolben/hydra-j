package epfl.project.algorithm;

import epfl.project.common.AbstractDataCollectorSet;
import epfl.project.common.KeyValue;
import java.util.ArrayList;

/**
 * *
 *
 * class : SpliterAlgorithm
 *
 * The spliter algorithm can be redefined by overridden the split function.
 * This spliter must be used when we split the result of a reduce task
 * 
 * Information are stored in lastReduceResult (ArrayList<KeyValue<KR, VR>>).
 * The method nextReduceResult() set lastReduceResult to the next information so it must be called to obtain all available information
 * 
 */
public abstract class SpliterAlgorithmReduce<KR extends Comparable<KR>, VR> implements SpliterAlgorithm{

    public ArrayList<KeyValue<KR, VR>> lastReduceResult;
    public AbstractDataCollectorSet sourceData;
    
    /**
     * Put the next data into the reduceResult array (old value are no more available)
     * This must be called to obtain the next values
     */
    public void nextReduceResult(){
    	lastReduceResult = sourceData.getPartitionForTask();
    }

}
