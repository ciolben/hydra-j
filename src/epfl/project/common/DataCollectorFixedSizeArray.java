package epfl.project.common;

import epfl.project.algorithm.CombinerAlgorithm;
import epfl.project.algorithm.PartitionerAlgorithm;
import epfl.project.faults.RedundantWorkers;

import java.util.ArrayList;

/**
 * *
 *
 * class : DataCollectorFixedSizeArray
 *
 * Implementation choice : written on the hdd or in memory note : data should be
 * written in any case on the hdd at some point.
 *
 * Generic arguments: K = Type for the intermediate key (output of the map
 * function) V = Type for the intermediate value (output of the map function)
 *
 * The key must be an integer between 0 and size - 1
 */
public class DataCollectorFixedSizeArray<K extends Comparable<K>, V> extends AbstractDataCollector<K, V> {

    private PartitionerAlgorithm<K, V> partitioner;
    private CombinerAlgorithm<K, V> combiner;
    private int size;
    private Object[] dataCollectedValue;
    private Object[] dataCollectedMultipleValue;

    /**
     * *
     * Constructor.
     *
     * @param pa partitioner
     */
    public DataCollectorFixedSizeArray(PartitionerAlgorithm<K, V> pa, CombinerAlgorithm<K, V> combiner, int size,  boolean redundantWorkers) {
        this.size = size;
        this.partitioner = pa;
        this.combiner = combiner;

        dataCollectedValue = new Object[this.size];
        if (redundantWorkers){
        	super.enableHash();
        }
    }

    @Override
    public void collect(K mKey, V mValue) {
        super.collect(mKey, mValue);


        Integer index = (Integer) mKey;
        @SuppressWarnings("unchecked")
        V oldValue = (V) dataCollectedValue[index];

        //no old value in the first array, must check in the second
        if (oldValue == null) {
            if (dataCollectedMultipleValue != null) {
                @SuppressWarnings("unchecked")
                ArrayList<Object> oldValueList = (ArrayList<Object>) dataCollectedMultipleValue[index];
                if (oldValueList == null) {
                    dataCollectedValue[index] = mValue;
                } else {
                    oldValueList.add(mValue);
                }
            } else {
                dataCollectedValue[index] = mValue;
            }
        } else {

            V combined = combiner.combine(oldValue, mValue);

            // no combiner and old value exist so we must save all value
            if (combined == null) {
                //oldTuple.combineValueList(null, data.getValue());
                if (dataCollectedMultipleValue == null) {
                    dataCollectedMultipleValue = new Object[size];
                }
                @SuppressWarnings("unchecked")
                ArrayList<Object> valueList = (ArrayList<Object>) dataCollectedMultipleValue[index];
                if (valueList == null) {
                    valueList = new ArrayList<>();
                    dataCollectedMultipleValue[index] = valueList;
                }
                valueList.add(mValue);
                valueList.add(oldValue);
                dataCollectedValue[index] = null;

                // combiner and old value exist	
            } else {
                //oldTuple.combineValue(combined);
                dataCollectedValue[index] = combined;
            }
        }

    }

    @Override
    public void clearCollector() {
        dataCollectedValue = null;
        dataCollectedMultipleValue = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Tuple<K, V>> getPartition(int index) {
        ArrayList<Tuple<K, V>> partition = new ArrayList<>();
        for (Integer i = 0; i < size; i++) {
            if (partitioner.partition(i) == index) {
                V value = (V) dataCollectedValue[i];
                if (value != null) {
                    partition.add(new Tuple<>((K) i, value));
                }
            }
        }

        if (dataCollectedMultipleValue != null) {
            for (Integer i = 0; i < size; i++) {
                if (partitioner.partition(i) == index) {
                    ArrayList<V> valueList = (ArrayList<V>) dataCollectedMultipleValue[i];
                    if (valueList != null) {
                        int sizeList = valueList.size();
                        for (int j = 0; j < sizeList; j++) {
                            partition.add(new Tuple<>((K) i, valueList.get(j)));
                        }
                    }
                }
            }
        }
        return partition;
    }
}
