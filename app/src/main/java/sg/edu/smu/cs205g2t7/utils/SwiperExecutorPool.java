package sg.edu.smu.cs205g2t7.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that represents a pool of threads for growing radii of background circle shapes.
 */
public class SwiperExecutorPool {
    /** Thread Pool of worker SwiperExecutor threads */
    private final ExecutorService pool;
    /**
     * Constructs a SwiperExecutorPool with a number of threads based on the 
     * available CPU cores
     */
    public SwiperExecutorPool() {
        final int cpuCores = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        pool = Executors.newFixedThreadPool(cpuCores);
    }
    /**
     * Submits a task to the thread pool for execution
     * @param task The task to be executed.
     */
    public void submit(final Runnable task) {
        pool.submit(task);
    }
}

