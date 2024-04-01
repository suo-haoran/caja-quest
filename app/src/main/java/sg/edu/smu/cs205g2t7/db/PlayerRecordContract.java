package sg.edu.smu.cs205g2t7.db;

import android.provider.BaseColumns;

public class PlayerRecordContract {

    private PlayerRecordContract() {}
    /* Inner class that defines the table contents */
    public static class RecordEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String _ID = "id";
        public static final String COLUMN_NAME_TIME = "time";
    }
}
