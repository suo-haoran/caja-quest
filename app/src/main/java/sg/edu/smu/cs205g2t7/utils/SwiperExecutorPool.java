package sg.edu.smu.cs205g2t7.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that represents a pool of threads for growing radii of background circle shapes.
 */
public class SwiperExecutorPool {

    private final ExecutorService pool;

    public SwiperExecutorPool() {
        final int cpuCores = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        pool = Executors.newFixedThreadPool(cpuCores);
    }

    public void submit(final Runnable task) {
        pool.submit(task);
    }
}

