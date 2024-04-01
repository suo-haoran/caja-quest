package sg.edu.smu.cs205g2t7.game;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra("name");
        GameView v = new GameView(this, name);
        setContentView(v);
    }
}
