package fake.domain.adamlopresto.golite.db;

import android.database.sqlite.SQLiteDatabase;

public class ServingsView {
    public static final String VIEW = "servings_view";

    //as of version 1
    public static final String COLUMN_ID = ServingsTable.COLUMN_ID;
    public static final String COLUMN_FOOD = ServingsTable.COLUMN_FOOD;
    public static final String COLUMN_NAME = FoodsTable.COLUMN_NAME;
    @SuppressWarnings("WeakerAccess")
    public static final String COLUMN_FOOD_NOTES = FoodsTable.COLUMN_NOTES;
    public static final String COLUMN_NUMBER = ServingsTable.COLUMN_NUMBER;
    public static final String COLUMN_UNIT = ServingsTable.COLUMN_UNIT;
    public static final String COLUMN_CAL = ServingsTable.COLUMN_CAL;
    public static final String COLUMN_LISTED = ServingsTable.COLUMN_LISTED;

    //added version 2
    public static final String COLUMN_QUANTITY = HistoryTable.COLUMN_QUANTITY;
    public static final String COLUMN_HISTORY_ID = "history_id";


    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE VIEW " + VIEW
                + " AS SELECT "
                + ServingsTable.TABLE + "." + COLUMN_ID + " AS " + COLUMN_ID + ", "
                + COLUMN_FOOD + ", "
                + COLUMN_NAME + ", "
                + COLUMN_FOOD_NOTES + ", "
                + COLUMN_NUMBER + ", "
                + COLUMN_UNIT + ", "
                + COLUMN_CAL + ", "
                + COLUMN_LISTED + ", "
                + COLUMN_QUANTITY + ", "
                + HistoryTable.TABLE + "." + HistoryTable.COLUMN_ID + " AS " + COLUMN_HISTORY_ID
                + " FROM " + FoodsTable.TABLE + " INNER JOIN " + ServingsTable.TABLE
                + " ON " + FoodsTable.TABLE + "." + FoodsTable.COLUMN_ID + "="
                + ServingsTable.TABLE + "." + ServingsTable.COLUMN_FOOD
                + " LEFT OUTER JOIN " + HistoryTable.TABLE + " ON "
                + ServingsTable.TABLE + "." + ServingsTable.COLUMN_ID + "="
                + HistoryTable.TABLE + "." + HistoryTable.COLUMN_SERVING
                + " AND " + HistoryTable.TABLE + "." + HistoryTable.COLUMN_DATE + "=DATE('NOW', 'LOCALTIME')"
        );
    }

    @SuppressWarnings("UnusedParameters")
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP VIEW " + VIEW);
            onCreate(db);
        }
    }
}
