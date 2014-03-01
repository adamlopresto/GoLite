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
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Date;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.HistoryTable;
import fake.domain.adamlopresto.golite.db.ServingsView;
import fake.domain.adamlopresto.golite.db.TotalsView;

/**
 * A list fragment representing a list of Servings. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ServingDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
@SuppressWarnings("WeakerAccess")
public class ServingListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final int INDEX_SERVING_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_NUMBER = 2;
    private static final int INDEX_UNIT = 3;
    private static final int INDEX_CAL = 4;
    private static final int INDEX_FOOD = 5;
    private static final int INDEX_QUANTITY = 6;
    private static final int INDEX_HISTORY_ID = 7;
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
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private static final int SERVINGS_ID = 0;
    private static final int TOTALS_ID = 1;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public static String activeDate = DatabaseHelper.DATE_FORMAT.format(new Date());
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private ServingsViewAdapter adapter;

    private TextView total;

    private String query;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServingListFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case SERVINGS_ID:

                String selection = null;
                String[] selectionArgs = null;
                if (null != query){
                    selection = ServingsView.COLUMN_NAME + " LIKE ?";
                    selectionArgs = new String[]{"%"+query+"%"};
                }

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
                        selection, selectionArgs, ServingsView.COLUMN_NAME);
            case TOTALS_ID:
                return new CursorLoader(getActivity(), GoLiteContentProvider.DAILY_TOTAL_URI,
                        new String[]{TotalsView.COLUMN_TOTAL},
                        "date = ?", new String[]{activeDate},
                        null
                );
            default:
                throw new AssertionError("Attempting to load a Loader for invalid id");
        }
    }

    @Override
    public void onLoadFinished(@NotNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case SERVINGS_ID:
                adapter.swapCursor(data);
                break;
            case TOTALS_ID:
                int totalNum = 0;
                if (data.moveToFirst())
                    totalNum = data.getInt(0);

                assert getActivity() != null;
                int max = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("calories_per_day_key", "1400"));
                total.setText(String.format("%d/%d", totalNum, max));
                if (totalNum > max)
                    total.setBackgroundColor(0xFFFF8888);
                else
                    total.setBackgroundColor(0xFF88FF88);

                break;
            default:
                throw new AssertionError("Loader returned an invalid id");
        }
    }

    @Override
    public void onLoaderReset(@NotNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case SERVINGS_ID:
                adapter.swapCursor(null);
                break;
            case TOTALS_ID:
                total.setText("0");
                break;
            default:
                throw new AssertionError("Loader returned an invalid id");
        }
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(android.os.Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        total = (TextView) view.findViewById(R.id.total);

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

        setListAdapter(adapter = new ServingsViewAdapter(activity));

        //noinspection ConstantConditions
        getLoaderManager().initLoader(SERVINGS_ID, null, this);
        getLoaderManager().initLoader(TOTALS_ID, null, this);

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        query = null;
        getLoaderManager().restartLoader(SERVINGS_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
                if (cursor.moveToPosition(position)) {
                    mCallbacks.onItemSelected(cursor.getLong(INDEX_FOOD));
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

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(getActivity(), "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            query = null;
        } else {
            query = newText;
        }
        //TODO
        getLoaderManager().restartLoader(SERVINGS_ID, null, this);
        return true;
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

    private static class ViewHolder implements View.OnClickListener {
        CheckBox nameView;
        TextView numberView; //shows number*qty if qty is set
        TextView unitView;
        TextView calView;

        double number; //alwasy the number in one serving
        double cal; //always the calories for one serving; display will adjust

        long history_id = -1L;
        long serving_id = -1L;

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (nameView.isChecked()) {
                ContentValues cv = new ContentValues(3);
                cv.put(HistoryTable.COLUMN_SERVING, serving_id);
                cv.put(HistoryTable.COLUMN_QUANTITY, 1);
                cv.put(HistoryTable.COLUMN_DATE, DatabaseHelper.DATE_FORMAT.format(new Date()));
                //noinspection ConstantConditions
                Uri newUri = v.getContext().getContentResolver().insert(GoLiteContentProvider.HISTORY_URI, cv);
                assert newUri != null;
                history_id = Long.valueOf(newUri.getLastPathSegment());
            } else {
                //noinspection ConstantConditions
                v.getContext().getContentResolver().delete(GoLiteContentProvider.HISTORY_URI,
                        HistoryTable.COLUMN_ID + "=?", DatabaseHelper.idToArgs(history_id));

                numberView.setText(NUMBER_FORMAT.format(number));
                calView.setText(NUMBER_FORMAT.format(cal));
            }

        }
    }

    private class ServingsViewAdapter extends ResourceCursorAdapter {
        ServingsViewAdapter(Context context) {
            super(context, R.layout.main_list_serving, null, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            holder.nameView.setText(cursor.getString(INDEX_NAME));
            double qty = cursor.isNull(INDEX_QUANTITY) ? 1 : cursor.getDouble(INDEX_QUANTITY);
            holder.nameView.setChecked(!cursor.isNull(INDEX_HISTORY_ID));
            holder.number = cursor.getDouble(INDEX_NUMBER);
            holder.numberView.setText(NUMBER_FORMAT.format(qty * holder.number));
            holder.unitView.setText(cursor.getString(INDEX_UNIT));
            holder.cal = cursor.getDouble(INDEX_CAL);
            holder.calView.setText(NUMBER_FORMAT.format(qty * holder.cal));

            holder.serving_id = cursor.getLong(INDEX_SERVING_ID);

            if (cursor.isNull(INDEX_HISTORY_ID)) {
                holder.history_id = -1L;
            } else {
                holder.history_id = cursor.getLong(INDEX_HISTORY_ID);
            }

        }

        @NotNull
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            assert v != null;
            ViewHolder holder = new ViewHolder();
            holder.nameView = (CheckBox) v.findViewById(R.id.name);
            holder.numberView = (TextView) v.findViewById(R.id.number);
            holder.unitView = (TextView) v.findViewById(R.id.units);
            holder.calView = (TextView) v.findViewById(R.id.calories);

            holder.nameView.setOnClickListener(holder);
            v.setTag(holder);
            return v;
        }


    }
}

