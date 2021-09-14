package com.example.mobileleminh;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private AutoCompleteTextView actv_property_type;
    private ArrayList<String> propertyArrayList;
    private ArrayAdapter<String> arrayAdapter;
    private AutoCompleteTextView actv_bedroom;
    private ArrayList<String> bedroomArray;
    private TextInputEditText edtxt_dateTime;
    private TextInputEditText edtxt_monthlyPrice;
    private TextInputEditText edtxt_note;
    private TextInputEditText edtxt_nameOfReporter;
    private AppCompatButton btn_submit;

    private MaterialRadioButton rb_furnished;
    private MaterialRadioButton rb_unfurnished;
    private MaterialRadioButton rb_partfurnished;

    int day, month, year, hour, minute;
    int mday, mMonth, mYear, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hook();
        setPropertyType();
        setBedroom();
        setDateTime();
        validate();
        watcher();



    }

    private void watcher() {
        actv_property_type.addTextChangedListener(propertyWatcher);
        actv_bedroom.addTextChangedListener(bedroomWatcher);
        edtxt_dateTime.addTextChangedListener(dateTimeWatcher);
        edtxt_monthlyPrice.addTextChangedListener(monthlyPriceWatcher);
        edtxt_nameOfReporter.addTextChangedListener(nameReporterWatcher);

    }


    private void validate() {
        btn_submit.setOnClickListener(view -> {
            String property = Objects.requireNonNull(actv_property_type.getText()).toString().trim();
            String bedroom = Objects.requireNonNull(actv_bedroom.getText()).toString().trim();
            String dateTime = Objects.requireNonNull(edtxt_dateTime.getText()).toString().trim();
            String price = Objects.requireNonNull(edtxt_monthlyPrice.getText()).toString().trim();
            String name = Objects.requireNonNull(edtxt_nameOfReporter.getText()).toString().trim();
            String note = Objects.requireNonNull(edtxt_note.getText()).toString().trim();
            String furnitureType = getFurnitureType().trim();

            if(TextUtils.isEmpty(property)){
                TextInputLayout til = findViewById(R.id.til_propertyType);
                til.setError(getString(R.string.validate_property_error_txt));
                til.requestFocus();
            }

            if (TextUtils.isEmpty(bedroom)){
                TextInputLayout til = findViewById(R.id.til_bedroom);
                til.setError(getString(R.string.validate_bedrooms_error_txt));
                til.requestFocus();
            }

            if(TextUtils.isEmpty(dateTime)){
                TextInputLayout til = findViewById(R.id.til_date_time);
                til.setError(getString(R.string.validate_date_time_error_txt));
                til.requestFocus();
            }

            if(TextUtils.isEmpty(price)){
                TextInputLayout til = findViewById(R.id.til_monthly_price);
                til.setError(getString(R.string.validate_monthly_price));
                til.requestFocus();
            }

            if(TextUtils.isEmpty(name)){
                TextInputLayout til = findViewById(R.id.til_name_reporter);
                til.setError(getString(R.string.validate_name_of_reporter));
                til.requestFocus();
            }

            if(TextUtils.isEmpty(property) || TextUtils.isEmpty(bedroom) || TextUtils.isEmpty(dateTime) || TextUtils.isEmpty(price) || TextUtils.isEmpty(name)){
                Toast.makeText(MainActivity.this,getString(R.string.empty_form),Toast.LENGTH_SHORT).show();

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
            }else{
                submit(property,bedroom,dateTime,furnitureType,price,note,name);
            }
        });





    }

    private void submit(String property, String bedroom, String dateTime, String furnitureType, String price, String note, String name) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message));
        builder.setCancelable(true);

        builder.setPositiveButton(getString(R.string.positive_btn), (dialogInterface, i) -> {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String,Object> rentalInfo = new HashMap<>();
                rentalInfo.put("propertyType",property);
                rentalInfo.put("bedroom",bedroom);
                rentalInfo.put("dateTime",dateTime);
                rentalInfo.put("furnitureType",furnitureType);
                rentalInfo.put("monthlyProce",price);
                rentalInfo.put("note",note);
                rentalInfo.put("nameOfReporter",name);
                db.collection("rental").add(rentalInfo).addOnSuccessListener(documentReference -> {
                   Toast.makeText(MainActivity.this,getString(R.string.positive_result_toast),Toast.LENGTH_SHORT).show();
                   dialogInterface.cancel();
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,getString(R.string.txt_failure_storage),Toast.LENGTH_SHORT).show();
                    dialogInterface.cancel();
                });
        });
        builder.setNegativeButton(getString(R.string.negative_btn), (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private String getFurnitureType(){
        String type = " ";
        if(rb_furnished.isChecked()){
            type = getString(R.string.rb_furnished);
        }
        if(rb_unfurnished.isChecked()){
            type = getString(R.string.rb_unfurnished);
        }
        if(rb_partfurnished.isChecked()){
            type = getString(R.string.rb_part_furnished);
        }
        return type;
    }


    private void setDateTime()  {
        edtxt_dateTime.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, MainActivity.this,year, month,day);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        mYear = year;
        mday = day;
        mMonth = month;
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, MainActivity.this, hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        mHour = i;
        mMinute = i1;
        String m;
        if(mMinute < 10){
            m= "0"+mMinute+"";
        }else{
            m = mMinute+"";
        }
        edtxt_dateTime.setText(mYear + "/" + mMonth + "/" + mday + " " + mHour + ":" + m);
    }

    private void setBedroom() {
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),R.layout.dropdown_item_list,bedroomArray);
        actv_bedroom.setAdapter(arrayAdapter);
        actv_bedroom.setThreshold(1);
    }

    private void setPropertyType(){
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),R.layout.dropdown_item_list,propertyArrayList);
        actv_property_type.setAdapter(arrayAdapter);
        actv_property_type.setThreshold(1);
    }

    //Property Watcher
    private final TextWatcher propertyWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_name_reporter);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_property_error_txt));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }

        }
    };

    //Bedroom watcher
    private final TextWatcher bedroomWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_bedroom);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_bedrooms_error_txt));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }

        }
    };

    //Date time watcher
    private final TextWatcher dateTimeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_date_time);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_date_time_error_txt));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }

        }
    };

    //Price watcher
    private final TextWatcher monthlyPriceWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_monthly_price);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_monthly_price));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }

        }
    };

    //Name watcher
    private final TextWatcher nameReporterWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_name_reporter);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_name_of_reporter));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }

        }
    };

    private void hook(){
        actv_property_type = findViewById(R.id.actxt_propertyType);
        actv_bedroom = findViewById(R.id.actxt_bedroom);
        edtxt_dateTime = findViewById(R.id.edtxt_date_time);
        edtxt_monthlyPrice = findViewById(R.id.edtxt_monthly_price);
        edtxt_note = findViewById(R.id.edtxt_note);
        edtxt_nameOfReporter = findViewById(R.id.edtxt_name);
        rb_furnished = findViewById(R.id.rb_furnished);
        rb_unfurnished = findViewById(R.id.rb_unfurnished);
        rb_partfurnished = findViewById(R.id.rb_part_furnished);
        btn_submit = findViewById(R.id.btn_submit);

        propertyArrayList = new ArrayList<>();
        propertyArrayList.add("House");
        propertyArrayList.add("Flat");
        propertyArrayList.add("Apartment");
        propertyArrayList.add("Bugalow");

        bedroomArray = new ArrayList<>();
        bedroomArray.add("Studio");
        for(int i = 1; i <= 20; i++){
            if(i < 10 )
            bedroomArray.add(i+"");
            else{
                bedroomArray.add(i+"");
            }
        }
    }
}