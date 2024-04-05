package sg.edu.smu.cs205g2t7.game;

import android.app.Activity;
import android.os.Bundle;
/**
 * User Interface for the Game instance
 */
public class GameActivity extends Activity {
    /**
     * Creates the context of the game
     * @param savedInstanceState Previous instance state if this page has been previously loaded
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int level = getIntent().getIntExtra("level", 0);
        GameView v = new GameView(this, level);
        setContentView(v);
    }
}
