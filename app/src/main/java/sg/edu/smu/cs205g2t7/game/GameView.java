package sg.edu.smu.cs205g2t7.game;

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
    private final Game game;
    private GameThread gameThread;

    public GameView(Context context) {
        this(context, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public GameView(Context context, int level) {
        super(context);
        setKeepScreenOn(true);
        getHolder().addCallback(this);
        setFocusable(View.FOCUSABLE);
        setOnTouchListener(new OnTouchListener() {
            // Credit: https://github.com/plter/Android2048GameLesson/blob/master/code/ide/AndroidStudio/Game2048Publish/app/src/main/java/com/jikexueyuan/game2048publish/GameView.java
            // Must be global variable, or else, only right and down will be triggered
            private float startX = 0;
            private float startY = 0;

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
                        determineAction(offsetX, offsetY);
                        break;
                }
                return true;
            }
        });
        game = new Game(getContext(), getHolder(), level, 2);
        gameThread = new GameThread(game);
    }

    private void determineAction(float offsetX, float offsetY) {
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            determineLeftRight(offsetX);
        } else {
            determineUpDown(offsetY);
        }
    }

    private void determineLeftRight(float offsetX) {
        if (offsetX < -5) {
            game.swipeLeft();
        } else if (offsetX > 5) {
            game.swipeRight();
        }
    }

    private void determineUpDown(float offsetY) {
        if (offsetY < -5) {
            game.swipeUp();
        } else if (offsetY > 5) {
            game.swipeDown();
        }
    }


    @Override
    public void surfaceCreated(@NonNull final SurfaceHolder surfaceHolder) {
        if ((gameThread == null) || (gameThread.getState() == Thread.State.TERMINATED)) {
            gameThread = new GameThread(game);
        }
        final Rect rect = getHolder().getSurfaceFrame();
        game.onDraw(rect.width(), rect.height());
        gameThread.startLoop();

    }

    @Override
    public void surfaceChanged(@NonNull final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        game.onDraw(width, height);
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
