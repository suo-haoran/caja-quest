package com.example.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lv = findViewById(R.id.record_list);
        List<Double> records = new ArrayList<>();

        PlayerRecordDbHelper dbHelper = new PlayerRecordDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursorRecords = db.rawQuery("SELECT * FROM " + PlayerRecordContract.RecordEntry.TABLE_NAME, null);
        if (cursorRecords.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                records.add(cursorRecords.getDouble(1));
            } while (cursorRecords.moveToNext());
        }
        cursorRecords.close();

        ArrayAdapter<Double> arrayAdapter = new ArrayAdapter<>(
            this,
                R.layout.record_list_item,
                R.id.txt_timing
        );
    }


}