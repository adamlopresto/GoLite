package fake.domain.adamlopresto.golite;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import org.jetbrains.annotations.NotNull;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.FoodsTable;
import fake.domain.adamlopresto.golite.db.ServingsView;

/**
 * A fragment representing a single food detail screen.
 * Contains all the servings for the food.
 * This fragment is either contained in a {@link ServingListActivity}
 * in two-pane mode (on tablets) or a {@link ServingDetailActivity}
 * on handsets.
 */
@SuppressWarnings("WeakerAccess")
public class ServingDetailFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private long food_id = -1L;
    private EditText name;
    private EditText notes;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServingDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the dummy content specified by the fragment
        // arguments. In a real-world scenario, use a Loader
        // to load content from a content provider.
        Bundle args = getArguments();
        if (args != null)
            food_id = args.getLong(ARG_ITEM_ID, -1L);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_serving_detail, container, false);

        if (rootView == null) throw new AssertionError();
        name = (EditText) rootView.findViewById(R.id.name);
        notes = (EditText) rootView.findViewById(R.id.notes);

        if (food_id != -1L){
            LoaderManager manager = getLoaderManager();
            assert manager != null;
            manager.initLoader(0, null, this);
            manager.initLoader(1, null, this);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0)
            return new CursorLoader(getActivity(),
                    GoLiteContentProvider.FOOD_URI,
                    new String[]{FoodsTable.COLUMN_NAME, FoodsTable.COLUMN_NOTES},
                    FoodsTable.COLUMN_ID+"=?", new String[]{String.valueOf(food_id)}, null
            );
        else
            return new CursorLoader(getActivity(),
                    GoLiteContentProvider.SERVING_URI,
                    new String[]{ServingsView.COLUMN_ID, ServingsView.COLUMN_NUMBER,
                            ServingsView.COLUMN_UNIT, ServingsView.COLUMN_CAL},
                    ServingsView.COLUMN_FOOD+"=?", DatabaseHelper.idToArgs(food_id),
                    ServingsView.COLUMN_LISTED+" DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.e("GoLite", "onLoadFinished, with "+loader+" and "+cursor);
        if (loader.getId() == 0){
            if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                name.setText(cursor.getString(0));
                notes.setText(cursor.getString(1));
            }
        } else {
            setListAdapter(new SimpleCursorAdapter(getActivity(), R.layout.detail_list_serving,
                    cursor,
                    new String[]{ServingsView.COLUMN_NUMBER, ServingsView.COLUMN_UNIT,
                                        ServingsView.COLUMN_CAL},
                    new int[]{R.id.number, R.id.units, R.id.calories},
                    0
            ));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
