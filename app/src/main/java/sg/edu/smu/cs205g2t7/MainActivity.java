package sg.edu.smu.cs205g2t7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import sg.edu.smu.cs205g2t7.author.AuthorsActivity;
import sg.edu.smu.cs205g2t7.game.GameActivity;
import sg.edu.smu.cs205g2t7.records.RecordActivity;
/**
 * User interface for the App
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Set up the activity when it is created. Sets the content view to the
     * layout defined in activity_main.xml.
     * @param savedInstanceState - Restore state of activity if it was previously destroyed and recreated by the system.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        hideStatusBar();
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    /**
     * Hides the status bar through the WindowInsetsController interface.
     * Insets are parts of the screen where the app intersects with system UI
     * This overlaps between your app and areas where system UI is displayed.
     */
    private void hideStatusBar() {
        WindowInsetsController wic = getWindow().getInsetsController();
        if (wic != null) {
            wic.hide(WindowInsets.Type.statusBars());
        }
    }
    /**
     * Navigate to the game if the user has entered the name.
     * @param view - For user interaction handling
     */
    public void navToGameActivity(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("level", 1);
        startActivity(intent);
    }
    /**
     * Navigate to the author activity when button is clicked
     * @param view - For user interaction handling
     */
    public void navToAuthorsActivity(View view) {
        Intent intent = new Intent(this, AuthorsActivity.class);
        startActivity(intent);
    }
    /**
     * Navigate to the record activity when the button is clicked
     * @param view - For user interaction handling
     */
    public void navToRecordActivity(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }
}