package nguyen.tipcalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;
import android.content.SharedPreferences.Editor;
import java.text.NumberFormat;

import static nguyen.tipcalculator.R.id.percentSeekBar;
import static nguyen.tipcalculator.R.id.percentTV;
import static nguyen.tipcalculator.R.id.roundingRadioGroup;

public class MainActivity extends AppCompatActivity
        implements OnEditorActionListener, OnKeyListener {

    //define member variables for the widgets
    private TextView percentTV;
    private TextView tipPercentTV;
    private TextView totalTV;
    private EditText billET;
//    private Button incrementButton;
//    private Button decrementButton;
    private Button applyButton;
    private Button resetButton;
    private SeekBar percentSeekBar;
    private RadioGroup RadioGroup;
    private RadioButton noneRadioButton;
    private RadioButton tipRadioButton;
    private RadioButton totalRadioButton;
    private Spinner splitSpinner;
    private TextView perPersonLabel;
    private TextView perPersonTV;

    //string instance variable
    private String billAmountString = "";
    private float billAmount;
    private float tipPercent = 0.15f;
    private float seekBarProgress;
    float splitAmount = 0;

    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;
    private int rounding = ROUND_NONE;
    private int split = 1;

    //format for percent and currency
    NumberFormat percent = NumberFormat.getPercentInstance();
    NumberFormat currency = NumberFormat.getCurrencyInstance();

    private SharedPreferences savedValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get reference to the widget
        percentTV = (TextView) findViewById(R.id.percentTV);
        tipPercentTV = (TextView) findViewById(R.id.tipPercentTV);
        totalTV = (TextView) findViewById(R.id.totalTV);
        billET = (EditText) findViewById(R.id.billET);
    //    incrementButton = (Button) findViewById(R.id.incrementButton);
    //    decrementButton = (Button) findViewById(R.id.decrementButton);
        applyButton = (Button) findViewById(R.id.applyButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        percentSeekBar = (SeekBar) findViewById(R.id.percentSeekBar);
        RadioGroup = (android.widget.RadioGroup) findViewById(R.id.roundingRadioGroup);
        noneRadioButton = (RadioButton) findViewById(R.id.noneRadioButton);
        tipRadioButton = (RadioButton) findViewById(R.id.tipRadioButton);
        totalRadioButton = (RadioButton) findViewById(R.id.totalRadioButton);
        splitSpinner = (Spinner) findViewById(R.id.splitSpinner);
        perPersonLabel = (TextView) findViewById(R.id.perPersonLabel);
        perPersonTV = (TextView) findViewById(R.id.perPersonTV);

        //set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        splitSpinner.setAdapter(adapter);

        //set listeners
        billET.setOnEditorActionListener(this);
        billET.setOnKeyListener(this);
        RadioGroup.setOnKeyListener(this);

        //named class as its listener
        ClickListener clickListener = new ClickListener();
    //    incrementButton.setOnClickListener(clickListner);
    //    decrementButton.setOnClickListener(clickListner);
        applyButton.setOnClickListener(clickListener);
        resetButton.setOnClickListener(clickListener);


        //anonymous class as its listener
        RadioGroup.setOnCheckedChangeListener(checkedChangeListener);

        //anonymous inner class as its listener
        splitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                split = position + 1;
                calculateAndDisplay();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        percentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarProgress = progress;
                tipPercent = seekBarProgress / 100;
                percentTV.setText(percent.format(tipPercent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });
        //get the shared preferences
        savedValues = getSharedPreferences("savedValues", MODE_PRIVATE);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(
                        billET.getWindowToken(), 0);
                    return true; //consume the event
            case KeyEvent.KEYCODE_DPAD_DOWN:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(
                        billET.getWindowToken(), 0);
                break; //don't consume the event
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(v.getId() == R.id.percentTV) {
                    calculateAndDisplay();
                }
                break; //don't consume the event
        }
        return false;
    }

    class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                /*
                case incrementButton:
                    tipPercent += 0.01;
                    calculateAndDisplay();
                    break;
                case R.id.decrementButton:
                    tipPercent -= 0.01;
                    if (tipPercent < 0) {
                        tipPercent = 0;
                    }
                    calculateAndDisplay();
                    break;
                */
                case R.id.applyButton:
                    seekBarProgress = percentSeekBar.getProgress();
                    tipPercent = seekBarProgress / 100;
                    calculateAndDisplay();
                    break;
                case R.id.resetButton:
                    reset();
                    break;
            }
        }
    }

    private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.noneRadioButton:
                    rounding = ROUND_NONE;
                    break;
                case R.id.tipRadioButton:
                    rounding = ROUND_TIP;
                    break;
                case R.id.totalRadioButton:
                    rounding = ROUND_TOTAL;
                    break;
            }
            calculateAndDisplay();
        }
    };

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        calculateAndDisplay();
        return false;
    }

    private void calculateAndDisplay() {
        billAmountString = billET.getEditableText().toString();

        if(billAmountString.equals("")) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }

        //calculate tip and total
        float tipAmount = billAmount * tipPercent;
        float totalAmount = tipAmount + billAmount;
        float percentAmount = tipPercent;

        if(rounding == ROUND_NONE) {
            tipAmount = billAmount * tipPercent;
            totalAmount = tipAmount + billAmount;
        } else if(rounding == ROUND_TIP) {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = tipAmount + billAmount;
        } else if(rounding == ROUND_TOTAL) {
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;
        }

        //calculate split amount and show / hide split amount widget
        splitAmount = 0;

        if(split == 1) {
            perPersonLabel.setVisibility(View.GONE);
            perPersonTV.setVisibility(View.GONE);
        } else {
            splitAmount = totalAmount / split;
            perPersonLabel.setVisibility(View.VISIBLE);
            perPersonTV.setVisibility(View.VISIBLE);
        }

        //display the formatted results
        percentTV.setText(percent.format(percentAmount));
        tipPercentTV.setText(currency.format(tipAmount));
        totalTV.setText(currency.format(totalAmount));
        perPersonTV.setText(currency.format(splitAmount));
    }

    private void reset() {
        billET.setText("");
        tipPercent = 0.15f;

        float tipAmount = 15;
        float totalAmount = 0;
        float percentAmount = 0;
        splitAmount = 0;

        percentSeekBar.setProgress(15);

        rounding = ROUND_NONE;
        noneRadioButton.setChecked(true);

        split = 0;
        splitSpinner.setSelection(split);

        calculateAndDisplay();
    }

    @Override
    protected void onPause() {
        //save the instance variables
        Editor editor = savedValues.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
       // editor.commit();
        editor.putInt("rounding", rounding);
        editor.putInt("split", split);
        editor.putFloat("progress", seekBarProgress).commit();

        editor.apply();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //get the instance variables
        billAmountString = savedValues.getString("billAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", 0.15f);
        rounding = savedValues.getInt("rounding", ROUND_NONE);
        split = savedValues.getInt("split", 1);
        seekBarProgress = savedValues.getFloat("progress", 15);

        //set the bill amount on its widget
        billET.setText(billAmountString);

        if(rounding == ROUND_NONE) {
            noneRadioButton.setChecked(true);
        } else if(rounding == ROUND_TIP) {
            tipRadioButton.setChecked(true);
        } else if(rounding == ROUND_TOTAL) {
            totalRadioButton.setChecked(true);
        }

        percentSeekBar.setProgress((int) seekBarProgress);

        //set split on spinner
        int position = split - 1;
        splitSpinner.setSelection(position);

        //calculate and display
        //calculateAndDisplay();
    }
}
