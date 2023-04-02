package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class OrderDetail extends AppCompatActivity {

    private Button exchange;
    private Button editReceipt;
    private Button finish;
    public String Customer_ID,Order_Id,result="",Company_Id,phone,address,time,method;
    public TextView Greeting, Recepit_Name,Receipt_TelNo,Receipt_Addr,Expect_Time,Delivery_Method;
    public JSONObject responseJSON;
    public JSONArray ja;
    public ListView listView;
    public int Gas_Quantity;
    ArrayList<CustomerOrderDetail> customerOrderDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());

        Expect_Time = findViewById(R.id.ExpectTime);
        Delivery_Method = findViewById(R.id.deliveryMethod);
        Greeting = findViewById(R.id.client_greetingTitle);
        Recepit_Name = findViewById(R.id.changable_receiptName);
        Receipt_TelNo = findViewById(R.id.changable_receiptTelNo);
        Receipt_Addr = findViewById(R.id.changable_receiptAddr);

        exchange = findViewById(R.id.receipt_exchange_status);
        editReceipt = findViewById(R.id.receipt_edit_button);
        finish = findViewById(R.id.receipt_next_button);

        customerOrderDetails = new ArrayList<CustomerOrderDetail>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    showData("http://10.0.2.2:80/SQL_Connect/Customer_Order_Detail.php",Customer_ID);
                    responseJSON = new JSONObject(result);
                    String Customer_Name = responseJSON.getString("CUSTOMER_Name");
                    Greeting.setText(Customer_Name);
                    Recepit_Name.setText(Customer_Name);
                    phone = responseJSON.getString("CUSTOMER_PhoneNo");
                    Receipt_TelNo.setText(phone);
                    address = responseJSON.getString("CUSTOMER_Address");
                    Receipt_Addr.setText(address);
                    Company_Id = responseJSON.getString("COMPANY_Id");

                    showData("http://10.0.2.2:80/SQL_Connect/Customer_Order_Detail_2.php",Customer_ID);
                    responseJSON = new JSONObject(result);
                    if(responseJSON.getString("response").equals("success")){
                        Log.i("check order response",responseJSON.getString("response"));
                        Order_Id = responseJSON.getString("ORDER_Id");
                        method = responseJSON.getString("Delivery_Method");
                        if(method.equals("0")){
                            Delivery_Method.setText("配送方式: 師傅送達");
                        }
                        if(method.equals("1")){
                            Delivery_Method.setText("配送方式: 自取");
                        }
                        time = responseJSON.getString("Expect_time");
                        Expect_Time.setText("送達時間: "+time);
                        Gas_Quantity = responseJSON.getInt("Gas_Quantity");

                        // 1. 秀訂單明細 欄位有: 格式 數量 配送方式 時間
                        showData("http://10.0.2.2/SQL_Connect/Worker_OrderDetail.php",Order_Id);
                        try {
                            ja = new JSONArray(result);
                            JSONObject jo = null;
                            String[] quantity, type, weight;
                            quantity = new String[ja.length()];
                            type = new String[ja.length()];
                            weight = new String[ja.length()];
                            customerOrderDetails = new ArrayList<>();

                            for (int i = 0; i < ja.length(); i++) {
                                jo = ja.getJSONObject(i);
                                quantity[i] = jo.getString("Order_Quantity");
                                type[i] = jo.getString("Order_type");
                                weight[i] = jo.getString("Order_weight");
                                CustomerOrderDetail od = new CustomerOrderDetail(quantity[i], type[i], weight[i]);
                                customerOrderDetails.add(od);
                            }
                            CustomerOrderDetailAdapterList adapter = new CustomerOrderDetailAdapterList (getApplicationContext(), R.layout.adapter_view_layout, customerOrderDetails);
                            if (customerOrderDetails.size() > 0) {
                                Log.i("order detail", String.valueOf(customerOrderDetails.size()));
                                listView = (ListView) findViewById(R.id.listview);
                                listView.setAdapter(null);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Stuff that updates the UI
                                        listView.setAdapter(adapter);
                                    }
                                });
                            }

                        }
                        catch (Exception e){
                            Log.i("Order detail Adapter",e.toString());
                        }
                    }
                    //客戶從來沒有下過訂單
                    else{
                        Intent intent = new Intent(OrderDetail.this, OrderGas.class);
                        startActivity(intent);
                        Log.i("check order response",responseJSON.getString("response"));
                    }
                } catch (Exception e) {
                    Log.i("Order Detail Exception",e.toString());
                }
            }

        });

        thread.start();

        exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetail.this, GasExchange.class);
                startActivity(intent);
            }
        });
        editReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetail.this, OrderGas.class);
                startActivity(intent);
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //將新的訂單資訊輸入進去 customer_id, company_id, delivery_condition,delivery_address,delivery_phone,gas_quantity,order_time,expect_time,delivery_method
                try{
                    newOrder();
                }
                catch (Exception e){
                    Log.i("Create order Exception", e.toString());
                }
            }
        });
    }

    public void showData(String Showurl,String id){
        try{
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String line = "";
            result = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            Log.i("result", "["+result+"]");
        }
        catch (Exception e){
            Log.i("Order detail",e.toString());
        }
    }
    public void newOrder(){
        try{
            String String_url = "http://10.0.2.2/SQL_Connect/NewOrder.php";
            Log.i("Execute order","Please execute"+Customer_ID+Company_Id+ String.valueOf(0)+address+phone+String.valueOf(Gas_Quantity)+time+method);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, String_url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equals("success")) {
                        Log.i("Create Order method",response);
                        Intent intent = new Intent(OrderDetail.this, OrderListUnfinished.class);
                        startActivity(intent);
                        finish.setClickable(false);
                    } else if (response.equals("failure")) {
                        Log.i("Create Order failure",response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("Create Order failure",error.toString());
                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    //customer_id, company_id, delivery_condition,delivery_address,delivery_phone,gas_quantity,order_time,expect_time,delivery_method
                    data.put("Customer_ID", Customer_ID);
                    data.put("Company_Id", Company_Id);
                    data.put("delivery_condition", String.valueOf(0));
                    data.put("delivery_address", address);
                    data.put("delivery_phone", phone);
                    data.put("Gas_Quantity", String.valueOf(Gas_Quantity));
                    // Create a DateFormat object and set the timezone to Taiwan
                    TimeZone timeZone = TimeZone.getTimeZone("Asia/Taipei");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateFormat.setTimeZone(timeZone);

                    // Format the current date and time as a string in the correct format
                    String currentDateTimeString = dateFormat.format(new Date());

                    // Log the string to the console
                    Log.i("time",currentDateTimeString);
                    data.put("order_time",currentDateTimeString);
                    data.put("expect_time",time);
                    data.put("delivery_method",method);

                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);}
        catch (Exception e){
            Log.i("Method of creating order",e.toString());
        }
    }


}