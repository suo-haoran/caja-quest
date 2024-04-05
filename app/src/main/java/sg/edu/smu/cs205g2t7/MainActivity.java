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

public class MainActivity extends AppCompatActivity {
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


    private void hideStatusBar() {
        WindowInsetsController wic = getWindow().getInsetsController();
        if (wic != null) {
            wic.hide(WindowInsets.Type.statusBars());
        }
    }

    public void navToGameActivity(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("level", 1);
        startActivity(intent);
    }

    public void navToAuthorsActivity(View view) {
        Intent intent = new Intent(this, AuthorsActivity.class);
        startActivity(intent);
    }

    public void navToRecordActivity(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }
}