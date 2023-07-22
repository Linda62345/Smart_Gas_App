package com.example.smartgasapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class SearchOrderResult extends AppCompatActivity {
    private Button enter;
    private String order_Id;
    private TextView name,phone,address,type,gas_Quan,time;
    public static int gas_quantity;
    public String Customer_Id;
    String line,result = "";
    String[] quantity,weight,order_type;
    ArrayList<CustomerOrderDetail> orderdetail;
    public ListView Lorderdetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_order_result);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        enter = findViewById(R.id.confirmOrder);

        Bundle bundle;
        if ((bundle = getIntent().getExtras()) != null) {
            order_Id = bundle.getString("orderId");
        } else {
            OrderListFinished orderList = new OrderListFinished();
            order_Id = orderList.static_order_id;
        }

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //看有幾桶瓦斯 就換幾次
                Intent intent = new Intent(SearchOrderResult.this, Homepage.class);
                startActivity(intent);
            }
        });

        name = findViewById(R.id.changable_customer_name);
        phone = findViewById(R.id.changable_contactNumber);
        address = findViewById(R.id.changable_deliveryAddress);
        type = findViewById(R.id.changable_deliveryType);
        gas_Quan = findViewById(R.id.changable_gasQTY);
        time = findViewById(R.id.changable_time);

        try {
            showData();
        } catch (Exception e) {
            Log.i("Info Exception", e.toString());
        }

        Lorderdetail = (ListView) findViewById(R.id.list_item);

        try {
            Orderdetail();

        } catch (Exception e) {
            Log.i("Orderdetail Exception",e.toString());
        }
    }

        public void showData() throws MalformedURLException {
            try{
                String Showurl = "http://54.199.33.241/test/customer_searchOrderResult.php";
                URL url = new URL(Showurl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(order_Id), "UTF-8");
                Log.i("order id: " , post_data);
                bufferedWriter.write(post_data);
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
                JSONObject responseJSON = new JSONObject(result);
                String Order_name, Order_phone, Order_Address, Order_Type, Order_GasQuan, PredictTime;
                Order_name = responseJSON.getString("Order_Name");
                name.setText(Order_name);
                Order_phone = responseJSON.getString("Order_Phone");
                phone.setText(Order_phone);
                Order_Address = responseJSON.getString("Order_Address");
                address.setText(Order_Address);
                Order_Type = responseJSON.getString("Order_Type");
                if (Order_Type.equals("0")) {
                    Order_Type= "人員送達";
                } else if (Order_Type.equals("1")) {
                    Order_Type= "自取";
                } else {
                    // Handle other cases if necessary
                    Order_Type= "錯誤";
                }
                type.setText(Order_Type);
                Customer_Id = responseJSON.getString("Customer_Id");
                Log.i("Customer_Id",Customer_Id);
                Order_GasQuan = responseJSON.getString("Gas_Quantity");
                gas_Quan.setText(Order_GasQuan);
                gas_quantity = Integer.parseInt(Order_GasQuan);

                String OT = responseJSON.getString("Prediction_Time");
                time.setText(OT);
            } catch (Exception e) {
                Log.i("Order Info Exception", e.toString());
            }
        }

    public void Orderdetail() throws MalformedURLException {
        try{
            String URL = "http://54.199.33.241/test/Worker_OrderDetail.php";
            URL url = new URL(URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(order_Id), "UTF-8");
            bufferedWriter.write(post_data);
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
            Log.i("orderdetail", "["+result+"]");
            try{
                JSONArray ja = new JSONArray(result);
                JSONObject jo = null;
                quantity = new String[ja.length()];
                order_type = new String[ja.length()];
                weight = new String[ja.length()];
                orderdetail = new ArrayList<>();
                CustomerOrderDetail od1 = new CustomerOrderDetail("數量","規格","重量");
                orderdetail.add(od1);

                for(int i = 0; i<ja.length();i++){
                    jo = ja.getJSONObject(i);
                    quantity[i] = jo.getString("Order_Quantity");
                    order_type[i] = jo.getString("Order_type");
                    weight[i] = jo.getString("Order_weight");
                    CustomerOrderDetail od = new CustomerOrderDetail(quantity[i],order_type[i],weight[i]);
                    orderdetail.add(od);
                }
                CustomerOrderDetailAdapterList adapter = new CustomerOrderDetailAdapterList(this,R.layout.adapter_view_layout,orderdetail);
                if(orderdetail.size()>0){
                    Log.i("order detail", String.valueOf(orderdetail.size()));
                    Lorderdetail.setAdapter(adapter);
                }
            }catch(Exception e){
                Log.i("Orderdetail JSON Exception",e.toString());
            }
        }catch (Exception e){
            Log.i("OrderDetail",e.toString());
        }



    }
    }
