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
/**
 * The view of the game, responsible for rendering the game and handling user input
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    /** Game instance */
    private final Game game;
    /** A single game thread */
    private GameThread gameThread;
    /**
     * Instantiates the game view at level 0
     * @param context game context
     */
    public GameView(Context context) {
        this(context, 0);
    }
    /**
     * Instantiates the game at a custom level
     * @param context game context
     * @param level the first or second level
     */
    @SuppressLint("ClickableViewAccessibility")
    public GameView(Context context, int level) {
        super(context);
        setKeepScreenOn(true);
        getHolder().addCallback(this);
        setFocusable(View.FOCUSABLE);
        setOnTouchListener(new OnTouchListener() {
            // Credit: https://github.com/plter/Android2048GameLesson/blob/master/code/ide/AndroidStudio/Game2048Publish/app/src/main/java/com/jikexueyuan/game2048publish/GameView.java
            // Must be global variable, or else, only right and down will be triggered
            /** Initial x-coord of touch down */
            private float startX = 0;
            /** Initial y-coord of touch down */
            private float startY = 0;
            /**
             * Event listener that is triggered on touch down and touch up
             * When a touch down happens, it stores the coordinates of the cursor at startX and startY. 
             * When there is a touch up, this calculates the offset from the
             * original positions
             * @param v The current view
             * @param event Describes movement in terms of action code and axis values
             * @return true --> this is an overridden method, and we return true to match the method signature in View.OnTouchListener.
             */
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
    /**
     * Determines the action and calls the appropriate action
     * @param offsetX displacement of event.getX() from startX
     * @param offsetY displacement of event.getY() from startY
     */
    private void determineAction(float offsetX, float offsetY) {
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            determineLeftRight(offsetX);
        } else {
            determineUpDown(offsetY);
        }
    }
    /**
     * Return a swipeLeft action or a swiperight action depending on offsetX
     * @param offsetX horizontal displacement of touch up X coordinate from touch down X coordinate
     */
    private void determineLeftRight(float offsetX) {
        if (offsetX < -5) {
            game.swipeLeft();
        } else if (offsetX > 5) {
            game.swipeRight();
        }
    }
    /**
     * Return a swipeUp action or swipeRight action depending on offsetY
     * @param offsetY vertical displacement of touch-up Y coordinate from touch-down Y coordinate
     */
    private void determineUpDown(float offsetY) {
        if (offsetY < -5) {
            game.swipeUp();
        } else if (offsetY > 5) {
            game.swipeDown();
        }
    }

    /**
     * Implemented method from SurfaceHolder.callback
     * Called immediately after the surface is first created 
     * @param surfaceHolder The surfaceHolder whose surface is being created 
     */
    @Override
    public void surfaceCreated(@NonNull final SurfaceHolder surfaceHolder) {
        if ((gameThread == null) || (gameThread.getState() == Thread.State.TERMINATED)) {
            gameThread = new GameThread(game);
        }
        final Rect rect = getHolder().getSurfaceFrame();
        game.onDraw(rect.width(), rect.height());
        gameThread.startLoop();

    }
    /**
     * Implemented method from SurfaceHolder.callBack
     * Called after structural changes (format or size) have been made to the 
     * surface
     * @param surfaceHolder the surfaceholder whose surface has changed.
     * @param format The new PixelFormat of the surface
     * @param width The new width of the surface 
     * @param height The new height of the surface
     */
    @Override
    public void surfaceChanged(@NonNull final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        game.onDraw(width, height);
    }
    /**
     * Implemented method from SurfaceHolder.callback
     * Stop all game threads when the surface is destroyed
     * @param surfaceHolder The surfaceholder whose surface is being destroyed
     */
    @Override
    public void surfaceDestroyed(@NonNull final SurfaceHolder surfaceHolder) {
        gameThread.stopLoop();
        gameThread = null;
    }
    /**
     * Render this view to the given canvas 
     * @param canvas The Canvas to which the View is rendered. 
     */
    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        game.draw();
    }
}
