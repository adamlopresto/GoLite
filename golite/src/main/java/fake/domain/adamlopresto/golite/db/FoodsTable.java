package fake.domain.adamlopresto.golite.db;

import android.database.sqlite.SQLiteDatabase;

public class FoodsTable {
    public static final String TABLE = "foods";

    //as of version 1
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "food_name";
    public static final String COLUMN_NOTES = "food_notes";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE
                + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, "
                + COLUMN_NOTES + " TEXT"
                + ")"
        );

    }

    @SuppressWarnings("EmptyMethod")
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
