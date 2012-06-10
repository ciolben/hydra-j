package epfl.project.common;

import epfl.project.algorithm.CombinerAlgorithm;
import epfl.project.algorithm.PartitionerAlgorithm;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class DataCollectorHashMap<K extends Comparable<K>, V> extends AbstractDataCollector<K, V> {

    private HashMap<Integer, HashMap<K, V>> dataCollected;
    private HashMap<Integer, HashMap<K, ArrayList<V>>> dataCollectedMultipleValue;
    private PartitionerAlgorithm<K, V> partitioner;
    private CombinerAlgorithm<K, V> combiner;
    
    private boolean redundatEnabled;
    private MessageDigest hash = null;
	private byte[] hashB = null;

    /**
     * *
     * Constructor.
     *
     * @param pa partitioner
     */
    public DataCollectorHashMap(PartitionerAlgorithm<K, V> pa, CombinerAlgorithm<K, V> combiner, boolean redundantWorkers) {
        // If the initial capacity is greater than
    	// the maximum number of entries divided by the load factor, no rehash operations will ever occur.
    	dataCollected = new HashMap<>(pa.getMaxIndexPartition() + 1, 1);

        this.partitioner = pa;
        this.combiner = combiner;
        
        if (redundantWorkers){
            super.enableHash();
        }

    }

    @Override
    public void collect(K mKey, V mValue) {
        super.collect(mKey, mValue);
    
        int index = partitioner.partition(mKey);
        HashMap<K, V> hashmap = dataCollected.get(index);

        if (hashmap == null) {
            hashmap = new HashMap<>();
            dataCollected.put(index, hashmap);
        }

        V old = null;

        old = hashmap.get(mKey);
        if (old != null) {
            V valueCombined = combiner.combine(old, mValue);

            // no combiner and old value exist so we must save all value
            if (valueCombined == null) {
                if (dataCollectedMultipleValue == null) {
                    dataCollectedMultipleValue = new HashMap<>(partitioner.getMaxIndexPartition() + 1, 1);
                }
                HashMap<K, ArrayList<V>> hashmapMultipleValue = dataCollectedMultipleValue.get(index);
                if (hashmapMultipleValue == null) {
                    hashmapMultipleValue = new HashMap<>();
                    dataCollectedMultipleValue.put(index, hashmapMultipleValue);
                }
                ArrayList<V> listeValue = hashmapMultipleValue.get(mKey);
                if (listeValue == null) {
                    listeValue = new ArrayList<>();
                    hashmapMultipleValue.put(mKey, listeValue);
                }
                listeValue.add(mValue);
                listeValue.add(old);
                hashmap.remove(mKey);

                // combiner and old value exist so combine and store
            } else {
                hashmap.put(mKey, valueCombined);
            }
            // no old value in the first hashmap, maybe in the second
        } else {
            // not in the second
            if (dataCollectedMultipleValue == null) {
                hashmap.put(mKey, mValue);
            } else {
                HashMap<K, ArrayList<V>> multipleValueHashmap = dataCollectedMultipleValue.get(index);
                if (multipleValueHashmap == null) {
                    hashmap.put(mKey, mValue);
                } else {
                    ArrayList<V> valueList = multipleValueHashmap.get(mKey);
                    if (valueList == null) {
                        hashmap.put(mKey, mValue);
                    } else {
                        valueList.add(mValue);
                    }
                }
            }
        }
    }

    public void print() {
        System.out.println(dataCollected);
    }

    @Override
    public void clearCollector() {
        dataCollected.clear();//TODO what if = null??
        if (dataCollectedMultipleValue != null) {
        	dataCollectedMultipleValue.clear();
        }
        dataCollected = null;
        dataCollectedMultipleValue = null;
    }

    @Override
    public ArrayList<Tuple<K, V>> getPartition(int index) {
        if (dataCollected == null){
        	return null;
        }
        HashMap<K, V> tmp = dataCollected.get(index);

        if (tmp == null) {
            return null;
        }

        ArrayList<Tuple<K, V>> liste = new ArrayList<>();
        Set<K> keySet = tmp.keySet();
        for (K key : keySet) {
            liste.add(new Tuple<>(key, tmp.get(key)));
        }

        if (dataCollectedMultipleValue != null) {
            HashMap<K, ArrayList<V>> tmpList = dataCollectedMultipleValue.get(index);
            if (tmpList != null) {
            	Set<K> keySet2 = tmpList.keySet();
            	ArrayList<V> valueList;
            	for (K key : keySet2) {
            		valueList = tmpList.get(key);

            		for (V value : valueList) {
            			liste.add(new Tuple<>(key, value));
            		}
            	}
            	tmpList.clear();
            }
        }
        tmp.clear();

        return liste;
    }
}
