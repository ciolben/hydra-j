package epfl.project.common;

public interface DataCollector<K extends Comparable<K>, V> {

    /**
     * Add data to the collector.
     *
     * @param data The data to add.
     */
    public void collect(K key, V value);
}
