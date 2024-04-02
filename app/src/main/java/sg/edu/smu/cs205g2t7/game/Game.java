package sg.edu.smu.cs205g2t7.game;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.Context;
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

import java.util.Locale;
import java.util.function.Consumer;

import sg.edu.smu.cs205g2t7.backgroundservice.NotificationPublisher;
import sg.edu.smu.cs205g2t7.R;
import sg.edu.smu.cs205g2t7.db.PlayerRecordDbHelper;
import sg.edu.smu.cs205g2t7.utils.Coordinates;
import sg.edu.smu.cs205g2t7.utils.Counter;
import sg.edu.smu.cs205g2t7.utils.DeltaStepper;
import sg.edu.smu.cs205g2t7.utils.ElapsedTimer;
import sg.edu.smu.cs205g2t7.utils.SwiperExecutorPool;

public class Game {
    private final static int targetFps = 30;
    private final static long intervalFps = 1000L;
    private final static long intervalUps = 1000L;
    private final Context context;
    private final SurfaceHolder holder;
    //private Circle circle;
    //private Circle end;
    //private Circle crate;
    private final Object mutex = new Object();
    private final SwiperExecutorPool pool = new SwiperExecutorPool();
    private final int numColumns = 5;
    private final int numRows = 5;
    private final int[][] board = new int[numRows][numColumns];
    private final Coordinates playerCoords = new Coordinates(0, 0);
    private final Coordinates crateCoords = new Coordinates(4, 2);
    private final Coordinates endCoords = new Coordinates(4, 4);
    private final Drawable endFlag;
    private final Drawable player;
    private final Drawable crate;
    private final Counter frameCounter = new Counter();
    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    private final Paint fpsText = new Paint();
    private final Paint circlePaint = new Paint();
    private final Paint circleOutlinePaint = new Paint();
    private final Paint tickPaint = new Paint();
    private final Paint handPaint = new Paint();
    private final Paint spinnerPaint = new Paint();
    private final String playerName;
    PlayerRecordDbHelper dbHelper;
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

    public Game(Context viewContext, SurfaceHolder holder, String playerName) {
        this.context = viewContext;
        this.dbHelper = new PlayerRecordDbHelper(context);
        this.holder = holder;
        this.endFlag = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.flag, null);
        this.player = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.player, null);
        this.playerName = playerName;
        this.crate = ResourcesCompat.getDrawable(viewContext.getResources(), R.drawable.crate, null);
        // 2 represents player, 1 represent crate, INT_MIN represents trap
        board[0] = new int[]{2, 0, 0, 0, 0};
        board[1] = new int[]{0, 0, 0, 0, 0};
        board[2] = new int[]{0, 0, Integer.MIN_VALUE, 0, 0}; // trap at 2,2
        board[3] = new int[]{0, 0, 0, 0, 0};
        board[4] = new int[]{0, 0, 0, 1, 0};

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

    public void swipeRight() {
        pool.submit(() -> {
                synchronized (mutex) {
                    movePlayerAndCrates(1, 0);
                }
        });
    }

    public void swipeLeft() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(-1, 0);
            }
        });
    }

    public void swipeUp() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(0, -1);
            }
        });
    }

    public void swipeDown() {
        pool.submit(() -> {
            synchronized (mutex) {
                movePlayerAndCrates(0, 1);
            }
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
    private void movePlayerAndCrates(int xDelta, int yDelta) {
        Coordinates nextCoord =
                playerCoords.clone(playerCoords.x + xDelta, playerCoords.y + yDelta);
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

        board[playerCoords.x][playerCoords.y] -= 2;
        playerCoords.x += xDelta;
        playerCoords.y += yDelta;
        board[playerCoords.x][playerCoords.y] += 2;

        if (nextCoord.equals(crateCoords) && !isOutOfBounds(crateCoords.clone(crateCoords.x + xDelta, crateCoords.y + yDelta))) {
            board[crateCoords.x][crateCoords.y] -= 1;
            crateCoords.x += xDelta;
            crateCoords.y += yDelta;
            board[crateCoords.x][crateCoords.y] += 1;
        }
        if (crateCoords.equals(endCoords)) {
            double seconds = (System.currentTimeMillis() - startTime) / 1000.0;
            dbHelper.storeRecord(seconds);
            sendNotification("Yay", "You win!");
            ((Activity) context).finish();
        }

        if (atCorner(crateCoords)) {
            sendNotification("Oh no", "You got stuck! Exiting..");
            ((Activity) context).finish();
        }
    }

    private boolean atCorner(Coordinates coordinates) {
        return (coordinates.x == 0 || coordinates.x == board[0].length - 1) && (coordinates.y == 0 || coordinates.y == board[0].length - 1);
    }

    private boolean isOutOfBounds(Coordinates coord) {
        return coord.x < 0 || coord.x >= board[0].length || coord.y < 0 || coord.y >= board.length;
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

    private void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        synchronized (mutex) {

            int cellWidth = 200;
            int cellHeight = 200;
            canvas.drawColor(Color.BLACK);

            // Draw the Player.
            final Paint paint = new Paint();
            int xOffset = 5;
            int yOffset = 330;
            player.setBounds(
                    new Rect(
                            xOffset + playerCoords.x * cellWidth,
                            yOffset + playerCoords.y * cellHeight,
                            xOffset + (playerCoords.x + 1) * cellWidth,
                            yOffset + (playerCoords.y + 1) * cellHeight
                    )
            );
            player.draw(canvas);

            // Draw Crate
            crate.setBounds(
                    new Rect(
                            xOffset + crateCoords.x * cellWidth,
                            yOffset + crateCoords.y * cellHeight,
                            xOffset + (crateCoords.x + 1) * cellWidth,
                            yOffset + (crateCoords.y + 1) * cellHeight
                    )
            );
            crate.draw(canvas);

            // Draw the end.
            endFlag.setBounds(
                    new Rect(
                            xOffset + endCoords.x * cellWidth,
                            yOffset + endCoords.y * cellHeight,
                            xOffset + (endCoords.x + 1) * cellWidth,
                            yOffset + (endCoords.y + 1) * cellHeight
                    )
            );
            endFlag.draw(canvas);

            // Draw Grids
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            int width = 1000;
            int height = 1000;

            int i;
            for (i = 0; i <= numColumns; i++) {
                canvas.drawLine(xOffset + i * cellWidth, yOffset, xOffset + i * cellWidth, height + yOffset, paint);
            }

            for (i = 0; i <= numRows; i++) {
                canvas.drawLine(xOffset, i * cellHeight + yOffset, width + xOffset, i * cellHeight + yOffset, paint);
            }
        }

        final float radius = Math.min(width, height) * 0.10f;
        final float centerWidth = 150;
        final float centerHeight = 200;
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
                canvas.drawLine(
                        centerWidth + (x * start),
                        centerHeight - (y * start),
                        centerWidth + (x * end),
                        centerHeight - (y * end),
                        tickPaint
                );
            }
        }
        // Draw the seconds' hand.
        {
            final float start = radius * 0.05f;
            final float end = radius * 0.9f;
            final double angle = (Math.PI * 2.0) * (secondCount / 60.0);
            final float x = (float) Math.sin(angle);
            final float y = (float) Math.cos(angle);
            canvas.drawLine(
                    centerWidth + (x * start),
                    centerHeight - (y * start),
                    centerWidth + (x * end),
                    centerHeight - (y * end),
                    handPaint
            );
        }
        // Draw the progressing arch.
        {
            final float d = radius * 0.95f;
            canvas.drawArc(
                    centerWidth - d,
                    centerHeight - d,
                    centerWidth + d,
                    centerHeight + d,
                    -90, spinner,
                    false,
                    spinnerPaint
            );
        }
        {
            Paint paint = new Paint();
            paint.setTextSize(48);
            paint.setColor(Color.WHITE);
            canvas.drawText(playerName, 400, 200, paint);
        }
        // Draw the frame-rate counter.
        {
            if (showFps) {
                canvas.drawText(
                        String.format(Locale.getDefault(), "%.2f", avgFps),
                        10.0f, 30.0f,
                        fpsText
                );
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
