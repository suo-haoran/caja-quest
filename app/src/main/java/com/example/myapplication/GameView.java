package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private Game game;
    private GameThread gameThread;
    @SuppressLint("ClickableViewAccessibility")
    public GameView(Context context) {
        super(context);
        setKeepScreenOn(true);
        getHolder().addCallback(this);
        setFocusable(View.FOCUSABLE);
        setOnTouchListener((view, event) -> {
            game.click(event);
            return true;
        });
        game = new Game(getContext(), getHolder());
        gameThread = new GameThread(game);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        if ((gameThread == null) || (gameThread.getState() == Thread.State.TERMINATED)) {
            gameThread = new GameThread(game);
        }
        final Rect rect = getHolder().getSurfaceFrame();
        game.resize(rect.width(), rect.height());
        gameThread.startLoop();
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        game.resize(width, height);
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
        gameThread.stopLoop();
        gameThread = null;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        game.draw();
    }
}
