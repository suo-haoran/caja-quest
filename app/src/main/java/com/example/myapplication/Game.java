package com.example.myapplication;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.SurfaceHolder;

import java.util.function.Consumer;

public class Game {
    private final Context context;
    private final SurfaceHolder holder;
    private final Object mutex = new Object();
    private int width = 0;
    private int height = 0;
    private Circle circle;
    private Circle end;
    private Circle crate;
//    private int circleIndex = 0;
//    private int crateIndex = 1;

    private final int xStep = 200;
    private final int yStep = 200;

    private final int xOffset = 5;
    private final int yOffset = 30;

    private final int numColumns = 5;
    private final int numRows = 5;
    private final int[][] board = new int[numRows][numColumns];
    private final Coordinates circleCoords = new Coordinates(0, 0);
    private final Coordinates crateCoords = new Coordinates(4, 3);
    private final Coordinates endCoords = new Coordinates(4, 4);

    private final static int targetFps = 30;

    private final static long intervalFps = 1000L;

    private final static long intervalUps = 1000L;

    private final Counter frameCounter = new Counter();

    private final ElapsedTimer elapsedTimer = new ElapsedTimer();

    private final DeltaStepper fpsUpdater = new DeltaStepper(intervalFps, this::fpsUpdate);

    private final DeltaStepper upsUpdater = new DeltaStepper(intervalUps, this::upsUpdate);

    private final Paint fpsText = new Paint();

    private final Paint circlePaint = new Paint();

    private final Paint circleOutlinePaint = new Paint();

    private final Paint tickPaint = new Paint();

    private final Paint handPaint = new Paint();

    private final Paint spinnerPaint = new Paint();


    private double avgFps = 0.0;

    private boolean showFps = false;

    private int secondCount = 0;

    private float spinner = 0.0f;

    private boolean finished = false;
    public Game(Context viewContext, SurfaceHolder holder) {
        this.context = viewContext;
        this.holder = holder;
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
        movePlayerAndCrates(1, 0);
    }

    public void swipeLeft() {
        movePlayerAndCrates(-1, 0);
    }

    public void swipeUp() {
        movePlayerAndCrates(0, -1);
    }

    public void swipeDown() {
        movePlayerAndCrates(0, 1);
    }

    /**
     * move player, if crate is in the way, player will push it
     * Vibrate if player is going out of bounds
     *
     * @param xDelta +- 1
     * @param yDelta +- 1
     */
    private void movePlayerAndCrates(int xDelta, int yDelta) {
        if (isOutOfBounds(circleCoords.clone(circleCoords.x + xDelta, circleCoords.y + yDelta))) {
            Vibrator v = getSystemService(context, Vibrator.class);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            return;
        }
        circle.moveX(xDelta * xStep);
        circle.moveY(yDelta * yStep);

        board[circleCoords.x][circleCoords.y] -= 2;
        circleCoords.x += xDelta;
        circleCoords.y += yDelta;
        board[circleCoords.x][circleCoords.y] += 2;

        if (circleCoords.equals(crateCoords) && !isOutOfBounds(crateCoords.clone(crateCoords.x + xDelta, crateCoords.y + yDelta))) {
            crate.moveX(xDelta * xStep);
            crate.moveY(yDelta * yStep);

            board[crateCoords.x][crateCoords.y] -= 1;
            crateCoords.x += xDelta;
            crateCoords.y += yDelta;
            board[crateCoords.x][crateCoords.y] += 1;
        }
        if (crateCoords.equals(endCoords)) {
            sendNotification("Yay", "You win!");
        }
    }

    private boolean isOutOfBounds(Coordinates coord) {
        return coord.x < 0 || coord.x >= board[0].length || coord.y < 0 || coord.y >= board.length;
    }

    private void sendNotification(String title, String message) {
        NotificationPublisher.showNotification(context, title, message);
    }

    private boolean useCanvas(final Consumer<Canvas> onDraw) {
        boolean result = false;
        try {
            final Canvas canvas = holder.lockCanvas();
            try {
                onDraw.accept(canvas);
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas);
                    result = true;
                } catch (final IllegalStateException e) {
                    // Do nothing
                }
            }
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void draw() {
        useCanvas(this::draw);
    }

    private void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        // Draw the end.
        {
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(end.getR(), end.getG(), end.getB()));
            canvas.drawCircle(end.getX(), end.getY(), end.getRadius(), paint);
        }
        synchronized (mutex) {

            canvas.drawColor(Color.BLACK);

            // Draw the Player.
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(circle.getR(), circle.getG(), circle.getB()));
            canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);

            // Draw Crate
            paint.setColor(Color.rgb(crate.getR(), crate.getG(), crate.getB()));
            canvas.drawCircle(crate.getX(), crate.getY(), crate.getRadius(), paint);

            // Draw Grids
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            int width = 1000;
            int height = 1000;
            int cellWidth = 200;
            int cellHeight = 200;

            int i;
            for (i = 0; i <= numColumns; i++) {
                canvas.drawLine(xOffset + i * cellWidth, yOffset, xOffset + i * cellWidth, height + yOffset, paint);
            }

            for (i = 0; i <= numRows; i++) {
                canvas.drawLine(xOffset, i * cellHeight + yOffset, width + xOffset, i * cellHeight + yOffset, paint);
            }
        }

        final float radius = Math.min(width, height) * 0.10f;
        final float centerWidth = width / 2.0f;
        final float centerHeight = height / 2.0f;
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
        // Draw the frame-rate counter.
        {
            if (showFps) {
                canvas.drawText(
                        String.format("%.2f", avgFps),
                        10.0f, 30.0f,
                        fpsText
                );
            }
        }
    }

    public void onDraw(int width, int height) {
        this.width = width;
        this.height = height;
        int start = 100;
        circle = new Circle(start + xOffset, yOffset + start);
        end = new Circle(xOffset + start + endCoords.x * xStep, yOffset + start + endCoords.y * yStep);
        crate = new Circle(xOffset + start + crateCoords.x * xStep, yOffset + start + crateCoords.y * yStep);
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
                    e.printStackTrace();
                }
                spinnerPaint.setColor(Color.BLACK);
            }
        }
        return true;
    }

    private boolean fpsUpdate(long deltaTime) {
        final double fractionTime = intervalFps / (double)deltaTime;
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
    // Calculate Dimensions for Grid
    // private void calculateDimensions() {
    //     if (numColumns < 1 || numRows < 1) {
    //         return;
    //     }

    //     cellWidth = getWidth() / numColumns;
    //     cellHeight = getHeight() / numRows;

    //     cellChecked = new boolean[numColumns][numRows];

    //     invalidate();
    // }
}
