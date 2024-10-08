package sg.edu.smu.cs205g2t7.game;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import sg.edu.smu.cs205g2t7.R;
import sg.edu.smu.cs205g2t7.db.PlayerRecordDbHelper;
import sg.edu.smu.cs205g2t7.end.EndGameActivity;
import sg.edu.smu.cs205g2t7.utils.Coordinates;
import sg.edu.smu.cs205g2t7.utils.DeltaStepper;
import sg.edu.smu.cs205g2t7.utils.ElapsedTimer;
import sg.edu.smu.cs205g2t7.utils.Movement;
import sg.edu.smu.cs205g2t7.utils.NotificationPublisher;
import sg.edu.smu.cs205g2t7.utils.SwiperExecutorPool;

public class Game {
    /** Frames per second */
    private final static int targetFps = 30;
    /** Interval between each movement */
    private final static long intervalUps = 1000L;
    /** Current Game instance Level */
    private final int level;
    /** Last game instance level  */
    private final int targetLevel;
    /** Application Context */
    private final Context context;
    /** To lock and unlock the canvas for drawing */
    private final SurfaceHolder holder;
    /** Mutex associated with drawing on the canvas */
    private final Object mutex = new Object();
    /** Pool of threads that execute an action */
    private final SwiperExecutorPool pool = new SwiperExecutorPool();
    /** Dimensions of game - width */
    private final int numColumns = 5;
    /** Dimensions of game - height */
    private final int numRows = 8;
    /** Starting coordinates of player */
    private final Coordinates playerCoords = new Coordinates(0, 0);
    /** Coordinates of crate, generated randomly and updated throughout the game */
    private Coordinates crateCoords;
    /** Coordinates of destination, destination is fixed (bottom right of screen) */
    private final Coordinates endCoords = new Coordinates(4, 7);
    /** Coordinates of obstacles */
    private List<Coordinates> obstacles;
    /** Stores the time taken for the game */
    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    /** Frame rate counter text */
    private final Paint fpsText = new Paint();
    /** Timer Counter */
    private final Paint circlePaint = new Paint();
    /** Outline of timer */
    private final Paint circleOutlinePaint = new Paint();
    /** Outline of clock ticks */
    private final Paint tickPaint = new Paint();
    /** Outline of seconds hand */
    private final Paint handPaint = new Paint();
    /** Outline of progression arch */
    private final Paint spinnerPaint = new Paint();
    /** End flag icon */
    private final Drawable endFlag;
    /** Crate icon */
    private final Drawable crate;
    /** Game background */
    private final Drawable background;
    /** App logo */
    private final Drawable logo;
    /** Database service class defined in package sg.smu.cs205g2t7.db */
    PlayerRecordDbHelper dbHelper;
    /** Player icon */
    private Drawable player;
    /** Canvas width */
    private int width = 0;
    /** Canvas Height */
    private int height = 0;
    /** start time in milliseconds */
    private long startTime;
    /** time elapsed since start */
    private int secondCount = 0;
    /** Specifies the sweep angle */
    private float spinner = 0.0f;
    /** Flag when the game is completed. */
    private boolean finished = false;
    /** Provides the minimum timedelta to perform an update */
    private final DeltaStepper upsUpdater = new DeltaStepper(intervalUps, this::upsUpdate);

    /**
     * Instantiates the game on the current app context, current view and player name.
     * It sets the text for the frame-rate counter, sets the background style,
     * sets the seconds lines, sets the seconds hand, and sets the progression arch
     * @param viewContext app context
     * @param holder the holder for a view
     * @param level current level index, starts from 1
     * @param targetLevel final level index, cannot be smaller than level
     */
    public Game(Context viewContext, SurfaceHolder holder, int level, int targetLevel) {
        this.context = viewContext;
        this.dbHelper = new PlayerRecordDbHelper(context);
        this.holder = holder;
        this.endFlag = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.execavator, null);
        this.player = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.still_down, null);
        this.level = level;
        if (targetLevel < level) {
            throw new IllegalArgumentException("level cannot be smaller than target level");
        }
        this.targetLevel = targetLevel;
        this.crate = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.crate, null);
        this.logo = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.logo_small, null);
        this.background = ResourcesCompat.getDrawable(context.getResources(), R.drawable.background, null);

        randomizeCrateAndConesLocation();
        setClockStyle();
    }
    /**
     * Sets styles for the countdown timer
     */
    private void setClockStyle() {
        // Set the text for a frame-rate counter.
        {
            fpsText.setColor(Color.rgb(200, 200, 200));
            fpsText.setTextSize(40.0f);
        }
        // Set the background style.
        {
            circlePaint.setColor(Color.rgb(21, 28, 85));
            circleOutlinePaint.setColor(Color.WHITE);
        }
        // Set the seconds lines.
        {
            tickPaint.setColor(Color.rgb(255, 255, 255));
            tickPaint.setAntiAlias(true);
            tickPaint.setStrokeWidth(1);
            tickPaint.setStyle(Paint.Style.STROKE);
        }
        // Set the seconds hand.
        {
            handPaint.setColor(Color.rgb(198, 146, 0));
            handPaint.setAntiAlias(true);
            handPaint.setStrokeWidth(5);
            handPaint.setStyle(Paint.Style.STROKE);
        }
        // Set the progression arch.
        {
            spinnerPaint.setColor(Color.rgb(198, 146, 0));
            spinnerPaint.setAntiAlias(true);
            spinnerPaint.setStrokeWidth(7);
            spinnerPaint.setStyle(Paint.Style.STROKE);
        }
    }
    /**
     * For a new level, create a randomized layout of the crates and obstacles (cones)
     * This method will populate the crateCoords and obstacles.
     */
    private void randomizeCrateAndConesLocation() {
        Random random = new Random();
        random.setSeed(level);

        crateCoords = new Coordinates(random.nextInt(numColumns - 2) + 1, random.nextInt(numRows - 2) + 1);

        obstacles = List.of(
                new Coordinates(random.nextInt(numColumns - 2) + 1, random.nextInt(numRows - 2) + 1),
                new Coordinates(random.nextInt(numColumns - 2) + 1, random.nextInt(numRows - 2) + 1)
        );
    }
    /**
     * Event for the swipeRight action
     * Each time the user swipes right, the main thread submits a swipeRight event
     * into the worker SwipeExecutorThread pool for one of the worker Threads to process.
     * The player coordinates are updated at movePlayerAndCrates by 1 unit to the right,
     * and the player icon is redrawn at those coordinates
     */
    public void swipeRight() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(1, 0, Movement.RIGHT);
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.still_right, null);
        });
    }
    /**
     * Event for the swipeRight action
     * Each time the user swipes right, the main thread submits a swipeRight event
     * into the worker SwipeExecutorThread pool for one of the worker Threads to process.
     * The player coordinates are updated at movePlayerAndCrates by 1 unit to the right,
     * and the player icon is redrawn at those coordinates
     */
    public void swipeLeft() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(-1, 0, Movement.LEFT);
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.still_left, null);
        });
    }
    /**
     * Event for the swipeUp action. This is similar to the swipeLeft and swipeRight actions
     */
    public void swipeUp() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(0, -1, Movement.UP);
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.still_up, null);
        });
    }
    /**
     * Event for the swipeDown action. This is similar to the swipeLeft and swipeRight actions
     */
    public void swipeDown() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(0, 1, Movement.DOWN);
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.still_down, null);
        });
    }

    /**
     * Updates the player coordinates.
     * If is in the way, player will push the crate.
     * Vibrate if player is going out of bounds
     *
     * @param xDelta value should be +1 or -1
     * @param yDelta value should be +1 or -1
     */
    private void movePlayerAndCrates(int xDelta, int yDelta, Movement movement) {
        Coordinates nextCoord = playerCoords.clone(playerCoords.x + xDelta, playerCoords.y + yDelta);
        if (isInvalidMovement(nextCoord, xDelta, yDelta)) {
            Vibrator v = getSystemService(context, Vibrator.class);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            return;
        }

        // Move player
        playerCoords.x += xDelta;
        playerCoords.y += yDelta;

        changePlayerResource(movement);

        // Push crate
        if (nextCoord.equals(crateCoords) && !isOutOfBounds(crateCoords.clone(crateCoords.x + xDelta, crateCoords.y + yDelta))) {
            crateCoords.x += xDelta;
            crateCoords.y += yDelta;
            Log.d("CrateCoordinates", crateCoords.x + "," + crateCoords.y);
        }

        // Pass level
        passLevel();
    }
    /**
     * This method is called when the user passes the level.
     * The game has two levels.
     * - At the end of the first level,
     * the player would be directed to the second
     * level immediately.
     * - At the end of the second level, the player would be
     * directed to the victory screen defined in EndGameActivity.
     */
    private void passLevel() {
        if (crateCoords.equals(endCoords)) {
            if (level == targetLevel) {
                double seconds = (System.currentTimeMillis() - startTime) / 1000.0;
                dbHelper.storeRecord(seconds);
                sendNotification("Yay", "You got the Core of Harmony!");
                Intent intent = new Intent(context, EndGameActivity.class);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, GameActivity.class);
                intent.putExtra("level", level + 1);
                context.startActivity(intent);
            }
            ((Activity) context).finish();
            // Reset level
        } else if (atCorner(crateCoords)) {
            sendNotification("Oh no", "You got stuck! Exiting..");
            ((Activity) context).finish();
        }
    }
    /**
     * For a particular movement type, draw the player moving in that direction specifically.
     * This is reflected in the person icon facing left when travelling left, or facing right
     * when travelling right, etc.
     * @param movement the direction of movement defined in sg.edu.cs205g2t7.utils.Movement
     */
    private void changePlayerResource(Movement movement) {
        switch (movement) {
            case UP ->
                    player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.moving_up, null);
            case DOWN ->
                    player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.moving_down, null);
            case LEFT ->
                    player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.moving_left, null);
            case RIGHT ->
                    player = ResourcesCompat.getDrawable(context.getResources(), R.drawable.moving_right, null);
        }
    }
    /**
     * Checks if a movement is within the boundaries of the screen or not.
     * @param playerNextCoord next coordinate of player
     * @param xDelta horizontal displacement of user from current position
     * @param yDelta vertical displacement of user from current position
     * @return true if movement is invalid and false otherwise.
     */
    private boolean isInvalidMovement(Coordinates playerNextCoord, int xDelta, int yDelta) {
        return isOutOfBounds(playerNextCoord)  // player cannot go out of bounds
                || (playerNextCoord.equals(crateCoords) && isOutOfBounds(crateCoords.clone(crateCoords.x + xDelta, crateCoords.y + yDelta))) // player cannot overlap with crates
                || overlapsWithEnd(playerNextCoord); // player cannot overlap with end
    }
    /**
     * Helper method to find if crate is at a corner
     * @param coord one coordinate object (x, y)
     * @return true if the crate is at a corner and false otherwise
     */
    private boolean atCorner(Coordinates coord) {
        return (coord.x == 0 || coord.x == numColumns - 1) && (coord.y == 0 || coord.y == numRows - 1);
    }
    /**
     * Helper method to find if a particular coordinates is out of bounds
     * @param coord one coordinate object (x,y)
     * @return true if the crate is out of bounds and false otherwise.
     */
    private boolean isOutOfBounds(Coordinates coord) {
        return coord.x < 0 || coord.x >= numColumns || coord.y < 0 || coord.y >= numRows || obstacles.contains(coord);
    }
    /**
     * Helper method to find if the player is on the end coordinates
     * @param playerCoord coordinates of the player
     * @return true if the player is on the end coordinates, and false otherwise
     */
    private boolean overlapsWithEnd(Coordinates playerCoord) {
        return playerCoord.equals(endCoords);
    }
    /**
     * Helper method to find if the player is on the send notifications coordinates
     * @param title notification title
     * @param message notification contents
     */
    private void sendNotification(String title, String message) {
        NotificationPublisher.showNotification(context, title, message);
    }
    /**
     * To use the canvas, a SwipeExecutor worker Thread must acquire the mutex.
     * After it is done, it will unlock it.
     * @param draw a function that takes in a canvas
     *
     * <p>
     * To call this function, you need a function that has the signature
     * <code>void draw(Canvas canvas)</code> and it can be invoked with
     * <code>useCanvas(this::draw)</code>
     */
    private void useCanvas(final Consumer<Canvas> draw) {
        try {
            final Canvas canvas = holder.lockCanvas();
            try {
                draw.accept(canvas);
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (final IllegalStateException | IllegalMonitorStateException e) {
                    // Do nothing
                }
            }
        } catch (final IllegalArgumentException e) {
            Log.e(getClass().getSimpleName(), "Unexpected err occurred");
        }
    }
    /**
     * Draws the whole game
     */
    public void draw() {
        useCanvas(this::draw);
    }

    /**
     * Draw the player, obstacles, crate, background, logo, every obstacle, and the
     * destination.
     * @param canvas canvas to be drawn on
     */
    private synchronized void drawGame(Canvas canvas) {
        synchronized (mutex) {
            canvas.drawColor(Color.BLACK);
            int xOffset = 5;
            int yOffset = 330;
            int cellWidth = width / numColumns;
            int cellHeight = (height - yOffset) / numRows;
            // Background and logo
            background.setBounds(new Rect(0, 0, width, height));
            background.draw(canvas);

            logo.setBounds(new Rect(width / 2 - 300, 0, width / 2 + 300, 300));
            logo.draw(canvas);

            // Draw the Player.
            player.setBounds(new Rect(xOffset + playerCoords.x * cellWidth, yOffset + playerCoords.y * cellHeight, xOffset + (playerCoords.x + 1) * cellWidth, yOffset + (playerCoords.y + 1) * cellHeight));
            player.draw(canvas);

            // Draw Obstacle
            for (Coordinates coneCoords : obstacles) {
                Drawable cone = ResourcesCompat.getDrawable(context.getResources(), R.drawable.safety_cone, null);
                if (cone != null) {
                    cone.setBounds(new Rect(xOffset + coneCoords.x * cellWidth, yOffset + coneCoords.y * cellHeight, xOffset + (coneCoords.x + 1) * cellWidth, yOffset + (coneCoords.y + 1) * cellHeight));
                    cone.draw(canvas);
                }
            }
            // Draw Crate
            crate.setBounds(new Rect(xOffset + crateCoords.x * cellWidth, yOffset + crateCoords.y * cellHeight, xOffset + (crateCoords.x + 1) * cellWidth, yOffset + (crateCoords.y + 1) * cellHeight));
            crate.draw(canvas);

            // Draw the end.
            endFlag.setBounds(new Rect(xOffset + endCoords.x * cellWidth, yOffset + endCoords.y * cellHeight, xOffset + (endCoords.x + 1) * cellWidth, yOffset + (endCoords.y + 1) * cellHeight));
            endFlag.draw(canvas);
        }
    }
    /**
     * Draw the timer face on the canvas
     * @param canvas canvas to be drawn on
     */
    private void drawClock(Canvas canvas) {

        final float radius = Math.min(width, height) * 0.10f;
        final float centerWidth = 150;
        final float centerHeight = 130;
        // Draw the face of a clock.
        {
            canvas.drawCircle(centerWidth, centerHeight, radius * 1.02f, circleOutlinePaint);
            canvas.drawCircle(centerWidth, centerHeight, radius, circlePaint);
        }
        // Draw the center of a clock.
        {
            canvas.drawCircle(centerWidth, centerHeight, radius * 0.01f, tickPaint);
        }
        // Draw minute ticks.
        {
            final float start = radius * 0.85f;
            final float end = radius * 0.88f;
            for (int i = 0; i < 60; ++i) {
                final double angle = (Math.PI * 2.0) * (i / 60.0);
                final float x = (float) Math.sin(angle);
                final float y = (float) Math.cos(angle);
                canvas.drawLine(centerWidth + (x * start), centerHeight - (y * start), centerWidth + (x * end), centerHeight - (y * end), tickPaint);
            }
        }
        // Draw the seconds' hand.
        {
            final float start = radius * 0.05f;
            final float end = radius * 0.9f;
            final double angle = (Math.PI * 2.0) * (secondCount / 60.0);
            final float x = (float) Math.sin(angle);
            final float y = (float) Math.cos(angle);
            canvas.drawLine(centerWidth + (x * start), centerHeight - (y * start), centerWidth + (x * end), centerHeight - (y * end), handPaint);
        }
        // Draw the progressing arch.
        {
            final float d = radius * 0.95f;
            canvas.drawArc(centerWidth - d, centerHeight - d, centerWidth + d, centerHeight + d, -90, spinner, false, spinnerPaint);
        }
        {
            Paint paint = new Paint();
            paint.setTextSize(48);
            paint.setColor(Color.WHITE);
        }
    }
    /**
     * Draws the background, logo, player, obstacles, and crate
     * based of their updated positions. A thread is only able to draw
     * after it acquires the mutex.
     * @param canvas canvas to be drawn on
     */  
    @SuppressLint("DefaultLocale")
    private void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        drawGame(canvas);
        drawClock(canvas);
    }
    /**
     * Initialize the width, height of the canvas and start time
     * @param width - canvas width
     * @param height - canvas height
     */
    public void onDraw(int width, int height) {
        this.width = width;
        this.height = height;
        this.startTime = System.currentTimeMillis();
    }
    /**
     * A function indicating a single time step, used by the upsUpdater.
     * It updates the timer if the timer is less than 60 seconds
     * Otherwise it will send a notification that the time has exceeded.
     * NOTE: The deltatime input parameter is unused here, but just meant to fit
     * the interface of the upsUpdater
     */
    private boolean upsUpdate(long deltaTime) {
        if (secondCount < 60) {
            ++secondCount;
        }
        if (secondCount == 60) {
            if (!finished) {
                finished = true;
                try {
                    sendNotification("Times up!", "Time limit exceeded");
                    ((Activity) context).finish();
                } catch (final Exception e) {
                    Log.e(getClass().getSimpleName(), "Unexpected err occurred");
                }
                spinnerPaint.setColor(Color.BLACK);
            }
        }
        return true;
    }
    /**
     * Compute the time that the thread sleeps for
     * @return duration in milliseconds of the time to sleep the thread
     */
    public long getSleepTime() {
        final double targetFrameTime = (1000.0 / targetFps);
        final long updateEndTime = System.currentTimeMillis();
        final long updateTime = updateEndTime - elapsedTimer.getUpdateStartTime();
        return Math.round(targetFrameTime - updateTime);
    }
    /**
     * Update the upsUpdater with a deltaTime
     */
    public void update() {
        final long deltaTime = elapsedTimer.progress();
        if (deltaTime <= 0) {
            return;
        }
        // Step updates.
        upsUpdater.update(deltaTime);
        // Immediate updates.
        spinner += (deltaTime / (60.0f * 1000.0f)) * 360.0f;
    }
}
