package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class OrderListFinished extends AppCompatActivity {

    private Button unfinished, finished, order;
    private ListView orderList;
    public String Customer_Id, start_date, end_date;
    InputStream is = null;
    String line,result = "";
    String[] data,order_Id, deliveryPhones, orderIds, orderWeights, orderDetails;
    Date startDate, endDate;
    TextView startDateTextView, endDateTextView, space;
    public static String static_order_id;
    EditText startYearEditText, startMonthEditText, startDateEditText, endYearEditText, endMonthEditText, endDateEditText;
    private String URL = "http://10.0.2.2/SQL_Connect/customer_OrderList.php";
    ArrayList<OrderListFinishList> orderListFinishLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list_finished);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        unfinished = findViewById(R.id.order_unfinished);

        unfinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderListFinished.this, OrderListUnfinished.class);
                startActivity(intent);
            }
        });

        // Step 1
        startYearEditText = findViewById(R.id.editStartYear_unfinishedInput);
        startMonthEditText = findViewById(R.id.editStartMonth_unfinishedInput);
        startDateEditText = findViewById(R.id.editStartDay_unfinishedInput);
        endYearEditText = findViewById(R.id.editEndYear_unfinishedInput);
        endMonthEditText = findViewById(R.id.editEndMonth_unfinishedInput);
        endDateEditText = findViewById(R.id.editEndDay_unfinishedInput);

        Button enterButton = findViewById(R.id.enterSearch);
        enterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String startYearString = startYearEditText.getText().toString();
                String startMonthString = startMonthEditText.getText().toString();
                String startDateString = startDateEditText.getText().toString();
                String endYearString = endYearEditText.getText().toString();
                String endMonthString = endMonthEditText.getText().toString();
                String endDateString = endDateEditText.getText().toString();

                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                try {
                    startDate = dateFormat1.parse(startYearString + "-" + startMonthString + "-" + startDateString);
                    endDate = dateFormat1.parse(endYearString + "-" + endMonthString + "-" + endDateString);

                    if (startDate.after(endDate)) {
                        Toast.makeText(OrderListFinished.this, "Start date cannot be bigger than end date", Toast.LENGTH_SHORT).show();


                    } else {
                        getOrderList();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                LoginActivity loginActivity = new LoginActivity();
                Customer_Id = String.valueOf(loginActivity.getCustomerID());
                Log.i("finish: ",Customer_Id);


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                start_date = dateFormat.format(startDate);
                end_date = dateFormat.format(endDate);

                Log.i("End Date:", end_date);
                Log.i("Start Date:", start_date);

                startDateTextView = findViewById(R.id.startDate_unfinishedView);
                startDateTextView.setText(start_date);

                space = findViewById(R.id.untilDate_unfinishedView);
                space.setText("-");

                endDateTextView = findViewById(R.id.endDate_unfinishedView);
                endDateTextView.setText(end_date);


                orderList = (ListView)findViewById(R.id.list_item);
                StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));

                orderList.setAdapter(null);
                try {
                    getData();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    getOrderList();
                } catch (Exception e) {
                    Log.i("OrderList create Exception",e.toString());
                }
                setAdapter();

            }
        });

//        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //當備案下時
//                String msg = data[position];
//                Toast.makeText(OrderListFinished.this, msg, Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(OrderListFinished.this, SearchOrderResult.class);

//                String Id = order_Id[position];
//                static_order_id = Id;
//                intent.putExtra("orderId", Id);
//                startActivity(intent);
//
//            }
//        });
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

//        if(data!=null){
//            ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data);
//            orderList.setAdapter(adapter);
            orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //當備案下時
                    String msg = data[position];
                    Toast.makeText(OrderListFinished.this, msg, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderListFinished.this, SearchOrderResultFinished.class);
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

            String Showurl = "http://10.0.2.2/SQL_Connect/customer_OrderList.php";
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
            Log.i("result", "["+result+"]");
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

        String Showurl = "http://10.0.2.2/SQL_Connect/customer_OrderList.php";
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
        Log.i("result", "["+result+"]");
        try {
            String dataurl = "http://10.0.2.2/SQL_Connect/customer_OrderList.php";
            URL url1 = new URL(dataurl);
            HttpURLConnection con = (HttpURLConnection) url1.openConnection();
            con.setRequestMethod("GET");
            is = new BufferedInputStream(con.getInputStream());
        }
        catch(Exception e){
            Log.i("OrderList Exception",e.toString());
        }
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            while((line = br.readLine())!=null){
                sb.append(line+"\n");
            }
            is.close();
            result = sb.toString();
            Log.i("OrderList result",result);
        }catch(Exception e){
            Log.i("OrderList Exception2",e.toString());
        }
        try{
            JSONArray ja = new JSONArray(result);
            JSONObject jo = null;

            data = new String[ja.length()];

            for(int i = 0; i<ja.length();i++){
                jo = ja.getJSONObject(i);
                data[i] = jo.getString("Order_Id");
                Log.i("order data",data[i]);
            }
        }catch(Exception e){
            Log.i("OrderList JSON Exception",e.toString());
        }
    }


}