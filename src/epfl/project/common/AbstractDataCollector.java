package epfl.project.common;

import epfl.project.sense.MRStats;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Abstract class that must be implemented by all data collector class
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractDataCollector<K extends Comparable<K>, V>
        implements DataCollector<K, V> {

    private MessageDigest hash = null;
    private byte[] hashB = null;
    private boolean redundatEnabled = false;
    
    private long kvCounter = 0;
    private boolean enableProbe = false;

    /**
     * cleaning code, destroy the hashmap of the collector
     */
    public abstract void clearCollector();
    
    /**
     * Create the Result directory, in which each reducer will write their
     * result. Finally, each result will be merged
     */
    protected synchronized void createResultDirectory() {
        File resultFile = new File("Result");
        if (!resultFile.exists()) {
            resultFile.mkdir();
        }
    }
    
    /**
     * Extract the partition from the collector
     *
     * @param index an index
     * @return the partition corresponding to the index
     */
    public abstract ArrayList<Tuple<K, V>> getPartition(int index);

    /**
     * Has to be called by subclasses using super.
     * @param mKey
     * @param mValue 
     */
    @Override
    public void collect(K mKey, V mValue) {

        //**************STATS**************
        if (enableProbe) {
            kvCounter++;
        }
        //MRStats.getInstance().getKVProbe().incrementRecord(0, true);
        //*********************************
        
        if (redundatEnabled) {
            hash.update((byte) mKey.hashCode());
            hash.update((byte) mValue.hashCode());
        }
    }
    
    protected void enableHash() {
        try {
            hash = MessageDigest.getInstance("MD5");
            redundatEnabled = true;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] getHash() {
        if (hashB == null && hash != null) {
            hashB = hash.digest();
        }
        return hashB;
    }
    
    @Override
	public String toString() {
		return Integer.toHexString(hashCode());
    }

    /**
     * Returns the counter of the key/value pair and update the probe record.
     * @return the kvCounter
     */
    public long getKvCounter() {
        MRStats.getInstance().getKVProbe().addRecord((byte) 0, kvCounter);
        return kvCounter;
    }

    /**
     * @param enableProbe the enableProbe to set
     */
    public void setEnableProbe(boolean enableProbe) {
        this.enableProbe = enableProbe;
    }
}
