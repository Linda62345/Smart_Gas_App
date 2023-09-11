package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class OrderListUnfinished extends AppCompatActivity {
    Calendar calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener pickerDialog;
    Calendar calendar2 = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener pickerDialog2;
    private Button unfinished;
    private ListView orderList;
    public String Customer_Id, start_date, end_date;
    InputStream is = null;
    String[] data,order_Id;
    Date startDate, endDate;
    TextView startDateChangeable, endDateChangeable;
    public static String static_order_id;
    EditText startYearEditText, startMonthEditText, startDateEditText, endYearEditText, endMonthEditText, endDateEditText;
    private String URL = "http://54.199.33.241/test/customer_UnOrderList.php";
    ArrayList<OrderListFinishList> orderListFinishLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list_unfinished);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        unfinished = findViewById(R.id.order_finished);

        unfinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderListUnfinished.this, OrderListFinished.class);
                startActivity(intent);
            }
        });

        startDateChangeable = findViewById(R.id.startDateChangeable);
        endDateChangeable = findViewById(R.id.endDateChangeable);

        // 日期選擇
        Date();
        startDateChangeable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(v);
            }
        });
        // 日期選擇
        Date2();
        endDateChangeable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker2(v);
            }
        });

        Button enterButton = findViewById(R.id.enterSearch);
        enterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                try {
//                    if (startDate.after(endDate)) {
//                        Toast.makeText(OrderListFinished.this, "Start date cannot be bigger than end date", Toast.LENGTH_SHORT).show();
//                    } else {
//                        getOrderList();
//                    }
//                }  catch (Exception e) {
//                    Log.i("orderListFinishDate",e.toString());
//                }
                LoginActivity loginActivity = new LoginActivity();
                Customer_Id = String.valueOf(loginActivity.getCustomerID());
                Log.i("finish: ",Customer_Id);

                Log.i("End Date:", end_date);
                Log.i("Start Date:", start_date);

                orderList = (ListView)findViewById(R.id.list_item);
                StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));

                orderList.setAdapter(null);
                try {
                    getData();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                try {
//                    getOrderList();
//                } catch (Exception e) {
//                    Log.i("OrderList create Exception",e.toString());
//                }
                setAdapter();

            }
        });

        orderListFinishLists = new ArrayList<OrderListFinishList>();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_notifications);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(), UserDashboard.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(), Homepage.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(getApplicationContext(), OrderListUnfinished.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    private void setAdapter() {
        if(orderListFinishLists.size()>0){
            OrderListFinishAdapterList adapter=
                    new OrderListFinishAdapterList(getApplicationContext(),R.layout.adapter_list_un_finish,orderListFinishLists);
            orderList.setAdapter(adapter);
            orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //當備案下時
                    String msg = data[position];
                    Toast.makeText(OrderListUnfinished.this, msg, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderListUnfinished.this, SearchOrderResultFinished.class);
                    String Id = order_Id[position];
                    static_order_id = Id;
                    startActivity(intent);
                }
            });
            orderListFinishLists = new ArrayList<OrderListFinishList>();
        }
        else{
            Toast.makeText(this, "無訂單", Toast.LENGTH_SHORT).show();
        }

    }

    private void getOrderList() throws MalformedURLException {
        try{

            String Showurl = "http://54.199.33.241/test/customer_UnOrderList.php";
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data1 = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Customer_Id), "UTF-8");
            String post_data2 = URLEncoder.encode("start_date", "UTF-8") + "=" + URLEncoder.encode(start_date, "UTF-8")
                    + "&" + URLEncoder.encode("end_date", "UTF-8") + "=" + URLEncoder.encode(end_date, "UTF-8");
            Log.i("post_data1: ", post_data1);
            Log.i("post_data2: ", post_data2);

            bufferedWriter.write(post_data1);
            bufferedWriter.write("&");
            bufferedWriter.write(post_data2);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String result = "";
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            Log.i("get Order List result", "["+result+"]");
            try{
                JSONArray ja = new JSONArray(result);
                JSONObject jo = null;

                orderListFinishLists.clear();

                data = new String[ja.length()];
                order_Id = new String[ja.length()];

                for(int i = 0; i<ja.length();i++){
                    jo = ja.getJSONObject(i);
                    String orderTime = jo.getString("Order_Time");

                    OrderListFinishList order = new OrderListFinishList(orderTime, "已完成");
                    orderListFinishLists.add(order);

                    Log.i("order data", order.getTime());
                    order_Id[i] = jo.getString("ORDER_Id");

                    data[i] = "訂購時間: " + orderTime + " - " + "已完成";

                    Log.i("order data",data[i]);
                    order_Id[i] = jo.getString("ORDER_Id");
                }

            }catch(Exception e){
                Log.i("OrderList JSON Exception",e.toString());
            }
        } catch (Exception e) {
            Log.i("GetOrderList Exception", e.toString());
        }

        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);

    }


    private void getData() throws IOException {
        //還要把customer Id丟過去

        String Showurl = "http://54.199.33.241/test/customer_UnOrderList.php";
        URL url = new URL(Showurl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        String post_data1 = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Customer_Id), "UTF-8");
        String post_data2 = URLEncoder.encode("start_date", "UTF-8") + "=" + URLEncoder.encode(start_date, "UTF-8")
                + "&" + URLEncoder.encode("end_date", "UTF-8") + "=" + URLEncoder.encode(end_date, "UTF-8");
        Log.i("post_data1: ", post_data1);
        Log.i("post_data2: ", post_data2);

        bufferedWriter.write(post_data1);
        bufferedWriter.write("&");
        bufferedWriter.write(post_data2);
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStream.close();
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
        String result = "";
        String line = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        bufferedReader.close();
        inputStream.close();
        httpURLConnection.disconnect();
        Log.i("result here", "["+result+"]");
        try{
            Log.i("result try","["+result+"]");
            JSONArray ja = new JSONArray(result);
            JSONObject jo = null;

            orderListFinishLists.clear();

            data = new String[ja.length()];
            order_Id = new String[ja.length()];

            for(int i = 0; i<ja.length();i++){
                jo = ja.getJSONObject(i);
                String orderTime = jo.getString("Order_Time");

                OrderListFinishList order = new OrderListFinishList(orderTime, "已完成");
                orderListFinishLists.add(order);

                Log.i("order data", order.getTime());
                order_Id[i] = jo.getString("ORDER_Id");

                data[i] = "訂購時間: " + orderTime + " - " + "已完成";

                Log.i("order data",data[i]);
                order_Id[i] = jo.getString("ORDER_Id");
            }
        }catch(Exception e){
            Log.i("OrderList JSON Exception",e.toString());
        }
    }

    public void Date(){
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Taipei");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(timeZone);

        // Format the current date and time as a string in the correct format
        String currentDateTimeString = dateFormat.format(new Date());
        startDateChangeable.setText(currentDateTimeString);
        start_date = currentDateTimeString;
        pickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DATE,dayOfMonth);
                start_date = year+"-"+(month+1)+"-"+dayOfMonth;
                startDateChangeable.setText(year+"-"+(month+1)+"-"+dayOfMonth);
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

    public void Date2(){
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Taipei");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(timeZone);

        // Format the current date and time as a string in the correct format
        String currentDateTimeString = dateFormat.format(new Date());
        endDateChangeable.setText(currentDateTimeString);
        end_date = currentDateTimeString;
        pickerDialog2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar2.set(Calendar.YEAR,year);
                calendar2.set(Calendar.MONTH,month);
                calendar2.set(Calendar.DATE,dayOfMonth);
                end_date = year+"-"+(month+1)+"-"+dayOfMonth;
                endDateChangeable.setText(year+"-"+(month+1)+"-"+dayOfMonth);
            }
        };
    }

    public void datePicker2(View v){
        //建立date的dialog
        DatePickerDialog dialog = new DatePickerDialog(v.getContext(),
                pickerDialog2,
                calendar2.get(Calendar.YEAR),
                calendar2.get(Calendar.MONTH),
                calendar2.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
