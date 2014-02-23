package fake.domain.adamlopresto.golite.db;

import android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("WeakerAccess")
public class HistoryTable {
    public static final String TABLE = "history";

    //as of version 1
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_SERVING = "serving";
    public static final String COLUMN_QUANTITY = "qty";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE
                + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT NOT NULL, "
                + COLUMN_SERVING + " INTEGER REFERENCES " + ServingsTable.TABLE + " ON DELETE CASCADE, "
                + COLUMN_QUANTITY + " NUMERIC NOT NULL "
                + ")"
        );

        db.execSQL("CREATE INDEX history_serving ON " + TABLE + " (" + COLUMN_SERVING + ")");
        db.execSQL("CREATE INDEX history_date ON " + TABLE + " (" + COLUMN_DATE + ")");
    }

    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
