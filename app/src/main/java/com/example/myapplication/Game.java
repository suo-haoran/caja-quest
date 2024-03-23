package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.Arrays;
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
    private int circleIndex = 0;
    private int crateIndex = 1;
    private final int[][] board = new int[1][];

    public Game(Context viewContext, SurfaceHolder holder) {
        this.context = viewContext;
        this.holder = holder;
        // 2 represents player, 1 represent crate
        board[0] = new int[]{2, 1, 0, 0, 0};
    }

    public void click(MotionEvent event) {
        if (crateIndex == board[0].length - 1) {
            sendNotification();
        }
        if (circleIndex + 1 != board[0].length) {
            circle.moveX(50);
            board[0][circleIndex] -= 2;
            circleIndex++;
            board[0][circleIndex] += 2;
        }

        if (circleIndex == crateIndex && crateIndex + 1 != board[0].length) {
            crate.moveX(50);
            board[0][crateIndex] -= 1;
            crateIndex++;
            board[0][crateIndex] += 1;
        }
        Log.d("Game", "Circle Index " + circleIndex);
        Log.d("Game", "CrateIndex " + crateIndex);
        Log.d("Game", Arrays.toString(board[0]));
    }

    private void sendNotification() {
        NotificationPublisher.showNotification(context);
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
        {
            canvas.drawColor(Color.BLACK);
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(circle.getR(), circle.getG(), circle.getB()));
            canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);
        }
        // Draw the end.
        {
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(end.getR(), end.getG(), end.getB()));
            canvas.drawCircle(end.getX(), end.getY(), end.getRadius(), paint);
        }
        // Draw Boxes
        {
            final Paint paint = new Paint();
            paint.setColor(Color.rgb(crate.getR(), crate.getG(), crate.getB()));

            canvas.drawCircle(crate.getX(), crate.getY(), crate.getRadius(), paint);
        }
    }

    // This is the initializer for some reason, in real life projects, please use onDraw in view.
    public void resize(int width, int height) {
        circle = new Circle(50, 100);
        end = new Circle(250, 100);
        crate = new Circle(100, 100);
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
