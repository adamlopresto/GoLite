package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class CalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calculator, menu);
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
    public static class PlaceholderFragment extends Fragment {

        private EditText number;

        //curNum is the number we're currently typing.
        private final StringBuffer curNum = new StringBuffer(32);

        private double runningTotal = 0;
        private char operator;

        private enum State {
            EMPTY,    //No input yet
            NUM1,     //Working on the first number
            OPERATOR, //Have seen an operator, but no second number
            NUM2,     //Working on number2
            DONE,     //We've hit "=" after a calculation
        }

        private State state = State.EMPTY;

        private TextView doneButton;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);
            number = rootView.findViewById(R.id.result);
            doneButton = rootView.findViewById(R.id.done);
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case EMPTY:
                            //TODO, probably error
                            break;
                        case NUM1: {
                            Intent intent = new Intent();
                            intent.putExtra("result", Double.parseDouble(curNum.toString()));
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.setResult(Activity.RESULT_OK, intent);
                                //Toast.makeText(activity, "Returning result "+numberDbl, Toast.LENGTH_LONG).show();
                                activity.finish();
                            }
                        }
                        case OPERATOR:
                            //TODO, probably error
                            break;
                        case NUM2:
                            calc();
                            number.setText(Utils.NUMBER_FORMAT.format(runningTotal));
                            state = State.DONE;
                            doneButton.setText("DONE");
                            break;
                        case DONE: {
                            Intent intent = new Intent();
                            intent.putExtra("result", runningTotal);
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.setResult(Activity.RESULT_OK, intent);
                                //Toast.makeText(activity, "Returning result "+numberDbl, Toast.LENGTH_LONG).show();
                                activity.finish();
                            }
                        }
                    }
                }
            });

            View.OnClickListener addSelf = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doneButton.setEnabled(true);
                    String digit = ((TextView)v).getText().toString();
                    switch (state) {
                        case DONE:
                            curNum.setLength(0);
                            //fall through
                        case EMPTY:
                            curNum.append(digit);
                            state = State.NUM1;
                            number.setText(curNum.toString());
                            doneButton.setText("DONE");
                            break;
                        case NUM1:
                            curNum.append(digit);
                            number.setText(curNum.toString());
                            break;
                        case OPERATOR: {
                            Editable text = number.getText();
                            text.append(((TextView) v).getText());
                            curNum.setLength(0);
                            curNum.append(digit);
                            state = State.NUM2;
                            doneButton.setText("=");
                            break;
                        }
                        case NUM2: {
                            Editable text = number.getText();
                            text.append(digit);
                            curNum.append(digit);
                            break;
                        }
                    }
                }
            };

            rootView.findViewById(R.id.btn0).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn1).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn2).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn3).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn4).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn5).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn6).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn7).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn8).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btn9).setOnClickListener(addSelf);
            rootView.findViewById(R.id.btnDecimalPoint).setOnClickListener(addSelf);

            View.OnClickListener operatorClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    char oper = ((TextView)v).getText().charAt(0);
                    switch (state) {
                        case EMPTY:
                            //error;
                            break;
                        case NUM2:
                            calc();
                            //fall through
                        case DONE:
                            operator = oper;
                            number.setText(Utils.NUMBER_FORMAT.format(runningTotal)+oper);
                            state = State.OPERATOR;
                            break;
                        case NUM1:
                        case OPERATOR:
                            runningTotal = Double.parseDouble(curNum.toString());
                            operator = oper;
                            number.setText(curNum.toString()+oper);
                            state = State.OPERATOR;
                            break;
                    }

                    doneButton.setEnabled(false);
                }
            };
            rootView.findViewById(R.id.btnPlus).setOnClickListener(operatorClick);
            rootView.findViewById(R.id.btnMinus).setOnClickListener(operatorClick);
            rootView.findViewById(R.id.btnDiv).setOnClickListener(operatorClick);
            rootView.findViewById(R.id.btnTimes).setOnClickListener(operatorClick);


            return rootView;
        }

        private void calc(){
            double num2 = Double.parseDouble(curNum.toString());
            switch (operator){
                case '+':
                    runningTotal += num2;
                    break;
                case '-':
                    runningTotal -= num2;
                    break;
                case '×':
                    runningTotal *= num2;
                    break;
                case '÷':
                    runningTotal /= num2;
                    break;
            }
            curNum.setLength(0);
            curNum.append(runningTotal);
        }
    }
}
