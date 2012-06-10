package epfl.project.algorithm;

/**
 * *
 * class : CombinerAlgorithm
 *
 * The combiner algorithm can be redefined by the user. The combine method must
 * be overridden.
 *
 */
public class DefaultCombinerAlgorithm<Key extends Comparable<Key>, Value>
        implements CombinerAlgorithm<Key, Value> {

    /**
     * *
     * The combine method takes care of merging two data structure.
     */
    @Override
    public Value combine(Value value1, Value value2) {
        return null;
    }

    @Override
    public void reset() {
    }
}
