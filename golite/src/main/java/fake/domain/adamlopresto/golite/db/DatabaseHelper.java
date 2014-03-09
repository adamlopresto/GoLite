package fake.domain.adamlopresto.golite.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.text.SimpleDateFormat;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int CURRENT_VERSION = 4;
    private static final String DATABASE_NAME = "GoLite";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static DatabaseHelper instance;

    private final Context context;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, context.getExternalFilesDir(null) + "/" + DATABASE_NAME, null, CURRENT_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FoodsTable.onCreate(db);
        ServingsTable.onCreate(db);
        HistoryTable.onCreate(db);
        ServingsView.onCreate(db);
        TotalsView.onCreate(db);
        HistoryView.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FoodsTable.onUpgrade(db, oldVersion, newVersion);
        ServingsTable.onUpgrade(db, oldVersion, newVersion);
        HistoryTable.onUpgrade(db, oldVersion, newVersion);
        ServingsView.onUpgrade(db, oldVersion, newVersion);
        TotalsView.onUpgrade(db, oldVersion, newVersion);
        HistoryView.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    public void notifyChange(Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null)
            resolver.notifyChange(uri, null);
    }

    public static String[] idToArgs(long id) {
        return new String[]{String.valueOf(id)};
    }

}
