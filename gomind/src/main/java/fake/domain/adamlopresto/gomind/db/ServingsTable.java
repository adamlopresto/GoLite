package fake.domain.adamlopresto.gomind.db;

import android.database.sqlite.SQLiteDatabase;

public class ServingsTable {
    public static final String TABLE = "servings";

    //as of version 1
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOOD = "food";
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_CAL = "calories";
    public static final String COLUMN_LISTED = "listed";

    /*
    Each serving has a reference to a food, a number, a unit, and a number of calories.
    So something like "7 chips = 200 calories" would have number 7, unit 'chips', calories 200
    Should all units be written in plural?
    Should there be a table of units to lookup the pluralization rules?
    Not yet. Maybe later.
     */

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE
                + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FOOD + " INTEGER REFERENCES " + FoodsTable.TABLE + " ON DELETE CASCADE, "
                + COLUMN_NUMBER + " NUMERIC NOT NULL, "
                + COLUMN_UNIT + " TEXT, "
                + COLUMN_CAL + " NUMERIC, "
                + COLUMN_LISTED + " BOOLEAN NOT NULL "
                + ")"
        );

        db.execSQL("CREATE INDEX servings_food ON " + TABLE + " (" + COLUMN_FOOD + ")");
    }

    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
