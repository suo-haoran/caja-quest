package sg.edu.smu.cs205g2t7.utils;

/**
 * A class that keeps track of time deltas between calls to its progress() method.
 */
public class ElapsedTimer {
    /** The time when the last progress() call was made */
    private long updateStartTime = 0L;
    /** Indicates if the timer has been initialized */
    private boolean initialized = false;
    /**
     * Get the time when the last progress was made
     * @return The time when the last progress() call was made
     */
    public long getUpdateStartTime() {
        return updateStartTime;
    }
    /**
     * Calculates and returns the time elapsed since the last
     * progress() call, and updates the updateStartTime variable
     * @return time elapsed from the last progress call
     */
    public long progress() {
        final long now = System.currentTimeMillis();
        if (!initialized) {
            initialized = true;
            updateStartTime = now;
        }
        final long delta = now - updateStartTime;
        updateStartTime = now;
        return delta;
    }
}
