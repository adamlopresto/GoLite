package fake.domain.adamlopresto.gomind.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by adam on 3/9/14.
 */
public class HistoryView {
    public static final String VIEW = "history_view";

    //as of version 4
    public static final String COLUMN_ID = HistoryTable.COLUMN_ID;
    public static final String COLUMN_QUANTITY = HistoryTable.COLUMN_QUANTITY;
    public static final String COLUMN_DATE = HistoryTable.COLUMN_DATE;
    public static final String COLUMN_SERVING = HistoryTable.COLUMN_SERVING;

    public static final String COLUMN_FOOD = ServingsTable.COLUMN_FOOD;
    public static final String COLUMN_NAME = FoodsTable.COLUMN_NAME;
    @SuppressWarnings("WeakerAccess")
    public static final String COLUMN_FOOD_NOTES = FoodsTable.COLUMN_NOTES;
    public static final String COLUMN_NUMBER = ServingsTable.COLUMN_NUMBER;
    public static final String COLUMN_UNIT = ServingsTable.COLUMN_UNIT;
    public static final String COLUMN_CAL = ServingsTable.COLUMN_CAL;
    public static final String COLUMN_LISTED = ServingsTable.COLUMN_LISTED;
    public static final String COLUMN_HISTORY_ID = ServingsView.COLUMN_HISTORY_ID;

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE VIEW " + VIEW
                        + " AS SELECT "
                        + HistoryTable.TABLE + "." + COLUMN_ID + " AS " + COLUMN_ID + ", "
                        + COLUMN_QUANTITY + ", "
                        + COLUMN_DATE + ", "
                        + COLUMN_SERVING + ", "
                        + COLUMN_FOOD + ", "
                        + COLUMN_NAME + ", "
                        + COLUMN_FOOD_NOTES + ", "
                        + COLUMN_NUMBER + ", "
                        + COLUMN_UNIT + ", "
                        + COLUMN_CAL + ", "
                        + COLUMN_LISTED + ", "
                        + HistoryTable.TABLE + "." + COLUMN_ID + " AS " + COLUMN_HISTORY_ID
                        + " FROM " + FoodsTable.TABLE + " INNER JOIN " + ServingsTable.TABLE
                        + " ON " + FoodsTable.TABLE + "." + FoodsTable.COLUMN_ID + "="
                        + ServingsTable.TABLE + "." + ServingsTable.COLUMN_FOOD
                        + " INNER JOIN " + HistoryTable.TABLE + " ON "
                        + ServingsTable.TABLE + "." + ServingsTable.COLUMN_ID + "="
                        + HistoryTable.TABLE + "." + HistoryTable.COLUMN_SERVING
        );
    }

    @SuppressWarnings("UnusedParameters")
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4)
            onCreate(db);
    }
}
