package fake.domain.adamlopresto.golite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.golite.db.DatabaseHelper;
import fake.domain.adamlopresto.golite.db.TotalsView;


public class WeeklyReviewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_review);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new WeeklyReviewFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weekly_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class WeeklyReviewFragment extends Fragment {

        public WeeklyReviewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            TableLayout rootView = (TableLayout) inflater.inflate(R.layout.fragment_weekly_review, container, false);
            Calendar calendar = GregorianCalendar.getInstance();
            String today = DatabaseHelper.DATE_FORMAT.format(calendar.getTime());
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int offset;
            switch(day) {
                case Calendar.SATURDAY:
                    offset = 1;
                    break;
                default:
                    offset = day + 1;
            }
            calendar.add(Calendar.DATE, -offset);
            String lastFriday = DatabaseHelper.DATE_FORMAT.format(calendar.getTime());

            ContentResolver resolver = getActivity().getContentResolver();
            Cursor cursor = resolver.query(GoLiteContentProvider.DAILY_TOTAL_URI,
                    new String[]{TotalsView.COLUMN_DATE, TotalsView.COLUMN_TOTAL},
                    TotalsView.COLUMN_DATE + " >= ?", new String[]{lastFriday}, TotalsView.COLUMN_DATE);
            cursor.moveToFirst();
            int total = 0;
            int totalDiff = 0;
            int max = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("calories_per_day_key", "1400"));
            @SuppressLint ("SimpleDateFormat") DateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
            while (!cursor.isAfterLast()){
                String dateAsString = cursor.getString(0);
                if (today.equals(dateAsString)){
                    inflater.inflate(R.layout.weekly_review_divider, rootView, true);
                    addRow(rootView, inflater, "Subtotal", total, totalDiff);
                }
                try {

                    Date date = DatabaseHelper.DATE_FORMAT.parse(dateAsString);
                    dateAsString = dayOfWeekFormat.format(date);
                } catch(Exception ignored){
                }


                int calories = cursor.getInt(1);
                total += calories;
                int overUnder = max-calories;

                addRow(rootView, inflater, dateAsString, calories, overUnder);
                totalDiff += overUnder;
                cursor.moveToNext();
            }
            cursor.close();
            inflater.inflate(R.layout.weekly_review_divider, rootView, true);
            addRow(rootView, inflater, "Total", total, totalDiff);


            return rootView;
        }

        private static void addRow(ViewGroup rootView, LayoutInflater inflater, CharSequence day, int calories, int overUnder){
            View row = inflater.inflate(R.layout.weekly_review_row, rootView, false);
            TextView dayView = ((TextView)row.findViewById(R.id.day));
            dayView.setText(day);

            TextView caloriesView = (TextView)row.findViewById(R.id.calories);
            caloriesView.setText(String.valueOf(calories));

            TextView overUnderView = (TextView)row.findViewById(R.id.diff);
            overUnderView.setText(String.valueOf(overUnder));

            if (overUnder < 0){
                final int color = 0xFFFF0000;
                dayView.setTextColor(color);
                caloriesView.setTextColor(color);
                overUnderView.setTextColor(color);
            }
            rootView.addView(row);
        }
    }
}
