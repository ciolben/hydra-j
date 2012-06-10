package epfl.project.mapreduce;

import epfl.project.common.DataCollector;
import java.util.ArrayList;

/***
 * 
 * interface : Reducer
 * 
 * A Reducer object provides the reduce function of a mapreduce task.
 *
 * @param <Key>		-
 * @param <Value>	-
 */
public interface Reducer<Key extends Comparable<Key>, Value, KReduce extends Comparable<KReduce>, VReduce > {
	public void reduce(Key key, ArrayList<Value> valueList, DataCollector<KReduce, VReduce> collector);
}
