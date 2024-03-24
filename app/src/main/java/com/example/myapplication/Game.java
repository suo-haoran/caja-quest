package com.example.myapplication;

import static androidx.core.content.ContextCompat.getSystemService;

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

    private int xStep = 200;
    private int yStep = 200;

    private int xOffset = 0;
    private int yOffset = 30;

    private int numColumns = 5;
    private int numRows = 5;
    private final int[][] board = new int[numRows][numColumns];
    private Coordinates circleCoords = new Coordinates(0, 0);
    private Coordinates crateCoords = new Coordinates(4, 3);
    private Coordinates endCoords = new Coordinates(4, 4);

    public Game(Context viewContext, SurfaceHolder holder) {
        this.context = viewContext;
        this.holder = holder;
        // 2 represents player, 1 represent crate, INT_MIN represents trap
        board[0] = new int[]{2, 0, 0, 0, 0};
        board[1] = new int[]{0, 0, 0, 0, 0};
        board[2] = new int[]{0, 0, Integer.MIN_VALUE, 0, 0}; // trap at 2,2
        board[3] = new int[]{0, 0, 0, 0, 0};
        board[4] = new int[]{0, 0, 0, 1, 0};
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
        // Draw the Player.
        synchronized (mutex){
            canvas.drawColor(Color.BLACK);
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(circle.getR(), circle.getG(), circle.getB()));
            canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);

            // Draw Crate
            paint.setColor(Color.rgb(crate.getR(), crate.getG(), crate.getB()));
            canvas.drawCircle(crate.getX(), crate.getY(), crate.getRadius(), paint);
        }
        // Draw the end.
        {
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(end.getR(), end.getG(), end.getB()));
            canvas.drawCircle(end.getX(), end.getY(), end.getRadius(), paint);
        }
        // Draw grids
        synchronized (mutex){
            if (numColumns == 0 || numRows == 0) {
                return;
            }

            final Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            int width = 1000;
            int height = 1000;
            int cellWidth = 200;
            int cellHeight = 200;

            for (int i = 0; i < numColumns; i++) {
                canvas.drawLine(xOffset + i * cellWidth, yOffset, xOffset + i * cellWidth, height + yOffset, paint);
            }

            for (int i = 0; i < numRows; i++) {
                canvas.drawLine(xOffset, i * cellHeight + yOffset, width + xOffset, i * cellHeight + yOffset, paint);
            }
        }
    }

    // This is the initializer for some reason, in real life projects, please use onDraw in view.
    public void resize(int width, int height) {
        int start = 100;
        circle = new Circle(start + xOffset, yOffset +start);
        end = new Circle(xOffset + start + endCoords.x * xStep, yOffset + start + endCoords.y * yStep);
        crate = new Circle(xOffset + start + crateCoords.x * xStep, yOffset + start + crateCoords.y * yStep);
    }

    public void update() {
    }

    public long getSleepTime() {
        return 1;
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
