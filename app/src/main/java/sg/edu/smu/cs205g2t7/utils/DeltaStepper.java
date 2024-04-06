package sg.edu.smu.cs205g2t7.utils;


import java.util.function.LongPredicate;

/**
 * A class that defers execution of a step until a time delta exceeds the minimum limit.
 */
public class DeltaStepper {
    /** Minimum time difference for a step to execute */
    private final long deltaLimit;
    /** A single ste*/
    private final LongPredicate step;
    /** Accumulated time delta */
    private long deltaSum = 0L;
    /**
     * Constructs a DeltaStepper with a specified delta limit and step function
     * @param deltaLimit minimum time difference for a step to execute
     * @param step A function representing the step to execute
     */
    public DeltaStepper(final long deltaLimit, final LongPredicate step) {
        this.deltaLimit = deltaLimit;
        this.step = step;
    }
    /**
     * Updates the DeltaStepper with the given time delta
     * @param delta The time delta to update the accumulated time delta
     */
    public void update(long delta) {
        deltaSum += delta;
        while (deltaSum > deltaLimit) {
            if (!step.test(deltaSum)) {
                deltaSum %= deltaLimit;
                break;
            }
            deltaSum -= deltaLimit;
        }
    }
}
