package fake.domain.adamlopresto.golite;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.FoodsTable;
import fake.domain.adamlopresto.golite.db.HistoryTable;
import fake.domain.adamlopresto.golite.db.HistoryView;
import fake.domain.adamlopresto.golite.db.ServingsTable;
import fake.domain.adamlopresto.golite.db.ServingsView;
import fake.domain.adamlopresto.golite.db.TotalsView;

@SuppressWarnings("WeakerAccess")
public class GoLiteContentProvider extends ContentProvider {

    private DatabaseHelper helper;

    // Used for the UriMatcher // Odd numbers have an ID, evens don't.
    private static final int FOODS = 0;
    private static final int FOOD_ID = 1;
    private static final int SERVINGS = 2;
    private static final int SERVING_ID = 3;
    private static final int SERVINGS_LISTED = 4;
    private static final int HISTORY = 6;
    private static final int HISTORY_ID = 7;
    private static final int SERVINGS_DATED_HISTORY = 8;
    private static final int DAILY_TOTAL = 10;
    private static final int DELETE_INVALID = 12;

    private static final String AUTHORITY = "fake.domain.adamlopresto.golite";

    private static final Uri BASE = Uri.parse("content://" + AUTHORITY);

    private static final String FOOD_BASE_PATH = "food";
    public static final Uri FOOD_URI = Uri.withAppendedPath(BASE, FOOD_BASE_PATH);

    private static final String SERVING_BASE_PATH = "serving";
    public static final Uri SERVING_URI = Uri.withAppendedPath(BASE, SERVING_BASE_PATH);

    @SuppressWarnings("WeakerAccess")
    public static final String SERVING_LISTED_PATH = SERVING_BASE_PATH + "/listed";
    @SuppressWarnings("unused")
    public static final Uri SERVING_LISTED_URI = Uri.withAppendedPath(BASE, SERVING_LISTED_PATH);

    private static final String HISTORY_BASE_PATH = "history";
    @SuppressWarnings("WeakerAccess")
    public static final Uri HISTORY_URI = Uri.withAppendedPath(BASE, HISTORY_BASE_PATH);

    private static final String SERVING_DATED_HISTORY_PATH = SERVING_BASE_PATH+"/dated";
    public static final Uri SERVING_DATED_HISTORY_URI = Uri.withAppendedPath(BASE, SERVING_DATED_HISTORY_PATH);

    private static final String DAILY_TOTAL_BASE_PATH = "daily_total";
    public static final Uri DAILY_TOTAL_URI = Uri.withAppendedPath(BASE, DAILY_TOTAL_BASE_PATH);

    private static final String DELETE_INVALID_PATH = "delete_invalid";
    public static final Uri DELETE_INVALID_URI = Uri.withAppendedPath(BASE, DELETE_INVALID_PATH);

    /*
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/GoShopItems";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/GoShopItem";
	 */

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        sURIMatcher.addURI(AUTHORITY, FOOD_BASE_PATH, FOODS);
        sURIMatcher.addURI(AUTHORITY, FOOD_BASE_PATH + "/#", FOOD_ID);
        sURIMatcher.addURI(AUTHORITY, SERVING_BASE_PATH, SERVINGS);
        sURIMatcher.addURI(AUTHORITY, SERVING_BASE_PATH + "/#", SERVING_ID);
        sURIMatcher.addURI(AUTHORITY, SERVING_LISTED_PATH, SERVINGS_LISTED);
        sURIMatcher.addURI(AUTHORITY, HISTORY_BASE_PATH, HISTORY);
        sURIMatcher.addURI(AUTHORITY, HISTORY_BASE_PATH + "/#", HISTORY_ID);
        sURIMatcher.addURI(AUTHORITY, SERVING_DATED_HISTORY_PATH + "/*", SERVINGS_DATED_HISTORY);
        sURIMatcher.addURI(AUTHORITY, DAILY_TOTAL_BASE_PATH, DAILY_TOTAL);
        sURIMatcher.addURI(AUTHORITY, DELETE_INVALID_PATH, DELETE_INVALID);
    }

    @Override
    public boolean onCreate() {
        helper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);

        //If it's odd, then it has an ID appended.
        if ((uriType % 2) == 1) {
            String id = uri.getLastPathSegment();
            selection = appendSelection(selection, "_id = ?");
            selectionArgs = appendSelectionArg(selectionArgs, id);
            uriType--;
        }

        switch (uriType) {
            case FOODS:
                queryBuilder.setTables(FoodsTable.TABLE);
                uri = FOOD_URI;
                break;
            case SERVINGS_LISTED:
                selection = appendSelection(selection, "listed = 1");
                //FALL THROUGH
            case SERVINGS:
                queryBuilder.setTables(ServingsView.VIEW);
                uri = SERVING_URI;
                break;
            case HISTORY:
                queryBuilder.setTables(HistoryView.VIEW);
                uri = HISTORY_URI;
                break;
            case SERVINGS_DATED_HISTORY:
                String date = uri.getLastPathSegment();
                Map<String, String> map = new HashMap<>(10);
                map.put(ServingsView.COLUMN_ID, "servings._id");
                map.put(ServingsView.COLUMN_FOOD, ServingsView.COLUMN_FOOD);
                map.put(ServingsView.COLUMN_NAME, ServingsView.COLUMN_NAME);
                map.put(ServingsView.COLUMN_FOOD_NOTES, ServingsView.COLUMN_FOOD_NOTES);
                map.put(ServingsView.COLUMN_NUMBER, ServingsView.COLUMN_NUMBER);
                map.put(ServingsView.COLUMN_UNIT, ServingsView.COLUMN_UNIT);
                map.put(ServingsView.COLUMN_CAL, ServingsView.COLUMN_CAL);
                map.put(ServingsView.COLUMN_LISTED, ServingsView.COLUMN_LISTED);
                map.put(ServingsView.COLUMN_QUANTITY, ServingsView.COLUMN_QUANTITY);
                map.put("history_id", "history._id");
                queryBuilder.setProjectionMap(map);
                queryBuilder.setTables("foods INNER JOIN servings ON foods._id=servings.food " +
                        "LEFT OUTER JOIN history ON servings._id=history.serving " +
                        "AND history.date="+DatabaseUtils.sqlEscapeString(date));
                uri = HISTORY_URI;
                break;
            case DAILY_TOTAL:
                queryBuilder.setTables(TotalsView.VIEW);
                uri = DAILY_TOTAL_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        if (db == null) {
            throw new RuntimeException("Couldn't get readable database");
        }
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        setNotificationUri(cursor, uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = getWritableDatabase();
        int rowsUpdated;

        //If it's odd, then it has an ID appended.
        if ((uriType % 2) == 1) {
            String id = uri.getLastPathSegment();
            selection = appendSelection(selection, "_id = ?");
            selectionArgs = appendSelectionArg(selectionArgs, id);
            uriType--;
        }

        switch (uriType) {
            case FOODS:
                rowsUpdated = sqlDB.delete(FoodsTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(FOOD_URI);
                    helper.notifyChange(SERVING_URI);
                    helper.notifyChange(HISTORY_URI);
                    helper.notifyChange(DAILY_TOTAL_URI);
                }
                return rowsUpdated;
            case SERVINGS:
                rowsUpdated = sqlDB.delete(ServingsTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(SERVING_URI);
                    helper.notifyChange(HISTORY_URI);
                    helper.notifyChange(DAILY_TOTAL_URI);
                }
                return rowsUpdated;
            case HISTORY:
                rowsUpdated = sqlDB.delete(HistoryTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(HISTORY_URI);
                    helper.notifyChange(SERVING_URI);
                    helper.notifyChange(DAILY_TOTAL_URI);
                }
                return rowsUpdated;
            case DELETE_INVALID:
                rowsUpdated = sqlDB.delete(FoodsTable.TABLE,
                        "NOT EXISTS (SELECT servings._id FROM servings WHERE servings.food = foods._id)",
                        null);
                if (rowsUpdated > 0) {
                    helper.notifyChange(FOOD_URI);
                    helper.notifyChange(SERVING_URI);
                    helper.notifyChange(HISTORY_URI);
                    helper.notifyChange(DAILY_TOTAL_URI);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = getWritableDatabase();
        long id;
        switch (uriType) {
            case FOODS:
                id = sqlDB.insertOrThrow(FoodsTable.TABLE, null, values);
                break;
            case SERVINGS:
                id = sqlDB.insertOrThrow(ServingsTable.TABLE, null, values);
                helper.notifyChange(HISTORY_URI);
                break;
            case HISTORY:
                id = sqlDB.insertOrThrow(HistoryTable.TABLE, null, values);
                helper.notifyChange(SERVING_URI);
                helper.notifyChange(DAILY_TOTAL_URI);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        helper.notifyChange(uri);
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = getWritableDatabase();
        int rowsUpdated;

        //If it's odd, then it has an ID appended.
        if ((uriType % 2) == 1) {
            String id = uri.getLastPathSegment();
            selection = appendSelection(selection, "_id = ?");
            selectionArgs = appendSelectionArg(selectionArgs, id);
            uriType--;
        }

        switch (uriType) {
            case FOODS:
                rowsUpdated = sqlDB.update(FoodsTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0)
                    helper.notifyChange(FOOD_URI);
                return rowsUpdated;
            case SERVINGS:
                rowsUpdated = sqlDB.update(ServingsTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(SERVING_URI);
                    helper.notifyChange(HISTORY_URI);
                    helper.notifyChange(DAILY_TOTAL_URI);
                }
                return rowsUpdated;
            case HISTORY:
                rowsUpdated = sqlDB.update(HistoryTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(HISTORY_URI);
                    helper.notifyChange(SERVING_URI);
                    helper.notifyChange(DAILY_TOTAL_URI);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private static String appendSelection(String original, String newSelection) {
        return DatabaseUtils.concatenateWhere(original, newSelection);
    }

    private static String[] appendSelectionArgs(String[] originalValues, String[] newValues) {
        if (originalValues == null) {
            return newValues;
        }
        if (newValues == null) {
            return originalValues;
        }
        return DatabaseUtils.appendSelectionArgs(originalValues, newValues);
    }

    private static String[] appendSelectionArg(String[] originalValues, String newValue) {
        return appendSelectionArgs(originalValues, new String[]{newValue});
    }

    private void setNotificationUri(Cursor cursor, Uri uri) {
        if (cursor != null) {
            Context context = getContext();
            if (context != null) {
                ContentResolver resolver = context.getContentResolver();
                if (resolver != null) {
                    cursor.setNotificationUri(resolver, uri);
                }
            }

        }
    }

    private SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db == null) {
            throw new SQLiteException("Could not open writable database");
        }
        return db;
    }

}
