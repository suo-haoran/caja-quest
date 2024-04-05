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

public class RecordActivity extends AppCompatActivity {

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


    private void hideStatusBar() {
        WindowInsetsController wic = getWindow().getInsetsController();
        if (wic != null) {
            wic.hide(WindowInsets.Type.statusBars());
        }
    }


}