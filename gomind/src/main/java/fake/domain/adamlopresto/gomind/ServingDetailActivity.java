package fake.domain.adamlopresto.gomind;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * An activity representing a single Serving detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ServingListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ServingDetailFragment}.
 */
@SuppressWarnings("WeakerAccess")
public class ServingDetailActivity extends AppCompatActivity {

    private ServingDetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serving_detail);

        // Show the Up button in the action bar.
        //noinspection ConstantConditions
        //FIXME
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            /*
            Bundle arguments = new Bundle();
            if (extras != null){
                arguments.putLong(ServingDetailFragment.ARG_ITEM_ID,
                    extras.getLong(ServingDetailFragment.ARG_ITEM_ID, -1L));

            String newItemName = extras.getString(ServingDetailFragment.ARG_FOOD_NAME);
            if (!TextUtils.isEmpty(newItemName))
                arguments.putString(ServingDetailFragment.ARG_FOOD_NAME, newItemName);

            */

            fragment = new ServingDetailFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.serving_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            finish();
            //getFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if (fragment == null)
                fragment = (ServingDetailFragment) getSupportFragmentManager().findFragmentById(R.id.serving_detail_container);
            fragment.addBarcode(scanResult.getContents());
        }
    }
}
