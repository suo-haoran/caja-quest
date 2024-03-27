package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PlayerRecordDbHelper extends SQLiteOpenHelper {
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlayerRecordContract.RecordEntry.TABLE_NAME;
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlayerRecordContract.RecordEntry.TABLE_NAME + " (" +
                    PlayerRecordContract.RecordEntry._ID + " INTEGER PRIMARY KEY," +
                    PlayerRecordContract.RecordEntry.COLUMN_NAME_TIME + " REAL)";
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "RecordEntry.db";

    public PlayerRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public List<Record> getRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorRecords = db.rawQuery("SELECT * FROM " + PlayerRecordContract.RecordEntry.TABLE_NAME, null);
        List<Record> records = new ArrayList<>();

        Log.d("DB", "Retrieve Records.");
        if (cursorRecords.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                int id = cursorRecords.getInt(0);
                double timing = cursorRecords.getDouble(1);
                Log.d("DB", "Retrieve Id: " + id + " Timing: " + timing);
                records.add(new Record(id, timing));
            } while (cursorRecords.moveToNext());
        }
        cursorRecords.close();
        return records;
    }

    public void storeRecord(double seconds) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlayerRecordContract.RecordEntry.COLUMN_NAME_TIME, seconds);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(PlayerRecordContract.RecordEntry.TABLE_NAME, null, values);
        Log.d("DB", "Insert, Record Id: " + newRowId + " Timing: " + seconds);
    }
}
