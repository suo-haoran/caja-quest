package sg.edu.smu.cs205g2t7.utils;


import java.util.function.LongPredicate;

/**
 * A class that defers execution of a step until a time delta exceeds the minimum limit.
 */
public class DeltaStepper {

    private final long deltaLimit;

    private final LongPredicate step;

    private long deltaSum = 0L;

    public DeltaStepper(final long deltaLimit, final LongPredicate step) {
        this.deltaLimit = deltaLimit;
        this.step = step;
    }

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
