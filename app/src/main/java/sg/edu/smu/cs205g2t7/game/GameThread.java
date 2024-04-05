package sg.edu.smu.cs205g2t7.game;


import android.util.Log;

/**
 * A class representing the game loop of the demo.
 */
public class GameThread extends Thread {
    /** Flag for thread is running */
    private boolean isRunning = false;
    /** Current game instance */
    private final Game game;
    /**
     * Constructor for the game thread
     * @param game current game instance
     */
    public GameThread(final Game game) {
        this.game = game;
    }
    /**
     * Schedules the thread for execution
     */
    public void startLoop() {
        isRunning = true;
        start();
    }
    /**
     * Stops the current executing thread by setting the isRunning variable to false. See run() method below.
     */
    public void stopLoop() {
        isRunning = false;
    }
    /**
     * Main run method
     */
    @Override
    public void run() {
        super.run();
        while (isRunning) {
            game.draw();
            gameSleep();
            game.update();
        }
    }
    /**
     * Sleep the thread for game.getSleepTime() seconds
     */
    private void gameSleep() {
        long sleepTime = game.getSleepTime();
        if (sleepTime > 0) {
            try {
                sleep(sleepTime);
            } catch (final Exception e) {
                Log.e(this.getName(), "Sleep Error");
            }
        }
    }
}
