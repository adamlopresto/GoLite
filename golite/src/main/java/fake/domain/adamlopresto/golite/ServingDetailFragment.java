package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.FoodsTable;
import fake.domain.adamlopresto.golite.db.HistoryTable;
import fake.domain.adamlopresto.golite.db.ServingsTable;
import fake.domain.adamlopresto.golite.db.ServingsView;

/**
 * A fragment representing a single food detail screen.
 * Contains all the servings for the food.
 * This fragment is either contained in a {@link ServingListActivity}
 * in two-pane mode (on tablets) or a {@link ServingDetailActivity}
 * on handsets.
 */
@SuppressWarnings("WeakerAccess")
public class ServingDetailFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private long food_id = -1L;
    @NotNull
    private EditText name;
    @NotNull
    private EditText notes;
    @NotNull
    private FoodServingAdapter adapter;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    private ViewHolder activeHolder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServingDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
            food_id = args.getLong(ARG_ITEM_ID, -1L);
    }

    @Override
    public void onAttach(@NotNull Activity activity){
        super.onAttach(activity);
        setListAdapter(adapter = new FoodServingAdapter(activity));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        //TODO: Fix deletion and editing.
        /*
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            int selected=0;
            MenuItem editItem;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked)
                    selected++;
                else
                    selected--;

                editItem.setVisible(1 == selected);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.cab_edit_delete, menu);
                editItem = menu.findItem(R.id.edit);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView lv = (ListView)parent;
                lv.setItemChecked(position, lv.isItemChecked(position));
                Toast.makeText(getActivity(), "Long clicked item", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        */
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_serving_detail, container, false);
        assert rootView != null;
        name = (EditText) rootView.findViewById(R.id.name);
        notes = (EditText) rootView.findViewById(R.id.notes);

        if (food_id != -1L) {
            LoaderManager manager = getLoaderManager();
            assert manager != null;
            manager.initLoader(0, null, this);
            manager.initLoader(1, null, this);
        }

        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPause() {
        super.onPause();
        saveData();

        if (activeHolder != null)
            activeHolder.updateAfterEdit();
    }

    private void saveData() {
        ContentValues values = new ContentValues(2);
        values.put(FoodsTable.COLUMN_NAME, name.getText().toString());
        values.put(FoodsTable.COLUMN_NOTES, notes.getText().toString());

        if (food_id == -1L) {
            Uri newItem = getActivity().getContentResolver().insert(GoLiteContentProvider.FOOD_URI, values);
            food_id = Long.parseLong(newItem.getLastPathSegment());
        } else {
            int numChanged = getActivity().getContentResolver().update(GoLiteContentProvider.FOOD_URI, values,
                    "_id = ?", DatabaseHelper.idToArgs(food_id));
            if (numChanged != 1) {
                Toast.makeText(getActivity(),
                        "Updated " + numChanged + " foods instead of exactly one. Report this as a bug.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.serving_detail, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_new == item.getItemId()){
            saveData();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = getActivity().getLayoutInflater().inflate(R.layout.alert_serving_edit, null);
            final EditText number  = (EditText)view.findViewById(R.id.number);
            final EditText units   = (EditText)view.findViewById(R.id.units);
            final EditText cal     = (EditText)view.findViewById(R.id.calories);
            final CheckBox visible = (CheckBox)view.findViewById(R.id.show_default);
            builder.setView(view);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContentValues values = new ContentValues(5);
                    values.put(ServingsTable.COLUMN_FOOD, food_id);
                    values.put(ServingsTable.COLUMN_NUMBER, number.getText().toString());
                    values.put(ServingsTable.COLUMN_UNIT, units.getText().toString());
                    values.put(ServingsTable.COLUMN_CAL, cal.getText().toString());
                    values.put(ServingsTable.COLUMN_LISTED, visible.isChecked() ? 1 : 0);
                    if (null == getActivity().getContentResolver()
                            .insert(GoLiteContentProvider.SERVING_URI, values))
                        Toast.makeText(getActivity(), "Failed to create serving", Toast.LENGTH_LONG)
                                .show();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0)
            return new CursorLoader(getActivity(),
                    GoLiteContentProvider.FOOD_URI,
                    new String[]{FoodsTable.COLUMN_NAME, FoodsTable.COLUMN_NOTES},
                    FoodsTable.COLUMN_ID + "=?", new String[]{String.valueOf(food_id)}, null
            );
        else
            return new CursorLoader(getActivity(),
                    GoLiteContentProvider.SERVING_URI,
                    new String[]{ServingsView.COLUMN_ID, ServingsView.COLUMN_NUMBER,
                            ServingsView.COLUMN_UNIT, ServingsView.COLUMN_CAL,
                            ServingsView.COLUMN_QUANTITY, ServingsView.COLUMN_HISTORY_ID},
                    ServingsView.COLUMN_FOOD + "=?", DatabaseHelper.idToArgs(food_id),
                    ServingsView.COLUMN_LISTED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == 0) {
            if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                name.setText(cursor.getString(0));
                notes.setText(cursor.getString(1));
            }
        } else {
            adapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class ViewHolder {
        EditText quantityView;
        TextView numberView;
        TextView unitsView;
        TextView caloriesView;
        TextView totalView;
        TextView totalLabel;

        double quantity;
        double calories;
        double total;

        long history_id = -1L;
        long serving_id = -1L;

        void extractFrom(@NotNull Cursor cursor) {
            serving_id = cursor.getLong(0);
            numberView.setText(NUMBER_FORMAT.format(cursor.getDouble(1)));
            unitsView.setText(cursor.getString(2));
            calories = cursor.getDouble(3);
            caloriesView.setText(NUMBER_FORMAT.format(calories));
            if (cursor.isNull(5)) {
                quantityView.setText("");
                totalView.setText("");
                history_id = -1L;
                totalLabel.setVisibility(View.INVISIBLE);
            } else {
                quantity = cursor.getDouble(4);
                quantityView.setText(NUMBER_FORMAT.format(quantity));
                history_id = cursor.getLong(5);
                total = quantity * calories;
                totalView.setText(NUMBER_FORMAT.format(total));
                totalLabel.setVisibility(View.VISIBLE);
            }
        }

        void updateAfterEdit() {
            Context ctx = quantityView.getContext();
            assert ctx != null;
            ContentResolver resolver = ctx.getContentResolver();
            assert resolver != null;
            double newQty = 0;
            try {
                //noinspection ConstantConditions
                newQty = Double.parseDouble(quantityView.getText().toString());
            } catch (NumberFormatException | NullPointerException ignored) {
                //noop
            }
            if (newQty == 0) {
                //Now empty
                if (history_id != -1L) {
                    //Had a value before, so delete
                    resolver.delete(GoLiteContentProvider.HISTORY_URI,
                            "_id = ?", DatabaseHelper.idToArgs(history_id));
                    history_id = -1L;
                    totalView.setText("");
                    totalLabel.setVisibility(View.INVISIBLE);
                }
                quantityView.setText("");
                quantity = 0;
            } else {
                //Now has a quantity
                total = newQty * calories;
                totalView.setText(NUMBER_FORMAT.format(total));
                totalLabel.setVisibility(View.VISIBLE);
                if (history_id == -1L) {
                    //Had no value, so create one
                    quantity = newQty;
                    ContentValues values = new ContentValues(3);
                    values.put(HistoryTable.COLUMN_DATE, ServingListFragment.activeDate);
                    values.put(HistoryTable.COLUMN_SERVING, serving_id);
                    values.put(HistoryTable.COLUMN_QUANTITY, newQty);
                    Uri newItem = resolver.insert(GoLiteContentProvider.HISTORY_URI, values);
                    if (newItem == null) {
                        Toast.makeText(ctx, "Could not create history item", Toast.LENGTH_SHORT).show();
                    } else {
                        history_id = Long.parseLong(newItem.getLastPathSegment());
                    }
                } else {
                    if (quantity != newQty) {
                        quantity = newQty;
                        ContentValues values = new ContentValues(1);
                        values.put(HistoryTable.COLUMN_QUANTITY, newQty);
                        resolver.update(GoLiteContentProvider.HISTORY_URI, values,
                                "_id = ?", DatabaseHelper.idToArgs(history_id));
                    }
                }
            }
        }
    }

    private class FoodServingAdapter extends ResourceCursorAdapter {

        public FoodServingAdapter(@NotNull Context context) {
            super(context, R.layout.detail_list_serving, null, 0);
        }

        /**
         * Bind an existing view to the data pointed to by cursor
         *
         * @param view    Existing view, returned earlier by newView
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.extractFrom(cursor);
        }

        @Nullable
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            assert view != null;
            final ViewHolder holder = new ViewHolder();
            holder.quantityView = (EditText) view.findViewById(R.id.quantity);
            holder.numberView = (TextView) view.findViewById(R.id.number);
            holder.unitsView = (TextView) view.findViewById(R.id.units);
            holder.caloriesView = (TextView) view.findViewById(R.id.calories);
            holder.totalView = (TextView) view.findViewById(R.id.total);
            holder.totalLabel = (TextView) view.findViewById(R.id.total_label);


            holder.quantityView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                        holder.updateAfterEdit();
                    return false;
                }
            });

            holder.quantityView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        activeHolder = holder;
                    else {
                        holder.updateAfterEdit();
                        activeHolder = null;
                    }
                }
            });

            view.setTag(holder);
            return view;
        }
    }
}
