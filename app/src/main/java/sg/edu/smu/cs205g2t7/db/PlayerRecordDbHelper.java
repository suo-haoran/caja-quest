package sg.edu.smu.cs205g2t7.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.cs205g2t7.records.Record;
/**
 * Helper class that directly interacts with the database
 */
public class PlayerRecordDbHelper extends SQLiteOpenHelper {
    /** SQL Statement to remove existing entry table */
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlayerRecordContract.RecordEntry.TABLE_NAME;
    /** SQL statement to create entry table */
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlayerRecordContract.RecordEntry.TABLE_NAME + " (" +
                    PlayerRecordContract.RecordEntry._ID + " INTEGER PRIMARY KEY," +
                    PlayerRecordContract.RecordEntry.COLUMN_NAME_TIME + " REAL)";
    /**
     * Specify the database version
     * Changing the schema requires incrementing the DB version
     */
    public static final int DATABASE_VERSION = 2;
    /** Name of database */
    public static final String DATABASE_NAME = "RecordEntry.db";
    /**
     * Instantiates the DbHelper object
     * @param context app context
     */
    public PlayerRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * Creates the entry table
     * @param db
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    /**
     * Defines the policy to upgrade the database when the schema changes.
     * This database is only a cache for online data, so its upgrade policy is
     * to simply to discard the data and start over
     * @param db data in database
     * @param oldVersion old version of database schema
     * @param newVersion new version of database schema
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    /**
     * Defines the policy to downgrade the database when the schema changes.
     * In our case, we have no intention to downgrade the database, so this just calls
     * the upgrade function
     * @param db data in database
     * @param oldVersion old version of database schema
     * @param newVersion new version of database schema
     */    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    /**
     * Retrieves records from the database
     * @return records - A list of past game record objects
     */
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
    /**
     * Method to insert into the database after the game is completed
     * @param seconds user's timing
     */
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
