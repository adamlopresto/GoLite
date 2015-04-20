package fake.domain.adamlopresto.golite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
public class ServingListActivity extends ActionBarActivity
        implements ServingListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ActionBarDrawerToggle drawerToggle;

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

        final ListView drawerList;
        drawerList = (ListView) findViewById(R.id.left_drawer);
        String[] dateChoices = new String[10];
        dateChoices[0] = "All";
        dateChoices[1] = "Today";
        dateChoices[2] = "Yesterday";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        @SuppressLint ("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("EEEE");
        for (int i = 3 ; i <= 8 ; ++i){
            dateChoices[i] = dateFormat.format(cal.getTime());
            cal.add(Calendar.DATE, -1);
        }
        dateChoices[9] = "Other date";
        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                dateChoices));

        final DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        drawerList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        drawerLayout.closeDrawers();

                        ((ServingListFragment) getFragmentManager()
                                .findFragmentById(R.id.serving_list)).onNavigationDrawerItemSelected(position);
                    }
                });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_opened, R.string.drawer_closed){

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
            if(scanResult!=null){
                final String barcode = scanResult.getContents();
                new AsyncTask<String, Void, String>() {
                    /**
                     * Override this method to perform a computation on a background thread. The
                     * specified parameters are the parameters passed to {@link #execute}
                     * by the caller of this task.
                     * <p/>
                     * This method can call {@link #publishProgress} to publish updates
                     * on the UI thread.
                     *
                     * @param params The parameters of the task.
                     * @return A result, defined by the subclass of this task.
                     * @see #onPreExecute()
                     * @see #onPostExecute
                     * @see #publishProgress
                     */
                    @Override
                    protected String doInBackground(String... params) {
                        String barcode = params[0];
                        if (!TextUtils.isEmpty(barcode)) {
                            SQLiteDatabase db = DatabaseHelper.getInstance(ServingListActivity.this).getReadableDatabase();
                            Cursor cursor = db.query(BarcodesTable.TABLE, new String[]{BarcodesTable.COLUMN_FOOD},
                                    BarcodesTable.COLUMN_BARCODE + "=?", new String[]{barcode},
                                    null, null, null);
                            cursor.moveToFirst();
                            if (!cursor.isAfterLast()) {
                                long food_id = cursor.getLong(0);
                                cursor.close();
                                Intent detailIntent = new Intent(ServingListActivity.this, ServingDetailActivity.class);
                                detailIntent.putExtra(ServingDetailFragment.ARG_ITEM_ID, food_id);
                                startActivity(detailIntent);
                                return null;
                            } else {
                                cursor.close();
                                return Utils.getNameFromBarcode(barcode);
                            }
                        }
                        return null;
                    }

                    /**
                     * <p>Runs on the UI thread after {@link #doInBackground}. The
                     * specified result is the value returned by {@link #doInBackground}.</p>
                     * <p/>
                     * <p>This method won't be invoked if the task was cancelled.</p>
                     *
                     * @param s The result of the operation computed by {@link #doInBackground}.
                     * @see #onPreExecute
                     * @see #doInBackground
                     * @see #onCancelled(Object)
                     */
                    @Override
                    protected void onPostExecute(String s) {
                        if (s != null) {
                            Intent detailIntent = new Intent(ServingListActivity.this, ServingDetailActivity.class);
                            if (!"null".equals(s))
                                detailIntent.putExtra(ServingDetailFragment.ARG_FOOD_NAME, s);
                            detailIntent.putExtra(ServingDetailFragment.ARG_BARCODE, barcode);
                            startActivity(detailIntent);
                        }
                    }
                }.execute(barcode);
            }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
}
