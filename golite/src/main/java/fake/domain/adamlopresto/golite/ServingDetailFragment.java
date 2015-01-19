package fake.domain.adamlopresto.golite;

import android.annotation.SuppressLint;
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
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;

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
    public static final String ARG_FOOD_NAME = "food_name";

    private static final int FOOD_LOADER = 0;
    private static final int SERVINGS_LOADER = 1;

    private long food_id = -1L;
    @NotNull
    private EditText name;
    @NotNull
    private EditText notes;
    @NotNull
    private FoodServingAdapter adapter;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    private Collection<ViewHolder> activeHolders = new HashSet<>();

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
        if (food_id == -1L && savedInstanceState != null)
            food_id = savedInstanceState.getLong(ARG_ITEM_ID, -1L);

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
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_serving_detail, container, false);
        assert rootView != null;
        name = (EditText) rootView.findViewById(R.id.name);
        notes = (EditText) rootView.findViewById(R.id.notes);

        if (food_id == -1L) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                String foodName = arguments.getString(ARG_FOOD_NAME);
                if (!TextUtils.isEmpty(foodName)) {
                    foodName = Character.toUpperCase(foodName.charAt(0)) + foodName.substring(1);
                    name.setText(foodName);
                    createNewServing();
                }
            }
        } else {
            LoaderManager manager = getLoaderManager();
            assert manager != null;
            manager.initLoader(FOOD_LOADER, null, this);
            manager.initLoader(SERVINGS_LOADER, null, this);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_ITEM_ID, food_id);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateOnly();

        if (!activeHolders.isEmpty()){
            for (ViewHolder holder : activeHolders) {
                holder.updateAfterEdit();
            }
            activeHolders.clear();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean updateOrCreate() {
        if (food_id == -1L) {
            ContentValues values = new ContentValues(2);
            String nameStr = Utils.getText(name);
            values.put(FoodsTable.COLUMN_NAME, nameStr);
            values.put(FoodsTable.COLUMN_NOTES, Utils.getText(notes));
            try {
                if (TextUtils.isEmpty(nameStr)){
                    Toast.makeText(getActivity(), "Can't create a food without a name", Toast.LENGTH_LONG).show();
                    return false;
                }
                Uri newItem = getActivity().getContentResolver().insert(GoLiteContentProvider.FOOD_URI, values);
                food_id = Long.parseLong(newItem.getLastPathSegment());
                return true;
            } catch (SQLiteConstraintException e) {
                Toast.makeText(getActivity(), "Another food with that name already exists.", Toast.LENGTH_LONG).show();
                return false;
            } catch (Exception e){
                Utils.error(getActivity(), e);
                return false;
            }
        } else {
            return updateOnly();
        }
    }

    /**
     * Saves updated data only if it already exists. Does nothing if the food doesn't exist yet.
     */
    @SuppressWarnings("ConstantConditions")
    private boolean updateOnly() {
        ContentValues values = new ContentValues(2);
        String nameStr = Utils.getText(name);
        values.put(FoodsTable.COLUMN_NAME, nameStr);
        values.put(FoodsTable.COLUMN_NOTES, Utils.getText(notes));

        try {
            if (food_id == -1L) {
                Context context = getActivity();
                if (context != null) {
                    Toast.makeText(context, "Food has no servings listed; not saving", Toast.LENGTH_LONG).show();
                }
            } else {
                int numChanged = getActivity().getContentResolver().update(GoLiteContentProvider.FOOD_URI, values,
                        "_id = ?", DatabaseHelper.idToArgs(food_id));
                if (numChanged == 1)
                    return true;
                Utils.error(getActivity(), "Updated " + numChanged + " foods instead of exactly one.");
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(getActivity(), "Another food with that name already exists.", Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Utils.error(getActivity(), e);
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.serving_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_new == item.getItemId()){
            return createNewServing();
        }
        return false;
    }

    private boolean createNewServing() {
        if (updateOrCreate()) {
            final Context context = getActivity();
            assert context != null;
            showEditDialog(context, "1", "serving", "", food_id, -1L);
            getLoaderManager().restartLoader(SERVINGS_LOADER, null, this);
            return true;
        } else {
            Toast.makeText(getActivity(), "Set a food name first", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private static void showEditDialog(final Context context, CharSequence num, CharSequence unitsStr,
                                       CharSequence calories, final long food_id, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        @SuppressWarnings("ConstantConditions") @SuppressLint("InflateParams") View view =
                LayoutInflater.from(context).inflate(R.layout.alert_serving_edit, null);
        if (view == null) {
            Utils.error(context, "Could not instantiate dialog to create new serving");
            return;
        }
        final EditText number = (EditText) view.findViewById(R.id.number);
        final EditText units = (EditText) view.findViewById(R.id.units);
        final EditText cal = (EditText) view.findViewById(R.id.calories);
        number.setText(num);
        units.setText(unitsStr);
        cal.setText(calories);
        final Checkable visible = (Checkable) view.findViewById(R.id.show_default);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues values = new ContentValues(5);
                values.put(ServingsTable.COLUMN_FOOD, food_id);
                values.put(ServingsTable.COLUMN_NUMBER, Utils.getText(number));
                values.put(ServingsTable.COLUMN_UNIT, Utils.getText(units));
                values.put(ServingsTable.COLUMN_CAL, Utils.getText(cal));
                values.put(ServingsTable.COLUMN_LISTED, visible.isChecked() ? 1 : 0);
                try {
                    if (-1L == id) {
                        if (null == context.getContentResolver()
                                .insert(GoLiteContentProvider.SERVING_URI, values)) {
                            Utils.error(context, "Failed to create serving");
                        }
                    } else {
                        if (1 != context.getContentResolver()
                                .update(GoLiteContentProvider.SERVING_URI, values,
                                        "_id = ?", DatabaseHelper.idToArgs(id)
                                )) {
                            Utils.error(context, "Failed to update serving");
                        }
                    }
                } catch (Throwable t) {
                    Utils.error(context, t);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == FOOD_LOADER)
            return new CursorLoader(getActivity(),
                    GoLiteContentProvider.FOOD_URI,
                    new String[]{FoodsTable.COLUMN_NAME, FoodsTable.COLUMN_NOTES},
                    FoodsTable.COLUMN_ID + "=?", new String[]{String.valueOf(food_id)}, null
            );
        else //id == SERVINGS_LOADER
            return new CursorLoader(getActivity(),
                    Uri.withAppendedPath(GoLiteContentProvider.SERVING_DATED_HISTORY_URI,
                            ServingListFragment.activeDate),
                    new String[]{ServingsView.COLUMN_ID, ServingsView.COLUMN_NUMBER,
                            ServingsView.COLUMN_UNIT, ServingsView.COLUMN_CAL,
                            ServingsView.COLUMN_QUANTITY, ServingsView.COLUMN_HISTORY_ID,
                            ServingsView.COLUMN_FOOD},
                    ServingsView.COLUMN_FOOD + "=?", DatabaseHelper.idToArgs(food_id),
                    ServingsView.COLUMN_LISTED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == FOOD_LOADER) {
            if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                name.setText(cursor.getString(0));
                notes.setText(cursor.getString(1));
            }
        } else { //SERVINGS_LOADER
            adapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
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
        long food_id = -1L;

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
            food_id = cursor.getLong(6);
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
                    try {
                        Uri newItem = resolver.insert(GoLiteContentProvider.HISTORY_URI, values);
                        //noinspection ConstantConditions
                        history_id = Long.parseLong(newItem.getLastPathSegment());
                    } catch (Exception e){
                        Utils.error(ctx, new Throwable("Could not create history item", e));
                    }
                } else {
                    if (quantity != newQty) {
                        quantity = newQty;
                        ContentValues values = new ContentValues(1);
                        values.put(HistoryTable.COLUMN_QUANTITY, newQty);
                        try {
                            resolver.update(GoLiteContentProvider.HISTORY_URI, values,
                                    "_id = ?", DatabaseHelper.idToArgs(history_id));
                        } catch (Exception e){
                            Utils.error(ctx, e);
                        }
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            //noinspection ConstantConditions
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.cab_edit_delete);
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.edit:
                    showEditDialog(quantityView.getContext(), numberView.getText(),
                            unitsView.getText(), NUMBER_FORMAT.format(calories), food_id, serving_id);
                    return true;
                case R.id.delete:
                    //TODO
                    AlertDialog.Builder builder = new AlertDialog.Builder(quantityView.getContext());
                    builder.setMessage("Are you sure you want to delete this serving? " +
                            "All history associated with it will be deleted. " +
                            "This cannot be undone.");
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            quantityView.getContext().getContentResolver().delete(
                                    GoLiteContentProvider.SERVING_URI, ServingsView.COLUMN_ID+"=?",
                                    DatabaseHelper.idToArgs(serving_id));
                        }
                    });
                    builder.show();
                    return true;
            }
            return false;
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

            view.findViewById(R.id.overflow).setOnClickListener(holder);

            holder.quantityView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                        holder.updateAfterEdit();
                    return false;
                }
            });

            /*
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
            */
            holder.quantityView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    activeHolders.add(holder);
                }
            });

            view.setTag(holder);
            return view;
        }
    }
}
