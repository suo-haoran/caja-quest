package sg.edu.smu.cs205g2t7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private EditText editTextName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextName = findViewById(R.id.edit_text_name);
    }


    private void hideStatusBar() {
        WindowInsetsController wic = getWindow().getInsetsController();
        if (wic != null) {
            wic.hide(WindowInsets.Type.statusBars());
        }
    }

    public void navToGameActivity(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        String name = editTextName.getText().toString();
        if (name.isEmpty()) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main), "Please Input the Name Before Clicking the Start Game Button",BaseTransientBottomBar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        intent.putExtra("name", name);
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