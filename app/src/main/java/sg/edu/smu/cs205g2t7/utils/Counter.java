package sg.edu.smu.cs205g2t7.utils;

/**
 * A class used as a frame-rate counter.
 */
public class Counter {
    private long value = 0L;
    /**
     * Increments the counter
     */
    public void increment() {
        ++value;
    }
    /**
     * Retrieves value of the counter
     * @return
     */
    public long getValue() {
        final long result = value;
        value = 0L;
        return result;
    }
}
