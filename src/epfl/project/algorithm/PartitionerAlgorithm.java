package epfl.project.algorithm;

/***
 * 
 * class : PartitionerAlgorithm
 * 
 * The partition algorithm can be redefined by overridden the partition
 * function.
 * 
 */
public abstract class PartitionerAlgorithm<Key extends Comparable<Key>, Value> {

        private int maxIndex = 10;

	/***
	 * The partition method by default calculate the part's id by doing the hash
	 * code of the key modulus the number of available processors.
	 * 
	 * @param key
	 *            Only the tuple's key is important.
	 * @return The part number.
	 */
	public abstract int partition(Key key);

	/**
	 * The partition function when usin a common array data collector (the key must be an Integer)
	 * @param key
	 * @return
	 */
	public int partition(Integer key) {
            return Math.abs(key.hashCode() % getMaxIndex());
        }

	public int getMaxIndexPartition() {
            return getMaxIndex();
        }

        public void redefineMaxIndex(int maxIndex) {
            this.setMaxIndex(maxIndex);
        }
        
        /***
         * Reset the algorithm. All fields must be set to initial values.
         */
       public abstract void reset();

        /**
        * @return the maxIndex
        */
        public int getMaxIndex() {
            return maxIndex;
        }

        /**
        * @param maxIndex the maxIndex to set
        */
        public void setMaxIndex(int maxIndex) {
            this.maxIndex = maxIndex;
        }
}
