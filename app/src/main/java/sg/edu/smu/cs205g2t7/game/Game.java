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
import sg.edu.smu.cs205g2t7.utils.Counter;
import sg.edu.smu.cs205g2t7.utils.DeltaStepper;
import sg.edu.smu.cs205g2t7.utils.ElapsedTimer;
import sg.edu.smu.cs205g2t7.utils.Movement;
import sg.edu.smu.cs205g2t7.utils.NotificationPublisher;
import sg.edu.smu.cs205g2t7.utils.SwiperExecutorPool;

public class Game {
    private final static int targetFps = 10;
    private final static long intervalFps = 1000L;
    private final static long intervalUps = 1000L;

    private final int level;

    private final int targetLevel;
    private final Context context;
    private final SurfaceHolder holder;
    private final Object mutex = new Object();
    private final SwiperExecutorPool pool = new SwiperExecutorPool();
    private final int numColumns = 5;
    private final int numRows = 8;
    private final Coordinates playerCoords = new Coordinates(0, 0);
    private Coordinates crateCoords;
    private final Coordinates endCoords = new Coordinates(4, 7);
    private List<Coordinates> obstacles;
    private final Counter frameCounter = new Counter();
    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    private final Paint fpsText = new Paint();
    private final Paint circlePaint = new Paint();
    private final Paint circleOutlinePaint = new Paint();
    private final Paint tickPaint = new Paint();
    private final Paint handPaint = new Paint();
    private final Paint spinnerPaint = new Paint();
    private final Drawable endFlag;
    private final Drawable crate;
    private final Drawable background;
    private final Drawable logo;
    PlayerRecordDbHelper dbHelper;
    private Drawable player;
    private int width = 0;
    private int height = 0;
    private long startTime;
    private double avgFps = 0.0;
    private final DeltaStepper fpsUpdater = new DeltaStepper(intervalFps, this::fpsUpdate);
    private int secondCount = 0;

    private float spinner = 0.0f;

    private boolean finished = false;
    private final DeltaStepper upsUpdater = new DeltaStepper(intervalUps, this::upsUpdate);
    private boolean showFps;

    public Game(Context viewContext, SurfaceHolder holder, int level, int targetLevel) {
        this.context = viewContext;
        this.dbHelper = new PlayerRecordDbHelper(context);
        this.holder = holder;
        this.endFlag = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.execavator, null);
        this.player = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.still_down, null);
        this.level = level;
        this.targetLevel = targetLevel;
        this.crate = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.crate, null);
        this.logo = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.logo_small, null);
        this.background = ResourcesCompat.getDrawable(context.getResources(), R.drawable.background, null);

        randomizeCrateAndConesLocation();

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

    private void randomizeCrateAndConesLocation() {
        Random random = new Random();
        random.setSeed(level);

        crateCoords = new Coordinates(random.nextInt(numColumns - 2) + 1, random.nextInt(numRows - 2) + 1);

        obstacles = List.of(
                new Coordinates(random.nextInt(numColumns - 2) + 1, random.nextInt(numRows - 2) + 1),
                new Coordinates(random.nextInt(numColumns - 2) + 1, random.nextInt(numRows - 2) + 1)
        );
    }

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

    public void toggleFps() {
        this.showFps = !this.showFps;
    }

    /**
     * move player, if crate is in the way, player will push it
     * Vibrate if player is going out of bounds
     *
     * @param xDelta +- 1
     * @param yDelta +- 1
     */
    private void movePlayerAndCrates(int xDelta, int yDelta, Movement movement) {
        Coordinates nextCoord = playerCoords.clone(playerCoords.x + xDelta, playerCoords.y + yDelta);
        // player cannot go out of bounds
        if (isOutOfBounds(nextCoord)) {
            Vibrator v = getSystemService(context, Vibrator.class);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            return;
        }

        // player cannot overlap with crates
        if (nextCoord.equals(crateCoords) && isOutOfBounds(crateCoords.clone(crateCoords.x + xDelta, crateCoords.y + yDelta))) {
            Vibrator v = getSystemService(context, Vibrator.class);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            return;
        }

        // player cannot overlap with end
        if (overlapsWithEnd(nextCoord)) {
            Vibrator v = getSystemService(context, Vibrator.class);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            return;
        }

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

        playerCoords.x += xDelta;
        playerCoords.y += yDelta;

        // Push crate
        if (nextCoord.equals(crateCoords) && !isOutOfBounds(crateCoords.clone(crateCoords.x + xDelta, crateCoords.y + yDelta))) {
            crateCoords.x += xDelta;
            crateCoords.y += yDelta;
            Log.d("CrateCoordinates", crateCoords.x + "," + crateCoords.y);
        }
        // Pass level
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

    private boolean atCorner(Coordinates coordinates) {
        return (coordinates.x == 0 || coordinates.x == numColumns - 1) && (coordinates.y == 0 || coordinates.y == numRows - 1);
    }

    private boolean isOutOfBounds(Coordinates coord) {
        return coord.x < 0 || coord.x >= numColumns || coord.y < 0 || coord.y >= numRows || obstacles.contains(coord);
    }

    private boolean overlapsWithEnd(Coordinates playerCoord) {
        return playerCoord.equals(endCoords);
    }

    private void sendNotification(String title, String message) {
        NotificationPublisher.showNotification(context, title, message);
    }

    private void useCanvas(final Consumer<Canvas> onDraw) {
        try {
            final Canvas canvas = holder.lockCanvas();
            try {
                onDraw.accept(canvas);
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (final IllegalStateException e) {
                    // Do nothing
                }
            }
        } catch (final IllegalArgumentException e) {
            Log.e(getClass().getSimpleName(), "Unexpected err occurred");
        }
    }

    public void draw() {
        useCanvas(this::draw);
    }

    @SuppressLint("DefaultLocale")
    private void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        synchronized (mutex) {
            canvas.drawColor(Color.BLACK);
            int xOffset = 5;
            int yOffset = 330;
            int cellWidth = width / numColumns;
            int cellHeight = (height - yOffset) / numRows;
            // Background and logo
            background.setBounds(new Rect(0, 0, width, height));
            background.draw(canvas);

            logo.setBounds(new Rect(width / 2 - 300, 0, width/2 + 300, 300));
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
        // Draw the frame-rate counter.
        {
            if (showFps) {
                canvas.drawText(String.format("%.2f", avgFps), 10.0f, 30.0f, fpsText);
            }
        }

    }

    public void onDraw(int width, int height) {
        this.width = width;
        this.height = height;
        this.startTime = System.currentTimeMillis();
    }

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

    private boolean fpsUpdate(long deltaTime) {
        final double fractionTime = intervalFps / (double) deltaTime;
        avgFps = frameCounter.getValue() * fractionTime;
        return false;
    }

    public long getSleepTime() {
        final double targetFrameTime = (1000.0 / targetFps);
        final long updateEndTime = System.currentTimeMillis();
        final long updateTime = updateEndTime - elapsedTimer.getUpdateStartTime();
        return Math.round(targetFrameTime - updateTime);
    }

    public void update() {
        final long deltaTime = elapsedTimer.progress();
        if (deltaTime <= 0) {
            return;
        }
        // Step updates.
        upsUpdater.update(deltaTime);
        fpsUpdater.update(deltaTime);
        // Immediate updates.
        spinner += (deltaTime / (60.0f * 1000.0f)) * 360.0f;
    }
}
