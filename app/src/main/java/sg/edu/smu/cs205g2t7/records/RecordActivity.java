package sg.edu.smu.cs205g2t7.records;

import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.smu.cs205g2t7.R;
/**
 * User Interface for Records page
 */
public class RecordActivity extends AppCompatActivity {
    /**
     * Creates the view for the app based of the record_main.xml file
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record);
        hideStatusBar();
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        RecyclerView rv = findViewById(R.id.record_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RecordsAdapter(this));
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


}