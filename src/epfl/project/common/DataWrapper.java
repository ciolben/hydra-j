package epfl.project.common;

/**
 * *
 *
 * class : DataWrapper
 *
 * Object for manipulating data type. Implementation choice : user customizable
 * ? more public method for data exchange between workers ?
 *
 */
public class DataWrapper implements Cloneable {
    //array

    private byte[] dataByteArray = null;
    private int[] dataIntArray = null;
    private double[] dataDoubleArray = null;
    private long[] dataLongArray = null;
    private char[] dataCharArray = null;
    private String[] dataStringArray = null;
    //simple value
    private byte dataByte;
    private int dataInt;
    private double dataDouble;
    private long dataLong;
    private char dataChar;
    private String dataString;
    private int progressPercentage;

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (byte[]) to wrap.
     */
    public void wrapByteArray(byte[] dataByteArray) {
        this.dataByteArray = dataByteArray;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (int[]) to wrap.
     */
    public void wrapIntArray(int[] dataIntArray) {
        this.dataIntArray = dataIntArray;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (double[]) to wrap.
     */
    public void wrapDoubleArray(double[] dataDoubleArray) {
        this.dataDoubleArray = dataDoubleArray;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (long[]) to wrap.
     */
    public void wrapLongArray(long[] dataLongArray) {
        this.dataLongArray = dataLongArray;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (char[]) to wrap.
     */
    public void wrapCharArray(char[] dataCharArray) {
        this.dataCharArray = dataCharArray;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (String[]) to wrap.
     */
    public void wrapStringArray(String[] dataStringArray) {
        this.dataStringArray = dataStringArray;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (byte) to wrap.
     */
    public void wrapByte(byte dataByte) {
        this.dataByte = dataByte;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (int) to wrap.
     */
    public void wrapInt(int dataInt) {
        this.dataInt = dataInt;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (double) to wrap.
     */
    public void wrapDouble(double dataDouble) {
        this.dataDouble = dataDouble;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (long) to wrap.
     */
    public void wrapLong(long dataLong) {
        this.dataLong = dataLong;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (char) to wrap.
     */
    public void wrapChar(char dataChar) {
        this.dataChar = dataChar;
    }

    /**
     * *
     * Add data to the wrapper.
     *
     * @param data The data (String) to wrap.
     */
    public void wrapString(String dataString) {
        this.dataString = dataString;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public byte unwrapByte() {
        return dataByte;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public byte[] unwrapByteArray() {
        return dataByteArray;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public int[] unwrapIntArray() {
        return dataIntArray;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public double[] unwrapDoubleArray() {
        return dataDoubleArray;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public long[] unwrapLongArray() {
        return dataLongArray;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public char[] unwrapCharArray() {
        return dataCharArray;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public String[] unwrapStringArray() {
        return dataStringArray;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public int unwrapInt() {
        return dataInt;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public double unwrapDouble() {
        return dataDouble;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public long unwrapLong() {
        return dataLong;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public char unwrapChar() {
        return dataChar;
    }

    /**
     * *
     * Get data back from wrapper.
     *
     * @return The unwrapped data.
     */
    public String unwrapString() {
        return dataString;
    }

    @Override
    public DataWrapper clone() {
        DataWrapper clone = null;
        try {
            clone = (DataWrapper) super.clone();

            if (dataByteArray != null) {
                clone.dataByteArray = dataByteArray.clone();
            }
            if (dataIntArray != null) {
                clone.dataIntArray = dataIntArray.clone();
            }
            if (dataDoubleArray != null) {
                clone.dataDoubleArray = dataDoubleArray.clone();
            }
            if (dataLongArray != null) {
                clone.dataLongArray = dataLongArray.clone();
            }
            if (dataCharArray != null) {
                clone.dataCharArray = dataCharArray.clone();
            }
            if (dataStringArray != null) {
                clone.dataStringArray = dataStringArray.clone();
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }
    
    /**
     * Indicates the position of this data in the stream that is read.
     * @param progressPercentage must be between 0 and 100.
     */
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    /**
     * Get the position of this data in the stream that is read.
     * @return a percentage value between 0 and 100.
     */
    public int getProgressPercentage() {
        return progressPercentage;
    }
}
