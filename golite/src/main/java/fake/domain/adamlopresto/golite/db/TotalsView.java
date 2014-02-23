package fake.domain.adamlopresto.golite.db;

import android.database.sqlite.SQLiteDatabase;

public class TotalsView {
    //as of version 3
    public static final String VIEW = "totals";

    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_DATE = "date";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE VIEW " + VIEW
                + " AS SELECT SUM(qty*calories) AS total, date"
                + " FROM history INNER JOIN servings ON history.serving = servings._id"
                + " GROUP BY date"
        );
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3)
            onCreate(db);
    }
}
