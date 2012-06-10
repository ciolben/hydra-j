package epfl.project.common;

import epfl.project.algorithm.CombinerAlgorithm;
import epfl.project.algorithm.PartitionerAlgorithm;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * *
 *
 * class : DataCollectorArrayList
 *
 * Implementation choice : written on the hdd or in memory note : data should be
 * written in any case on the hdd at some point.
 *
 * Generic arguments: K = Type for the intermediate key (output of the map
 * function) V = Type for the intermediate value (output of the map function)
 */
public class DataCollectorArrayList<K extends Comparable<K>, V> extends AbstractDataCollector<K, V> {

    private HashMap<Integer, Tuple2<ArrayList<K>, ArrayList<V>>> dataCollected;
    private HashMap<Integer, Tuple2<ArrayList<K>, ArrayList<ArrayList<V>>>> dataCollectedMultipleValue;
    private PartitionerAlgorithm<K, V> partitioner;
    private CombinerAlgorithm<K, V> combiner;

    /**
     * *
     * Constructor.
     *
     * @param pa partitioner
     */
    public DataCollectorArrayList(PartitionerAlgorithm<K, V> pa, CombinerAlgorithm<K, V> combiner, boolean redundantWorkers) {
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
        Tuple2<ArrayList<K>, ArrayList<V>> tmp = dataCollected.get(index);



        if (tmp == null) {
            tmp = new Tuple2<>(new ArrayList<K>(), new ArrayList<V>());
            dataCollected.put(index, tmp);
        }
        ArrayList<K> listk = tmp.getA();
        ArrayList<V> listv = tmp.getB();
        boolean found = false;

        int size = listk.size();

        K keyTmp;
        for (int i = 0; i < size && !found; i++) {
            keyTmp = listk.get(i);
            if (keyTmp.equals(mKey)) {
                found = true;

                V combined = combiner.combine(listv.get(i), mValue);

                // no combiner and old value exist so we must save all value
                if (combined == null) {

                    if (dataCollectedMultipleValue == null) {
                        dataCollectedMultipleValue = new HashMap<>(partitioner.getMaxIndexPartition() + 1, 1);
                    }

                    Tuple2<ArrayList<K>, ArrayList<ArrayList<V>>> tuple = dataCollectedMultipleValue.get(index);
                    if (tuple == null) {
                        tuple = new Tuple2<>(new ArrayList<K>(), new ArrayList<ArrayList<V>>());
                        dataCollectedMultipleValue.put(index, tuple);
                    }
                    ArrayList<K> listKey = tuple.getA();
                    ArrayList<ArrayList<V>> listValue = tuple.getB();

                    int sizeListKey = listKey.size();
                    boolean foundMultiple = false;
                    for (int j = 0; j < sizeListKey && !foundMultiple; j++) {
                        K key = listKey.get(j);
                        if (key.equals(mKey)) {
                            foundMultiple = true;
                            ArrayList<V> listeMultipleValue = listValue.get(j);

                            if (listeMultipleValue == null) {
                                listeMultipleValue = new ArrayList<>();
                                listValue.add(listeMultipleValue);
                            }


                            listeMultipleValue.add(mValue);
                            listeMultipleValue.add(listv.get(i));
                            listk.remove(i);
                            listv.remove(i);
                        }
                    }

                    if (!foundMultiple) {
                        listKey.add(mKey);
                        ArrayList<V> newVal = new ArrayList<>();
                        newVal.add(mValue);
                        listValue.add(newVal);
                    }
                    //have a combiner and an old value
                } else {
                    listv.set(i, combined);
                }

            }
        }

        //doesnt found the value in the first list, must check in the liste that contain multiple value
        if (!found) {
            if (dataCollectedMultipleValue == null) {
                listk.add(mKey);
                listv.add(mValue);
            } else {
                Tuple2<ArrayList<K>, ArrayList<ArrayList<V>>> tuple = dataCollectedMultipleValue.get(index);
                if (tuple == null) {
                    listk.add(mKey);
                    listv.add(mValue);
                } else {
                    ArrayList<K> listKey = tuple.getA();
                    ArrayList<ArrayList<V>> listValue = tuple.getB();

                    int sizeKeyListe = listKey.size();
                    boolean foundListe2 = false;
                    for (int i = 0; i < sizeKeyListe && !foundListe2; i++) {
                        if (listKey.get(i).equals(mKey)) {
                            foundListe2 = true;
                            listValue.get(i).add(mValue);
                        }
                    }

                    if (!foundListe2) {
                        listk.add(mKey);
                        listv.add(mValue);
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
        dataCollected.clear();
        if (dataCollectedMultipleValue != null) {
            dataCollectedMultipleValue.clear();
        }
    }

    @Override
    public ArrayList<Tuple<K, V>> getPartition(int index) {
        Tuple2<ArrayList<K>, ArrayList<V>> tmp = dataCollected.get(index);
        if (tmp == null) {
            return null;
        }
        ArrayList<K> tmpk = tmp.getA();

        if (tmpk == null) {
            return null;
        }

        ArrayList<V> tmpv = tmp.getB();

        ArrayList<Tuple<K, V>> list = new ArrayList<>();
        int size = tmpk.size();
        for (int i = 0; i < size; i++) {
            list.add(new Tuple<>(tmpk.get(i), tmpv.get(i)));
        }

        //take also data from the second list
        if (dataCollectedMultipleValue != null) {
            Tuple2<ArrayList<K>, ArrayList<ArrayList<V>>> tmpMultiple = dataCollectedMultipleValue.get(index);
            if (tmpMultiple != null) {
            	ArrayList<K> tmpKey = tmpMultiple.getA();
            	ArrayList<ArrayList<V>> tmpValueList = tmpMultiple.getB();
            	int sizeMultiple = tmpKey.size();
            	for (int i = 0; i < sizeMultiple; i++) {
            		ArrayList<V> valueList = tmpValueList.get(i);
            		int valueListSize = valueList.size();
            		for (int j = 0; j < valueListSize; j++) {
            			list.add(new Tuple<>(tmpKey.get(i), valueList.get(j)));
            		}
            	}
            }
        }

        return list;

    }
}

// not need the "Tuple" class, this class contain only the needed method
class Tuple2<A, B> {

    private A a;
    private B b;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
    }
}