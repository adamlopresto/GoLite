package fake.domain.adamlopresto.gomind;


import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.ResourceCursorAdapter;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.zxing.integration.android.IntentIntegrator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import fake.domain.adamlopresto.gomind.db.DatabaseHelper;
import fake.domain.adamlopresto.gomind.db.HistoryTable;
import fake.domain.adamlopresto.gomind.db.HistoryView;
import fake.domain.adamlopresto.gomind.db.ServingsTable;
import fake.domain.adamlopresto.gomind.db.ServingsView;
import fake.domain.adamlopresto.gomind.db.TotalsView;

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
        MenuItem.OnActionExpandListener {

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
    private ActionBarDrawerToggle drawerToggle;

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

                Uri uri = GoMindContentProvider.SERVING_LISTED_URI;
                String selection = null;
                String[] selectionArgs = null;
                if (null != query){
                    selection = ServingsView.COLUMN_NAME + " LIKE ?";
                    selectionArgs = new String[]{"%"+query+"%"};
                    uri = Uri.withAppendedPath(GoMindContentProvider.SERVING_DATED_HISTORY_URI, activeDate);
                } else if (!showAll){
                    uri = GoMindContentProvider.HISTORY_URI;
                    selection = HistoryView.COLUMN_DATE + "=?";
                    selectionArgs = new String[]{activeDate};
                }

                return new CursorLoader(requireContext(), uri,
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
                return new CursorLoader(requireContext(), GoMindContentProvider.DAILY_TOTAL_URI,
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

                Context context = getContext();
                assert context != null;
                int max = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("calories_per_day_key", "1400"));
                int left = max-totalNum;
                if (left < 0){
                    total.setText(String.format(Locale.getDefault(), "%d/%d, over by %d", totalNum, max, -left));
                    total.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundOverLimit));
                } else if (left == 0){
                    total.setText(String.format(Locale.getDefault(), "%d/%d", totalNum, max));
                    total.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundRoomLeft));
                } else {
                    total.setText(String.format(Locale.getDefault(), "%d/%d, %d left", totalNum, max, left));
                    total.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundRoomLeft));
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
            Utils.error(requireContext(), "Null list view");
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
                requireActivity().getMenuInflater().inflate(R.menu.cab_delete, menu);
                editItem = menu.findItem(R.id.edit);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        SparseBooleanArray items = getListView().getCheckedItemPositions();
                        assert items != null;
                        int size = items.size();
                        ListView listView = getListView();
                        assert listView != null;

                        ContentResolver resolver = requireContext().getContentResolver();
                        ContentValues cv = new ContentValues(1);
                        cv.put(ServingsTable.COLUMN_LISTED, false);

                        for (int i = 0; i < size; i++) {
                            //in theory, we shouldn't have false values. In theory.
                            if (items.valueAt(i)) {
                                long serving_id = listView.getItemIdAtPosition(items.keyAt(i));
                                resolver.update(GoMindContentProvider.SERVING_URI, cv, "_id = ?",
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
    public void onAttach(@NotNull final Context context) {
        super.onAttach(context);

        setListAdapter(adapter = new ServingsViewAdapter(context));

        //noinspection ConstantConditions
        LoaderManager.getInstance(this).initLoader(SERVINGS_ID, null, this);
        LoaderManager.getInstance(this).initLoader(TOTALS_ID, null, this);

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchItem != null) {
            searchItem.collapseActionView();
        }
        query = null;
        requireContext().getContentResolver().delete(GoMindContentProvider.DELETE_INVALID_URI, null, null);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            inflater.inflate(R.menu.main_menu, menu);
            searchItem = menu.findItem(R.id.search);
            searchItem.setOnActionExpandListener(this);
            SearchManager searchManager = (SearchManager)requireContext().getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView)searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(requireContext(), "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
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
            Utils.error(requireContext(), e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_new: {
                Intent intent = new Intent(requireContext(), ServingDetailActivity.class);
                if (!TextUtils.isEmpty(query))
                    intent.putExtra(ServingDetailFragment.ARG_FOOD_NAME, query);
                startActivity(intent);
                return true;
            }
            case R.id.settings:
                startActivity(new Intent(requireContext(), SettingsActivity.class));
                return true;
            case R.id.weekly_review:
                startActivity(new Intent(requireContext(), WeeklyReviewActivity.class));
                return true;
            case R.id.menu_scan:
                IntentIntegrator integrator = new IntentIntegrator(requireActivity());
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
    public void onListItemClick(@NotNull ListView listView, @NotNull View view, int position, long id) {
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
    public void onSaveInstanceState(@NotNull Bundle outState) {
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
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
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
     * My own method to pass navigation items back to the fragment.
     * @param itemPosition The index of the selected item
     */
    public void onNavigationDrawerItemSelected(int itemPosition) {
        Calendar cal = new GregorianCalendar();
        switch (itemPosition){
            case 0: //All
                activeDate = DatabaseHelper.DATE_FORMAT.format(cal.getTime());
                showAll = true;
                break;
            case 2: //Specific days last week
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                cal.add(GregorianCalendar.DAY_OF_MONTH, 1-itemPosition);
                // fall through
            case 1: //Today
                activeDate = DatabaseHelper.DATE_FORMAT.format(cal.getTime());
                showAll = false;
                break;
            case 9: //other date
                DatePickerDialog dlg = new DatePickerDialog(requireContext(),
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
                break;
            default:
                Utils.error(requireContext(), "Unexpected dropdown item, position "+itemPosition);
        }
        restartLoaders(true);
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
        void onItemSelected(long id);
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
                Uri newUri = v.getContext().getContentResolver().insert(GoMindContentProvider.HISTORY_URI, cv);
                assert newUri != null;
                history_id = Long.parseLong(newUri.getLastPathSegment());
            } else {
                v.getContext().getContentResolver().delete(GoMindContentProvider.HISTORY_URI,
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
            holder.nameView = v.findViewById(R.id.name);
            holder.numberView = v.findViewById(R.id.number);
            holder.unitView = v.findViewById(R.id.units);
            holder.calView = v.findViewById(R.id.calories);

            holder.nameView.setOnClickListener(holder);
            v.setTag(holder);
            return v;
        }


    }
}

