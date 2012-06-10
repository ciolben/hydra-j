package epfl.project.common;

import java.util.ArrayList;

/**
 * Models a tuple like (x, y).
 *
 * @param <A> First value in the tuple.
 * @param <B> Second value in the tuple.
 *
 */
public class Tuple<A extends Comparable<A>, B> implements Comparable<Object>, KeyValue<A, B> {

    private A mKey;
    private B mValue;
    private ArrayList<B> listValue;

    private Tuple(){};//used to permit write/read to disk

    /**
     * Constructor of the tuple.
     *
     * @param user The x component.
     * @param value The y component.
     */
    public Tuple(A user, B value) {
        mKey = user;
        mValue = value;
    }

    public synchronized void addValueList(B value) {
        if (listValue == null) {
            listValue = new ArrayList<>();
        }

        listValue.add(value);
    }

    public synchronized void setKey(A key) {
        this.mKey = key;
    }

    /**
     * Get the key of the tuple.
     *
     * @return The key.
     */
    public synchronized A getKey() {
        return mKey;
    }

    /**
     * Get the value of the tuple.
     *
     * @return The value.
     */
    public synchronized B getValue() {
        return mValue;
    }

    /**
     * Get the list of value of the tuple.
     *
     * @return The value.
     */
    public synchronized ArrayList<B> getValueList() {
        return listValue;
    }

    /**
     * Redefined method compareTo to enable sorting by key.
     *
     * @param o Object to compare to this.
     * @return -1 if less than, 0 if equal, 1 if greater than. -2 if not
     * comparable
     */
    @Override
    public synchronized int compareTo(Object o) {
        if (o != null && o instanceof Tuple<?, ?>) {
            Tuple<A, B> tuple;
            try {
                tuple = (Tuple<A, B>) o;
            } catch (ClassCastException exception) {
                return -2;
            }
            A key = tuple.getKey();
            int compResult = key.compareTo(mKey);
            return compResult;
        }
        return -2;
    }

    @Override
    public String toString() {
        return "( " + mKey + " , " + mValue + " )";
    }
    
}
