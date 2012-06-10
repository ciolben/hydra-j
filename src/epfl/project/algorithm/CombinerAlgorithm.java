package epfl.project.algorithm;

/***
 * class : CombinerAlgorithm
 * 
 * The combiner algorithm can be redefined by the user. The
 * combine method must be overridden.
 *
 */
public interface CombinerAlgorithm<Key extends Comparable<Key>, Value> {
	
	/***
	 * The combine method takes care of merging two data structure.
	 */
	public Value combine(Value value1, Value value2);
        /***
         * Reset the algorithm. All fields must be set to initial values.
         */
        public void reset();
}