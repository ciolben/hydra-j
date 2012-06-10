package epfl.project.mapreduce;

import epfl.project.common.DataCollector;
import epfl.project.common.DataWrapper;

/***
 * 
 * interface : Mapper
 * 
 * A Mapper object provides the map function of the mapreduce task.
 *
 * @param <Key> 	-
 * @param <Value>	-
 */
public interface Mapper<Key extends Comparable<Key>, Value> {
	public void map(DataWrapper data, DataCollector<Key, Value> collector);
}
