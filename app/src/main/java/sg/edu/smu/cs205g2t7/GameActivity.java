package sg.edu.smu.cs205g2t7;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class GameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra("name");
        GameView v = new GameView(this, name);
        setContentView(v);
    }
}
