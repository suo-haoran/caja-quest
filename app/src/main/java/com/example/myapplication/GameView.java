package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
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
        setOnTouchListener(new View.OnTouchListener() {
            private float startX = 0;
            private float startY =0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float offsetX = event.getX() - startX;
                        float offsetY = event.getY() - startY;

//                if (crateIndex == board[0].length - 1) {
//                    sendNotification("Yay", "You win!");
//                    break;
//                }
                        if (Math.abs(offsetX)>Math.abs(offsetY)) {
                            if (offsetX <-5) {
                                game.swipeLeft();
                            }else if (offsetX >5) {
                                game.swipeRight();
                            }
                        }else{
                            if (offsetY <-5) {
                                game.swipeUp();
                            }else if (offsetY >5) {
                                game.swipeDown();
                            }
                        }
                        break;
                }
                return true;
            }
        });
        game = new Game(getContext(), getHolder());
        gameThread = new GameThread(game);
    }

    @Override
    public void surfaceCreated(@NonNull final SurfaceHolder surfaceHolder) {
        if ((gameThread == null) || (gameThread.getState() == Thread.State.TERMINATED)) {
            gameThread = new GameThread(game);
        }
        final Rect rect = getHolder().getSurfaceFrame();
        game.resize(rect.width(), rect.height());
        gameThread.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        game.resize(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull final SurfaceHolder surfaceHolder) {
        gameThread.stopLoop();
        gameThread = null;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        game.draw();
    }
}
