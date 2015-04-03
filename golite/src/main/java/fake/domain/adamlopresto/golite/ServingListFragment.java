package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.HistoryTable;
import fake.domain.adamlopresto.golite.db.HistoryView;
import fake.domain.adamlopresto.golite.db.ServingsTable;
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
public class ServingListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        MenuItemCompat.OnActionExpandListener {

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
    public static boolean showAll = true;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private ServingsViewAdapter adapter;

    private TextView total;

    private String query;
    private MenuItem searchItem;
    private MenuItem scanItem;
    private MenuItem newItem;

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

                Uri uri = GoLiteContentProvider.SERVING_LISTED_URI;
                String selection = null;
                String[] selectionArgs = null;
                if (null != query){
                    selection = ServingsView.COLUMN_NAME + " LIKE ?";
                    selectionArgs = new String[]{"%"+query+"%"};
                    uri = Uri.withAppendedPath(GoLiteContentProvider.SERVING_DATED_HISTORY_URI, activeDate);
                } else if (!showAll){
                    uri = GoLiteContentProvider.HISTORY_URI;
                    selection = HistoryView.COLUMN_DATE + "=?";
                    selectionArgs = new String[]{activeDate};
                }

                return new CursorLoader(getActivity(), uri,
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

                Context context = getActivity();
                assert context != null;
                int max = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("calories_per_day_key", "1400"));
                int left = max-totalNum;
                if (left < 0){
                    total.setText(String.format("%d/%d, over by %d", totalNum, max, -left));
                    total.setBackgroundColor(context.getResources().getColor(R.color.backgroundOverLimit));
                } else if (left == 0){
                    total.setText(String.format("%d/%d", totalNum, max));
                    total.setBackgroundColor(context.getResources().getColor(R.color.backgroundRoomLeft));
                } else {
                    total.setText(String.format("%d/%d, %d left", totalNum, max, left));
                    total.setBackgroundColor(context.getResources().getColor(R.color.backgroundRoomLeft));
                }
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

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        ListView lv = getListView();
        if (lv == null){
            Utils.error(getActivity(), "Null list view");
            return;
        }
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            MenuItem editItem;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getContext().getMenuInflater().inflate(R.menu.cab_delete, menu);
                editItem = menu.findItem(R.id.edit);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        SparseBooleanArray items = getListView().getCheckedItemPositions();
                        assert items != null;
                        int size = items.size();
                        ListView listView = getListView();
                        assert listView != null;

                        ContentResolver resolver = getContext().getContentResolver();
                        ContentValues cv = new ContentValues(1);
                        cv.put(ServingsTable.COLUMN_LISTED, false);

                        for (int i = 0 ; i < size; i++){
                            //in theory, we shouldn't have false values. In theory.
                            if (items.valueAt(i)){
                                long serving_id = listView.getItemIdAtPosition(items.keyAt(i));
                                resolver.update(GoLiteContentProvider.SERVING_URI, cv, "_id = ?",
                                        DatabaseHelper.idToArgs(serving_id));
                            }
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView lv = (ListView) parent;
                lv.setItemChecked(position, lv.isItemChecked(position));
                return true;
            }
        });
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
    public void onAttach(@NotNull final Activity activity) {
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

        ActionBar actionBar = ((ActionBarActivity)activity).getSupportActionBar();
        if (actionBar == null){
            Utils.error(getContext(), "Null ActionBar");
            return;
        }
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        Context themedContext = actionBar.getThemedContext();
        if (themedContext == null)
            themedContext = activity;

        SpinnerAdapter adapter = new ArrayAdapter<>(themedContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, new String[]{
                "All", "Today", "Yesterday", "Other date"
        });

        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                Calendar cal = new GregorianCalendar();
                switch (itemPosition){
                    case 0: //All
                        activeDate = DatabaseHelper.DATE_FORMAT.format(cal.getTime());
                        showAll = true;
                        break;
                    case 2: //Yesterday
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                        // fall through
                    case 1: //Today
                        activeDate = DatabaseHelper.DATE_FORMAT.format(cal.getTime());
                        showAll = false;
                        break;
                    case 3: //other date
                        DatePickerDialog dlg = new DatePickerDialog(activity,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                                        activeDate = DatabaseHelper.DATE_FORMAT.format(cal.getTime());
                                        showAll = false;
                                        restartLoaders(true);
                                    }
                                }, cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH));
                        dlg.show();
                        return true;
                    default:
                        Utils.error(activity, "Unexpected dropdown item, position "+itemPosition+", id "+itemId);
                        return false;
                }
                restartLoaders(true);
                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchItem != null) {
            searchItem.collapseActionView();
        }
        query = null;
        getContext().getContentResolver().delete(GoLiteContentProvider.DELETE_INVALID_URI, null, null);
        restartLoaders(true);
    }

    private void restartLoaders(boolean both) {
        LoaderManager lm = getLoaderManager();
        if (lm != null) {
            lm.restartLoader(SERVINGS_ID, null, this);
            if (both)
                lm.restartLoader(TOTALS_ID, null, this);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            inflater.inflate(R.menu.main_menu, menu);
            searchItem = menu.findItem(R.id.search);
            MenuItemCompat.setOnActionExpandListener(searchItem, this);
            View searchView = MenuItemCompat.getActionView(searchItem);
            SearchViewCompat.setOnQueryTextListener(searchView, new SearchViewCompat.OnQueryTextListenerCompat(){
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(getContext(), "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
                    ServingListFragment.this.query = query;
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (TextUtils.isEmpty(newText)) {
                        query = null;
                    } else {
                        query = newText;
                    }
                    //TODO
                    restartLoaders(false);
                    return true;
                }
            });
            //searchView.setOnQueryTextListener(this);
            scanItem = menu.findItem(R.id.menu_scan);
            newItem = menu.findItem(R.id.menu_new);
        } catch (Exception e){
            Utils.error(getContext(), e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_new: {
                Intent intent = new Intent(getActivity(), ServingDetailActivity.class);
                if (!TextUtils.isEmpty(query))
                    intent.putExtra(ServingDetailFragment.ARG_FOOD_NAME, query);
                startActivity(intent);
                return true;
            }
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.weekly_review:
                startActivity(new Intent(getActivity(), WeeklyReviewActivity.class));
                return true;
            case R.id.menu_scan:
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan();
                return true;
            default:
                return false;
        }
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

        editItem(position);
    }

    private void editItem(int position) {
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
     * Gets the current activity to use as a Context, or throws an exception.
     * Guaranteed not to return null, to centralize error handling.
     * @return Activity the Activity we're connected to, if any.
     */
    @NotNull
    private Activity getContext(){
        Activity context = getActivity();
        if (context == null){
            Utils.error(null, "Activity is unexpectedly null");
            throw new AssertionError("Null context");
        }
        return context;
    }

    /**
     * Called when a menu item with {@link android.view.MenuItem#SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW}
     * is expanded.
     *
     * @param item Item that was expanded
     * @return true if the item should expand, false if expansion should be suppressed.
     */
    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        newItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        scanItem.setVisible(false);
        return true;
    }

    /**
     * Called when a menu item with {@link android.view.MenuItem#SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW}
     * is collapsed.
     *
     * @param item Item that was collapsed
     * @return true if the item should collapse, false if collapsing should be suppressed.
     */
    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        scanItem.setVisible(true);
        newItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        query = null;
        restartLoaders(false);
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
                cv.put(HistoryTable.COLUMN_DATE, activeDate);
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

