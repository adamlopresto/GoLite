package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import fake.domain.adamlopresto.golite.db.BarcodesTable;
import fake.domain.adamlopresto.golite.db.DatabaseHelper;


/**
 * An activity representing a list of Servings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ServingDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ServingListFragment} and the item details
 * (if present) is a {@link ServingDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ServingListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ServingListActivity extends Activity
        implements ServingListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serving_list);

        if (findViewById(R.id.serving_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            //noinspection ConstantConditions
            ((ServingListFragment) getFragmentManager()
                    .findFragmentById(R.id.serving_list))
                    .setActivateOnItemClick(true);
        }

    }
    /**
     * Callback method from {@link ServingListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(ServingDetailFragment.ARG_ITEM_ID, id);
            ServingDetailFragment fragment = new ServingDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.serving_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ServingDetailActivity.class);
            detailIntent.putExtra(ServingDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();
            Cursor cursor = db.query(BarcodesTable.TABLE, new String[]{BarcodesTable.COLUMN_FOOD},
                    BarcodesTable.COLUMN_BARCODE + "=?", new String[]{scanResult.getContents()},
                    null, null, null);
            if (!cursor.isAfterLast()){
                cursor.moveToFirst();
                long food_id = cursor.getLong(0);
                Intent detailIntent = new Intent(this, ServingDetailActivity.class);
                detailIntent.putExtra(ServingDetailFragment.ARG_ITEM_ID, food_id);
                startActivity(detailIntent);

            } else {
                Toast.makeText(this, "No food found with barcode "+scanResult.getContents(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
