package nguyen.tipcalculator;

import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;
import android.content.SharedPreferences.Editor;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements OnEditorActionListener, OnClickListener {


    //define member variables for the widgets
    private TextView percentTV;
    private TextView tipPercentTV;
    private TextView totalTV;
    private EditText billET;
    private Button incrementButton;
    private Button decrementButton;
    private Button resetButton;

    //string instance variable
    private String billAmountString = "";
    private float billAmount;
    private float tipPercent = 0.15f;

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
        incrementButton = (Button) findViewById(R.id.incrementButton);
        decrementButton = (Button) findViewById(R.id.decrementButton);
        resetButton = (Button) findViewById(R.id.resetButton);

        //set listeners
        billET.setOnEditorActionListener(this);
        incrementButton.setOnClickListener(this);
        decrementButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

        //get the shared preferences
        savedValues = getSharedPreferences("savedValues", MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.incrementButton:
                tipPercent += 0.01;
                calculateAndDisplay();
                break;
            case R.id.decrementButton:
                tipPercent -= 0.01;
                if(tipPercent < 0) {
                    tipPercent = 0;
                }
                calculateAndDisplay();
                break;
            case R.id.resetButton:


                reset();
                break;
        }
    }

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

        //display the formatted results
        percentTV.setText(percent.format(percentAmount));
        tipPercentTV.setText(currency.format(tipAmount));
        totalTV.setText(currency.format(totalAmount));
    }

    private void reset() {
        billET.setText("");
        tipPercent = 0.15f;

        float tipAmount = 0;
        float totalAmount = 0;
        float percentAmount = 0;

        percentTV.setText(percent.format(percentAmount));
        tipPercentTV.setText(currency.format(tipAmount));
        totalTV.setText(currency.format(totalAmount));
    }

    @Override
    protected void onPause() {
        //save the instance variables
        Editor editor = savedValues.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.commit();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //get the instance variables
        billAmountString = savedValues.getString("billAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", 0.15f);

        //set the bill amount on its widget
        billET.setText(billAmountString);

        //calculate and display
        calculateAndDisplay();
    }
}
