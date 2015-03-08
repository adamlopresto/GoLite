package fake.domain.adamlopresto.golite;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ServingEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ServingEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServingEditFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SERVING_ID = "serving_id";
    private static final String ARG_FOOD_ID = "food_id";

    private long servingId;
    private long foodId;

    //Number of calories the nutrition info says per serving
    private EditText nutritionInfoCalories;
    //Number associated with a serving, per nutrition info
    private EditText nutritionInfoServingSize;
    //Units for a serving
    private EditText nutritionInfoUnits;
    private TextView unitsLabel;
    private TextView servingsCal;
    private EditText typicalServing;


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param servingID The id of this serving, if it already exists. -1L if it doesn't and needs to
     *                  be created.
     * @param foodId The id of the food this serving will attach to.
     * @return A new instance of fragment ServingEditFragment.
     */
    public static ServingEditFragment newInstance(long servingID, long foodId) {
        ServingEditFragment fragment = new ServingEditFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SERVING_ID, servingID);
        args.putLong(ARG_FOOD_ID, foodId);
        fragment.setArguments(args);
        return fragment;
    }

    public ServingEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            servingId = getArguments().getLong(ARG_SERVING_ID);
            foodId = getArguments().getLong(ARG_FOOD_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_serving_edit, container, false);
        nutritionInfoCalories    = (EditText) rootView.findViewById(R.id.nutrition_info_cal);
        nutritionInfoServingSize = (EditText) rootView.findViewById(R.id.nutrition_info_serving_size);
        nutritionInfoUnits       = (EditText) rootView.findViewById(R.id.units);
        unitsLabel               = (TextView) rootView.findViewById(R.id.units_label);
        servingsCal              = (TextView) rootView.findViewById(R.id.serving_cal);
        typicalServing           = (EditText) rootView.findViewById(R.id.typical_serving);

        TextWatcher updateCalories =  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //noop
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //noop
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCalories();
            }
        };

        nutritionInfoCalories.addTextChangedListener(updateCalories);
        nutritionInfoServingSize.addTextChangedListener(updateCalories);
        typicalServing.addTextChangedListener(updateCalories);

        nutritionInfoUnits.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //noop
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //noop
            }

            @Override
            public void afterTextChanged(Editable s) {
                unitsLabel.setText(nutritionInfoUnits.getText().toString());
            }
        });

        return rootView;
    }

    public void updateCalories(){
        double caloriesPerServing = Utils.getDoubleFromTextView(nutritionInfoCalories)
                * Utils.getDoubleFromTextView(typicalServing)
                / Utils.getDoubleFromTextView(nutritionInfoServingSize);
        servingsCal.setText(String.valueOf(caloriesPerServing));
    }

    /*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    */

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
