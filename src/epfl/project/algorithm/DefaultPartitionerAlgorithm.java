package epfl.project.algorithm;

/**
 * *
 *
 * class : PartitionerAlgorithm
 *
 * The partition algorithm can be redefined by overridden the partition
 * function.
 *
 */
public class DefaultPartitionerAlgorithm<Key extends Comparable<Key>, Value> extends PartitionerAlgorithm<Key, Value> {

    public DefaultPartitionerAlgorithm() {
       // max = Runtime.getRuntime().availableProcessors();//32
        setMaxIndex(32);
    }
    
    /**
     * *
     * The partition method by default calculate the part's id by doing the hash
     * code of the key modulus the number of available processors.
     *
     * @param tuple Only the tuple's key is important.
     * @return The part number.
     */
    @Override
    public int partition(Key key) {
        return Math.abs(key.hashCode() % getMaxIndex());
    }

    @Override
    public void reset() {
    }
}
