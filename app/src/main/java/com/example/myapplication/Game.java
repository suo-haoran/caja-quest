package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.function.Consumer;

public class Game {
    private final Context context;
    private final SurfaceHolder holder;

    public Game(Context viewContext, SurfaceHolder holder) {
        this.context = viewContext;
        this.holder = holder;
    }
    public void click(MotionEvent event) {

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

    }

    public void resize(int width, int height) {

    }

    public void update() {
    }

    public long getSleepTime() {
        return 1;
    }
}
