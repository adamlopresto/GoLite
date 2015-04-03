package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class CalculatorActivity extends ActionBarActivity {

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

        private double num1 = 0;
        private double num2 = 0;
        private char operator;

        private enum State {
            EMPTY,    //No input yet
            NUM1,     //Working on the first number
            NUM1DEC,  //Working on first number, have seen a decimal point
            OPERATOR, //Have seen an operator, but no second number
            NUM2,     //Working on number2
            NUM2DEC   //Same, having seen decimal point
        }

        private State state = State.EMPTY;

        private TextView doneButton;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);
            number = (EditText)rootView.findViewById(R.id.result);
            doneButton = (TextView)rootView.findViewById(R.id.done);
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case EMPTY:
                            //TODO, probably error
                            break;
                        case NUM1:
                        case NUM1DEC:
                            Intent intent = new Intent();
                            intent.putExtra("result", num1);
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.setResult(Activity.RESULT_OK, intent);
                                //Toast.makeText(activity, "Returning result "+numberDbl, Toast.LENGTH_LONG).show();
                                activity.finish();
                            }
                        case OPERATOR:
                            //TODO, probably error
                        case NUM2:
                        case NUM2DEC:
                            calc();
                            number.setText(Utils.NUMBER_FORMAT.format(num1));
                            state = State.NUM1;
                            doneButton.setText("DONE");
                    }
                }
            });

            View.OnClickListener addSelf = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doneButton.setEnabled(true);
                    int digit = Integer.parseInt(((TextView)v).getText().toString());
                    switch (state) {
                        case EMPTY:
                            num1 = digit;
                            state = State.NUM1;
                            number.setText(Utils.NUMBER_FORMAT.format(num1));
                            doneButton.setText("DONE");
                            break;
                        case NUM1:
                            num1 *= 10;
                            num1 += digit;
                            number.setText(Utils.NUMBER_FORMAT.format(num1));
                            break;
                        case NUM1DEC:
                            //TODO
                            break;
                        case OPERATOR: {
                            Editable text = number.getText();
                            text.append(((TextView) v).getText());
                            num2 = digit;
                            state = State.NUM2;
                            doneButton.setText("=");
                            break;
                        }
                        case NUM2: {
                            Editable text = number.getText();
                            text.append(((TextView) v).getText());
                            num2 *= 10;
                            num2 += digit;
                            break;
                        }
                        case NUM2DEC:
                            //TODO
                            break;
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

            View.OnClickListener operatorClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    char oper = ((TextView)v).getText().charAt(0);
                    switch (state) {
                        case EMPTY:
                            //error;
                            break;
                        case NUM2:
                        case NUM2DEC:
                            calc();
                            //FALL THROUGH!
                        case NUM1:
                        case NUM1DEC:
                        case OPERATOR:
                            operator = oper;
                            number.setText(Utils.NUMBER_FORMAT.format(num1)+oper);
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
            switch (operator){
                case '+':
                    num1 += num2;
                    break;
                case '-':
                    num1 -= num2;
                    break;
                case '×':
                    num1 *= num2;
                    break;
                case '÷':
                    num1 /= num2;
                    break;
            }
            num2 = 0;
        }
    }
}
