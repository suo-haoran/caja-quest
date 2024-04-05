package sg.edu.smu.cs205g2t7.game;


import android.util.Log;

/**
 * A class representing the game loop of the demo.
 */
public class GameThread extends Thread {
    private boolean isRunning = false;

    private final Game game;

    public GameThread(final Game game) {
        this.game = game;
    }

    public void startLoop() {
        isRunning = true;
        start();
    }

    public void stopLoop() {
        isRunning = false;
    }

    @Override
    public void run() {
        super.run();
        while (isRunning) {
            game.draw();
            gameSleep();
            game.update();
        }
    }

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
