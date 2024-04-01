package sg.edu.smu.cs205g2t7;

/**
 * A class that keeps track of time deltas between calls to its progress() method.
 */
public class ElapsedTimer {
    private long updateStartTime = 0L;

    private boolean initialized = false;

    public long getUpdateStartTime() {
        return updateStartTime;
    }

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
