package sg.edu.smu.cs205g2t7.db;

import android.provider.BaseColumns;
/**
 * Contains constants related to the database schema
 */
public class PlayerRecordContract {
    /**
     * Not meant for instantiation
     */
    private PlayerRecordContract() {}
    /**
     * Defines the table structure of the database
     */
    public static class RecordEntry implements BaseColumns {
        /** Name of database table */
        public static final String TABLE_NAME = "entry";
        /** Primary Key */
        public static final String _ID = "id";
        /** Time Column */
        public static final String COLUMN_NAME_TIME = "time";
    }
}
