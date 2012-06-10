package epfl.project.common;

import epfl.project.algorithm.CombinerAlgorithm;
import epfl.project.algorithm.PartitionerAlgorithm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * *
 *
 * class : DataCollectorTreeMap
 *
 * Implementation choice : written on the hdd or in memory note : data should be
 * written in any case on the hdd at some point.
 *
 * Generic arguments: K = Type for the intermediate key (output of the map
 * function) V = Type for the intermediate value (output of the map function)
 */
public class DataCollectorTreeMap<K extends Comparable<K>, V> extends AbstractDataCollector<K, V> {

    private HashMap<Integer, TreeMap<K, V>> dataCollected;
    private HashMap<Integer, TreeMap<K, ArrayList<V>>> dataCollectedMultipleValue;
    private PartitionerAlgorithm<K, V> partitioner;
    private CombinerAlgorithm<K, V> combiner;

    /**
     * *
     * Constructor.
     *
     * @param pa partitioner
     */
    public DataCollectorTreeMap(PartitionerAlgorithm<K, V> pa, CombinerAlgorithm<K, V> combiner, boolean redundantWorkers) {
        dataCollected = new HashMap<>(pa.getMaxIndexPartition() + 1, 1);

        this.partitioner = pa;
        this.combiner = combiner;
        if (redundantWorkers){
        	super.enableHash();
        }

    }

    /**
     * *
     * Add data to the collector.
     *
     * @param data The data to add.
     */
    @Override
    public void collect(K mKey, V mValue) {
        super.collect(mKey, mValue);

    	
        int index = partitioner.partition(mKey);
        TreeMap<K, V> tree = dataCollected.get(index);

        if (tree == null) {
            tree = new TreeMap<>();
            dataCollected.put(index, tree);
        }

        V old = null;

        old = tree.get(mKey);
        if (old != null) {
            V valueCombined = combiner.combine(old, mValue);

            // no combiner and old value exist so we must save all value
            if (valueCombined == null) {
                if (dataCollectedMultipleValue == null) {
                    dataCollectedMultipleValue = new HashMap<>(partitioner.getMaxIndexPartition() + 1, 1);
                }
                TreeMap<K, ArrayList<V>> treeMultipleValue = dataCollectedMultipleValue.get(index);
                if (treeMultipleValue == null) {
                    treeMultipleValue = new TreeMap<>();
                    dataCollectedMultipleValue.put(index, treeMultipleValue);
                }
                ArrayList<V> listeValue = treeMultipleValue.get(mKey);
                if (listeValue == null) {
                    listeValue = new ArrayList<>();
                    treeMultipleValue.put(mKey, listeValue);
                }
                listeValue.add(mValue);
                listeValue.add(old);
                tree.remove(mKey);

                // combiner and old value exist so combine and store
            } else {
                tree.put(mKey, valueCombined);
            }
            // no old value in the first hashmap, maybe in the second
        } else {
            // not in the second
            if (dataCollectedMultipleValue == null) {
                tree.put(mKey, mValue);
            } else {
                TreeMap<K, ArrayList<V>> multipleValueHashmap = dataCollectedMultipleValue.get(index);
                if (multipleValueHashmap == null) {
                    tree.put(mKey, mValue);
                } else {
                    ArrayList<V> valueList = multipleValueHashmap.get(mKey);
                    if (valueList == null) {
                        tree.put(mKey, mValue);
                    } else {
                        valueList.add(mValue);
                    }
                }
            }
        }
    }

    /**
     * *
     * Get data collected back.
     *
     * @return -
     */
    public HashMap<Integer, TreeMap<K, V>> getData() {
        return dataCollected;
    }

    /**
     * Write intermediate key value pairs on the hdd. A mapper has his own
     * folder inside a "map" folder. He write a file per partition.
     *
     * @param id the identification of the mapper
     */
    public void writeIntermediateKeyValue(int id) {
        /*
         * File mapfile = new File("map"); if (!mapfile.exists()) {
         * mapfile.mkdir(); }
         *
         * File file = new File("map" + "/" + Integer.toString(id)); try {
         * file.mkdirs(); ObjectOutputStream stream; TreeMap<K, Tuple<K, V>>
         * tree; for (int i = 0; i < partitioner.getMaxIndexPartition(); i++) {
         * tree = dataCollected.get(i); if (!tree.isEmpty()) { stream = new
         * ObjectOutputStream(new BufferedOutputStream(new
         * FileOutputStream("map" + "/" + Integer.toString(id) + "/" +
         * Integer.toString(i) + ".txt")));
         * stream.writeObject(dataCollected.get(i)); stream.close(); } } } catch
         * (IOException e) { e.printStackTrace();
        }
         */
    }

    /**
     * Read the intermediate key value pair from the hdd (merge all list of the
     * same partition)
     *
     * @param partition the number of the partition to read
     * @param totalMapper the total number of mappers
     * @return an arrayList of all intermediate key value pair for the given
     * partition
     */
    /*
     * @SuppressWarnings({"unchecked"}) public ArrayList<Tuple<K, V>>
     * readFromDisk(int partition, int totalMapper) { ObjectInputStream stream =
     * null; TreeMap<K, Tuple<K, V>> tree = new TreeMap<K, Tuple<K, V>>(); File
     * file;
     *
     * for (int i = 0; i < totalMapper; i++) { file = new File("map" + "/" +
     * Integer.toString(i) + "/" + Integer.toString(partition) + ".txt"); if
     * (file.exists() && file.canRead()) { try { stream = new
     * ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
     * tree = (TreeMap<K, Tuple<K, V>>) stream.readObject(); stream.close();
     * file.delete(); } catch (FileNotFoundException e) { e.printStackTrace(); }
     * catch (IOException e) { e.printStackTrace(); } catch
     * (ClassNotFoundException e) { e.printStackTrace(); } } } return new
     * ArrayList<Tuple<K, V>>(tree.values());
    }
     */
    public void print() {
        System.out.println(dataCollected);
    }

    @Override
    public void clearCollector() {
        dataCollected.clear();
        if (dataCollectedMultipleValue != null) {
            dataCollectedMultipleValue.clear();
        }
        dataCollected = null;
        dataCollectedMultipleValue = null;
    }

    @Override
    public ArrayList<Tuple<K, V>> getPartition(int index) {
        TreeMap<K, V> tmp = dataCollected.get(index);

        if (tmp == null) {
            return null;
        }

        ArrayList<Tuple<K, V>> liste = new ArrayList<>();
        Set<K> keySet = tmp.keySet();
        for (K key : keySet) {
            liste.add(new Tuple<>(key, tmp.get(key)));
        }

        if (dataCollectedMultipleValue != null) {
            TreeMap<K, ArrayList<V>> tmpList = dataCollectedMultipleValue.get(index);
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
