package com.example.smartgasapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class DeliveryMethod extends AppCompatActivity {
    private Button order;
    private RadioGroup radioGroup;
    public Spinner Time_Spinner;
    public static int delivery_method;
    private TextView textView,textTime;
    DatePickerDialog.OnDateSetListener pickerDialog;
    Calendar calendar = Calendar.getInstance();
    TimePickerDialog.OnTimeSetListener timeDialog;
    Calendar calendar1 = Calendar.getInstance();
    public static String date,time,Time_Select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_method);
        order = findViewById(R.id.ConfrimOrder);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        OrderDetail orderDetail = new OrderDetail();
        orderDetail.edit = false;

        textView = findViewById(R.id.textView2);
        //textTime = findViewById(R.id.textView);
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.check(R.id.deliverOption);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                checkedId = radioGroup.getCheckedRadioButtonId();
                if(checkedId==R.id.deliverOption){
                    delivery_method = 0;
                }
                else{
                    delivery_method = 1;
                }
            }
        });
        Log.i("delivery_method", String.valueOf(delivery_method));
        TimePick();
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePick();
                Intent intent = new Intent(DeliveryMethod.this, OrderDetail.class);
                startActivity(intent);
                orderDetail.edit = true;
            }
        });

        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(),UserDashboard.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(),Homepage.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(getApplicationContext(),OrderListUnfinished.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        Date();
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(v);
            }
        });

        /*Time();
        textTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker(v);
            }
        });*/
    }
    public void Date(){
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Taipei");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(timeZone);

        // Format the current date and time as a string in the correct format
        String currentDateTimeString = dateFormat.format(new Date());
        textView.setText(currentDateTimeString);
        date = currentDateTimeString;
        pickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DATE,dayOfMonth);
                date = year+"-"+(month+1)+"-"+dayOfMonth+" ";
                textView.setText("日期："+year+"/"+(month+1)+"/"+dayOfMonth);
            }
        };
    }
    public void Time(){
        // Format the current date and time as a string in the correct format
        String currentDateTimeString = "19:00:00";
        textTime.setText("時間: 19時00分");
        time = currentDateTimeString;
        timeDialog = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar1.set(Calendar.HOUR,hourOfDay);
                calendar1.set(Calendar.MINUTE,minute);
                time = hourOfDay+":"+minute;
                textTime.setText("時間："+hourOfDay+"時"+minute+"分");
            }
        };
    }
    public void datePicker(View v){
        //建立date的dialog
        DatePickerDialog dialog = new DatePickerDialog(v.getContext(),
                pickerDialog,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
    public void timePicker(View v){
        TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                timeDialog,
                calendar1.get(Calendar.HOUR),
                calendar1.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }
    public void TimePick(){
        Time_Spinner = findViewById(R.id.Time);
        Time_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Time_Select = Time_Spinner.getSelectedItem().toString();
                Log.i("Spinner Time Select",Time_Select);
                String[] parts = Time_Select.split("-");
                time = parts[0];
                Log.i("Spinner time",time);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

}
