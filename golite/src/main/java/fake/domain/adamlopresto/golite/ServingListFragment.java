package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Date;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.HistoryTable;
import fake.domain.adamlopresto.golite.db.ServingsView;

/**
 * A list fragment representing a list of Servings. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ServingDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ServingListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ServingsViewAdapter adapter;

    private static final int INDEX_SERVING_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_NUMBER = 2;
    private static final int INDEX_UNIT = 3;
    private static final int INDEX_CAL = 4;
    private static final int INDEX_FOOD = 5;
    private static final int INDEX_QUANTITY = 6;
    private static final int INDEX_HISTORY_ID = 7;

    private final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GoLiteContentProvider.SERVING_LISTED_URI,
                new String[]{ServingsView.COLUMN_ID,
                        ServingsView.COLUMN_NAME,
                        ServingsView.COLUMN_NUMBER,
                        ServingsView.COLUMN_UNIT,
                        ServingsView.COLUMN_CAL,
                        ServingsView.COLUMN_FOOD,
                        ServingsView.COLUMN_QUANTITY,
                        ServingsView.COLUMN_HISTORY_ID
                },
                null, null, ServingsView.COLUMN_NAME);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServingListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        setListAdapter(adapter = new ServingsViewAdapter());

        //noinspection ConstantConditions
        getLoaderManager().initLoader(0, null, this);

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        if (adapter != null) {
            Cursor cursor = adapter.getCursor();
            if (cursor != null && !cursor.isClosed()) {
               int pos = cursor.getPosition();
                if (cursor.moveToPosition(position)){
                    mCallbacks.onItemSelected(cursor.getLong(cursor.getColumnIndexOrThrow(ServingsView.COLUMN_FOOD)));
                    cursor.moveToPosition(pos);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        //noinspection ConstantConditions
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            //noinspection ConstantConditions
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            //noinspection ConstantConditions
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private class ServingsViewAdapter extends ResourceCursorAdapter{
        ServingsViewAdapter(){
            super(getActivity(), R.layout.main_list_serving,
                null, 0);
            /*
                new String[]{ServingsView.COLUMN_NAME, ServingsView.COLUMN_NUMBER, ServingsView.COLUMN_UNIT,
                ServingsView.COLUMN_CAL}, new int[]{R.id.name, R.id.number, R.id.units, R.id.calories}, 0);
            */
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            holder.name.setText(cursor.getString(INDEX_NAME));
            double qty = cursor.isNull(INDEX_QUANTITY) ? 1 : cursor.getDouble(INDEX_QUANTITY);
            holder.name.setChecked(!cursor.isNull(INDEX_HISTORY_ID));
            holder.number.setText(NUMBER_FORMAT.format(qty * cursor.getDouble(INDEX_NUMBER)));
            holder.unit.setText(cursor.getString(INDEX_UNIT));
            holder.cal.setText(NUMBER_FORMAT.format(qty * cursor.getDouble(INDEX_CAL)));

            holder.serving_id = cursor.getLong(INDEX_SERVING_ID);

            if (cursor.isNull(INDEX_HISTORY_ID)){
                holder.history_id = -1L;
            } else {
                holder.history_id = cursor.getLong(INDEX_HISTORY_ID);
            }

            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.name.isChecked()){
                        ContentValues cv = new ContentValues(3);
                        cv.put(HistoryTable.COLUMN_SERVING, holder.serving_id);
                        cv.put(HistoryTable.COLUMN_QUANTITY, 1);
                        cv.put(HistoryTable.COLUMN_DATE, DatabaseHelper.DATE_FORMAT.format(new Date()));
                        Uri newUri = v.getContext().getContentResolver().insert(GoLiteContentProvider.HISTORY_URI, cv);
                        holder.history_id = Long.valueOf(newUri.getLastPathSegment());
                    } else {
                        v.getContext().getContentResolver().delete(GoLiteContentProvider.HISTORY_URI,
                                HistoryTable.COLUMN_ID+"=?", DatabaseHelper.idToArgs(holder.history_id));
                    }

                }
            });
        }

        @NotNull
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            ViewHolder holder = new ViewHolder();
            holder.name   = (CheckBox)v.findViewById(R.id.name);
            holder.number = (TextView)v.findViewById(R.id.number);
            holder.unit   = (TextView)v.findViewById(R.id.units);
            holder.cal    = (TextView)v.findViewById(R.id.calories);
            v.setTag(holder);
            return v;
        }


    }

    private static class ViewHolder {
        CheckBox name;
        TextView number;
        TextView unit;
        TextView cal;

        long history_id = -1L;
        long serving_id = -1L;
    }
}

