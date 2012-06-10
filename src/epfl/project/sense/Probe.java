package epfl.project.sense;

/**
 * Low memory probe.
 * @author Loic
 */
public class Probe {
    private final Object [] records;
    
    /**
     * Constructor of the probe.
     * @param numberOfRecord used to determine the number of different kind
     * of records the probe will handle. If the number is less or equal to
     * zero, the number of record is considered to be one.
     * Note : maximum number of record is though 127.
     */
    public Probe(byte numberOfRecord) {
        if (numberOfRecord < 0) numberOfRecord = 1;
        records = new Object[numberOfRecord];
    }
    
    /**
     * Add a record to the records list and override existing object at this
     * index in a synchronized way.
     * @param index from 0 to 127. If negative, will use index 0. If index
     * doesn't exist, no exception is thrown and nothing is done.
     * @param record the object to store. Object can be null.
     */
    public void addRecord(byte index, Object record) {
        if (index >= records.length) return;
        if (index < 0) index = 0;
        synchronized (records) { records[index] = record; }
    }
    
    /**
     * Get the record at the specified index in a synchronized way.
     * @param index the index of the record to retrieve. If index is negative
     * then index 0 is used.
     * @return the record. <italic>null</italic> if record is null or there is
     * no record at this index.
     */
    public Object getRecord(byte index) {
        if (index >= records.length) return null;
        if (index < 0) index = 0;
        synchronized (records) { return records[index]; }
    }
    
    /**
     * Synchronized method to count things. Any error in the index or trying to
     * increment something not incrementable will have no effect. <br>
     * Note : the object will become an instance of <code>Number</code> if the
     * incrementation succeded.
     * @param index the index of the <code><b>Number</b></code> to increment
     * by one.
     * @param useLong set it to <code>true</code> to keep the precision.
     */
    public void incrementRecord(int index, boolean useLong) {
        if (index >= records.length || index < 0) return;
        synchronized (records) {
            if (records == null || !(records[index] instanceof Number)) return;
            if (useLong) records[index] = ((Number) records[index]).longValue() + 1L;
            else records[index] = ((Number) records[index]).intValue() + 1;
        }
    }
}
