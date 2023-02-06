package fake.domain.adamlopresto.gomind.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by adam on 1/24/2015.
 */
public class BarcodesTable {
    public static final String TABLE = "barcodes";

    //Initial version, 5
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOOD = "food";
    public static final String COLUMN_BARCODE = "barcode";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE
                        + " ("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_FOOD + " INTEGER REFERENCES " + FoodsTable.TABLE + " ON DELETE CASCADE, "
                        + COLUMN_BARCODE + " TEXT NOT NULL UNIQUE"
                        + ")"
        );

        db.execSQL("CREATE INDEX barcodes_food ON " + TABLE + " (" + COLUMN_FOOD + ")");
        db.execSQL("CREATE INDEX barcodes_barcode ON " + TABLE + " (" + COLUMN_BARCODE + ")");
    }

    @SuppressWarnings({"UnusedParameters"})
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 4)
            onCreate(db);
    }
}
