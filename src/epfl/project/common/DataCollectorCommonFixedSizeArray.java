package epfl.project.common;

import epfl.project.algorithm.PartitionerAlgorithm;
import epfl.project.faults.RedundantWorkers;

import java.util.ArrayList;
import java.util.Arrays;

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
public class DataCollectorCommonFixedSizeArray<K extends Comparable<K>, V> extends AbstractDataCollector<K, V> {

    private PartitionerAlgorithm<K, V> partitioner;
    private int size;
    private CommonArray dataCollected;

    public DataCollectorCommonFixedSizeArray(PartitionerAlgorithm<K, V> pa, int size,  boolean redundantWorkers) {
        this.size = size;
        this.partitioner = pa;

        //Singleton
        dataCollected = CommonArray.getInstance(size);
        if (redundantWorkers){
        	super.enableHash();
        }

    }

    @Override
    public void collect(K mKey, V mValue) {
        super.collect(mKey, mValue);

        //Tuple<K, V> data = new Tuple<K, V>(mKey, mValue);
        //only one possible value for each index
        Integer index = (Integer) mKey;

        dataCollected.add(mValue, index);
    }

    @Override
    public void clearCollector() {
        dataCollected = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Tuple<K, V>> getPartition(int index) {

        ArrayList<Tuple<K, V>> partition = new ArrayList<>();

        for (Integer i = 0; i < size; i++) {
            if (partitioner.partition(i) == index) {
                V value = (V) dataCollected.get(i);
                if (value != null) {
                    partition.add(new Tuple<>((K) i, value));
                }
            }
        }

        return partition;
    }
}
//Singleton
class CommonArray {

    private Object[] dataCollected;
    private static CommonArray uniqueInstance;
    private static int lastSize;

    private CommonArray(int size) {
        dataCollected = new Object[size];
    }

    public static synchronized CommonArray getInstance(int size) {
        if (uniqueInstance == null || size != lastSize) {
            lastSize = size;
            uniqueInstance = new CommonArray(size);
        }
        return uniqueInstance;
    }

    public synchronized void add(Object obj, int index) {
        dataCollected[index] = obj;
    }

    public Object get(int index) {
        return dataCollected[index];
    }

    public void print() {
        System.out.println(Arrays.toString(dataCollected));
    }
}
