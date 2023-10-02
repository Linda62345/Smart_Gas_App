package com.example.smartgasapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

    private Button exchange,editReceipt,finish;
    public String Customer_ID,Order_Id,result="",Company_Id,phone,address,date,time,method,New_Order_Id;
    public TextView Greeting, Recepit_Name,Receipt_TelNo,Receipt_Addr,Expect_Time,Delivery_Method,Expect_Date;
    public JSONObject responseJSON;
    public JSONArray ja;
    public ListView listView;
    public Spinner Time_Spinner;
    public int Gas_Quantity,orderDetailQuan,Gas_Delete=0,position=-1;
    public static boolean edit=false;
    public static ArrayList<OrderDetailItem> customerOrderDetails;
    public static String Gas_Volume;
    DatePickerDialog.OnDateSetListener pickerDialog;
    Calendar calendar = Calendar.getInstance();
    TimePickerDialog.OnTimeSetListener timeDialog;
    Calendar calendar1 = Calendar.getInstance();
    GasExchange gasExchange;
    CompositeGasMenu compositeGasMenu;
    OrderDetailListAdapter orderDetailListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //ui定義
        findUI();

        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());
        Company_Id = String.valueOf(loginActivity.COMPANY_Id);
        Time_Spinner = findViewById(R.id.Time);

        DeliveryMethod deliveryMethod = new DeliveryMethod();
        compositeGasMenu = new CompositeGasMenu();
        cylinder_gas_menu cylinder_gas_menu = new cylinder_gas_menu();
        Gas_Delete=0;

        customerOrderDetails = new ArrayList<OrderDetailItem>();
        customerOrderDetails.clear();
        //殘氣兌換與訂單詳細資料作結合
        Log.i("gasExchange.Gas_Quantity", String.valueOf(gasExchange.Gas_Quantity));
        gasExchange = new GasExchange();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    showGasRemain();
                    showData("http://54.199.33.241/test/Customer_Order_Detail.php",Customer_ID);
                    responseJSON = new JSONObject(result);
                    String Customer_Name = responseJSON.getString("CUSTOMER_Name");
                    phone = responseJSON.getString("CUSTOMER_PhoneNo");
                    address = responseJSON.getString("CUSTOMER_Address");
                    Company_Id = responseJSON.getString("COMPANY_Id");

                    showData("http://54.199.33.241/test/Customer_Order_Detail_2.php",Customer_ID);
                    responseJSON = new JSONObject(result);
                    //無上一筆訂單資料 或是 已編輯訂單
                    if(responseJSON.getString("response").equals("failure")||edit==true){
                        //放編輯訂單裡面的資料
                        //顯示配送方式 時間
                        method="";
                        time = "";
                        date = "";
                        Gas_Quantity=0;

                        method = String.valueOf(deliveryMethod.delivery_method);
                        Log.i("配送方式",method);
                        date = deliveryMethod.date;
                        time = deliveryMethod.time;
                        //選取時間要一致
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) Time_Spinner.getAdapter();
                        position = adapter.getPosition(deliveryMethod.Time_Select);

                        Gas_Quantity = compositeGasMenu.a+compositeGasMenu.b+compositeGasMenu.c+cylinder_gas_menu.a+cylinder_gas_menu.b+cylinder_gas_menu.c;
                        if(gasExchange.Gas_Quantity!=0){
                            Gas_Quantity += gasExchange.Gas_Quantity;
                        }
                        //顯示訂單詳細資料
                        setGas_Quantity();


                    }
                    //有上一筆訂單資料
                    else{
                        String Total_time = "";
                        date = "";
                        time = "";
                        Order_Id = responseJSON.getString("ORDER_Id");
                        method = responseJSON.getString("Delivery_Method");
                        //Total_time = responseJSON.getString("Expect_time");
                        //Log.i("Original Total time",Total_time);
                        //date = Total_time.substring(0,10);
                        //Log.i("Original date",date);

                        TimeZone timeZone = TimeZone.getTimeZone("Asia/Taipei");
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        dateFormat.setTimeZone(timeZone);

                        // Get the current date and time
                        Date currentDate = new Date();

                        // Create a Calendar instance and set it to the current date
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(currentDate);

                        // Add two days to the current date
                        calendar.add(Calendar.DAY_OF_MONTH, 2);

                        // Get the updated date after adding two days
                        Date twoDaysLater = calendar.getTime();

                        // Format the updated date as a string in the correct format
                        date = dateFormat.format(twoDaysLater);
                        Log.i("date two",date);

                        //time = Total_time.substring(11,19);
                        //Log.i("Original time",time);
                        Gas_Quantity = responseJSON.getInt("Gas_Quantity");

                        // 1. 秀訂單明細 欄位有: 格式 數量 配送方式 時間
                        showData("http://54.199.33.241/test/Worker_OrderDetail.php",Order_Id);
                        try {
                            ja = new JSONArray(result);
                            JSONObject jo = null;
                            String[] quantity, type, weight;
                            quantity = new String[ja.length()];
                            type = new String[ja.length()];
                            weight = new String[ja.length()];


                            for (int i = 0; i < ja.length(); i++) {
                                jo = ja.getJSONObject(i);
                                quantity[i] = jo.getString("Order_Quantity");
                                type[i] = jo.getString("Order_type");
                                weight[i] = jo.getString("Order_weight");
                                // 一開始都是設置不兌換
                                OrderDetailItem od = new OrderDetailItem(quantity[i], type[i], weight[i],false);
                                customerOrderDetails.add(od);
                            }
                        }
                        catch (Exception e){
                            Log.i("Order detail Adapter",e.toString());
                        }


                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Greeting.setText(Customer_Name);
                            Recepit_Name.setText(Customer_Name);
                            Receipt_TelNo.setText(phone);
                            Receipt_Addr.setText(address);

                            //將資料顯示在UI上
                            if(method.equals("0")){
                                Delivery_Method.setText("人員送達");
                            }
                            if(method.equals("1")){
                                Delivery_Method.setText("到店自取");
                            }
                            if(method.equals("3")){
                                Delivery_Method.setText("錯誤");
                            }
                            Expect_Date.setText(date);
                            //Expect_Time.setText("送達時間: "+time);
                            Log.i("AL size", String.valueOf(customerOrderDetails.size()));

                            //時間可選擇
                            Date();
                            Expect_Date.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    datePicker(v);
                                }
                            });

                            TimePick();

                            if(position!=-1){
                                Log.i("position", String.valueOf(position));
                                Time_Spinner.setSelection(position);
                            }

                            //這裡要設定date 跟 time的顯示要正確
                    /*Time();
                    Expect_Time.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            timePicker(v);
                        }
                    });*/

                            //殘氣結合訂單
                            //RemainGas();

                            //show訂單詳細資料
                            OrderDetailListAdapter adapter = new OrderDetailListAdapter (getApplicationContext(), R.layout.adapter_order_detail_list, customerOrderDetails);
                            if (customerOrderDetails.size() > 0) {
                                Log.i("order detail", String.valueOf(customerOrderDetails.size()));
                                listView = (ListView) findViewById(R.id.listview);
                                listView.setAdapter(null);
                                // Stuff that updates the UI
                                listView.setAdapter(adapter);
                            }
                        }
                    });

                } catch (Exception e) {
                    Log.i("Order Detail Exception",e.toString());
                }
            }
        });

        thread.start();

        Log.i("ArrayList size", String.valueOf(customerOrderDetails.size()));

        editReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //若有編輯過 數據會更新 會跑到判斷else裡面去
                edit = true;
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

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

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
            String String_url = "http://54.199.33.241/test/NewOrder.php";
            Log.i("Execute order","Please execute"+Customer_ID+Company_Id+ String.valueOf(0)+address+phone+String.valueOf(Gas_Quantity)+time+method);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, String_url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("success")) {
                        Log.i("Create Order method",response);
                        New_Order_Id = response.replace("success", "");
                        Log.i("Order Id",New_Order_Id);

                        //這裡放insert gas order detail
                        NewOrderDetail();

                        Intent intent = new Intent(OrderDetail.this, OrderSuccess.class);
                        startActivity(intent);
                        finish.setClickable(false);
                    } else if (response.contains("failure")) {
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
                    data.put("expect_time",date+" "+time);
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
    public void NewOrderDetail(){
        //放在下一步的Button
        //order id: New_Order_Id
        //拿arrayList裡面的資料 customerOrderDetails
        //要那些欄位 order_id, order_quantity, order_type, order_weight
        for(int i=0;i<customerOrderDetails.size();i++){
            int index = i;
            String String_url = "http://54.199.33.241/test/NewOrderDetail.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, String_url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("success")) {
                        Log.i("Order Detail method",response);
                    } else if (response.contains("failure")) {
                        Log.i("Order Detail failure",response);
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
                    data.put("Order_ID", New_Order_Id);
                    data.put("Quantity", customerOrderDetails.get(index).getQuantity());
                    Log.i("Quantity", customerOrderDetails.get(index).getQuantity());
                    data.put("Type", customerOrderDetails.get(index).getType());
                    Log.i("Type", customerOrderDetails.get(index).getType());
                    data.put("Weight", customerOrderDetails.get(index).getWeight());
                    Log.i("Weight", customerOrderDetails.get(index).getWeight());
                    if(customerOrderDetails.get(index).getExchange()){
                        data.put("Exchange","1");
                        Log.i("1","1");
                        Gas_Delete += Integer.parseInt(customerOrderDetails.get(index).getQuantity())*Integer.parseInt(customerOrderDetails.get(index).getWeight());
                        Log.i("Gas_delete123",String.valueOf(Gas_Delete));
                        //殘氣扣除
                        if(Gas_Delete>0){
                            deleteGasRemain();
                        }
                    }
                    else{
                        data.put("Exchange","0");
                        Log.i("0","0");
                    }
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }
    }
    public void Date(){
        pickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DATE,dayOfMonth);
                date = year+"-"+month+"-"+dayOfMonth+" ";
                Expect_Date.setText(year+"/"+(month+1)+"/"+dayOfMonth);
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
    public void TimePick(){
        Time_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String Time_Select = Time_Spinner.getSelectedItem().toString();
                String[] parts = Time_Select.split("-");
                Time_Select = parts[0];
                time = Time_Select;
                Log.i("Spinner time",time);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                String Time_Select = Time_Spinner.getSelectedItem().toString();
                String[] parts = Time_Select.split("-");
                Time_Select = parts[0];
                time = Time_Select;
                Log.i("Spinner time",time);
            }
        });
    }

    public CustomerOrderDetail GasExchangeOrder(){
        CustomerOrderDetail od = null;
        if(gasExchange.Gas_Quantity!=0){
            od = new CustomerOrderDetail(String.valueOf(gasExchange.Gas_Quantity),gasExchange.Gas_Type, gasExchange.Gas_Weight);
            od.setExchange("1");
        }
        return od;
    }

    public void setGas_Quantity(){
        if(compositeGasMenu.a>0){
            OrderDetailItem od = new OrderDetailItem(String.valueOf(compositeGasMenu.a), "composite", compositeGasMenu.weight1,false);
            customerOrderDetails.add(od);
        }
        if(compositeGasMenu.b>0){
            OrderDetailItem od = new OrderDetailItem(String.valueOf(compositeGasMenu.b), "composite", compositeGasMenu.weight2,false);
            customerOrderDetails.add(od);
        }
        if(compositeGasMenu.c>0){
            OrderDetailItem od = new OrderDetailItem(String.valueOf(compositeGasMenu.c), "composite", compositeGasMenu.weight3,false);
            customerOrderDetails.add(od);
        }
        if(cylinder_gas_menu.a>0){
            OrderDetailItem od = new OrderDetailItem(String.valueOf(cylinder_gas_menu.a), "tradition", cylinder_gas_menu.weight1,false);
            customerOrderDetails.add(od);
        }
        if(cylinder_gas_menu.b>0){
            OrderDetailItem od = new OrderDetailItem(String.valueOf(cylinder_gas_menu.b), "tradition", cylinder_gas_menu.weight2,false);
            customerOrderDetails.add(od);
        }
        if(cylinder_gas_menu.c>0){
            OrderDetailItem od = new OrderDetailItem(String.valueOf(cylinder_gas_menu.c), "tradition", cylinder_gas_menu.weight3,false);
            customerOrderDetails.add(od);
        }
    }

    public void deleteGasRemain(){
        //三個值 Customer Id Company Id Gas Volume
        String String_url = "http://54.199.33.241/test/Delete_Gas_Remain.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, String_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains("success")) {
                    Log.i("Delete Gas Remain",response);
                } else if (response.contains("failure")) {
                    Log.i("Delete Gas Remain failure",response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Delete Gas Remain failure",error.toString());
                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("Customer_ID", Customer_ID);
                data.put("Company_Id", Company_Id);
                //int weight = Integer.parseInt(gasExchange.Gas_Volume);
                data.put("Gas_Delete", String.valueOf(Gas_Delete));
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    public void findUI(){
        Expect_Date = findViewById(R.id.ExpectDate);
        //Expect_Time = findViewById(R.id.ExpectTime);
        Delivery_Method = findViewById(R.id.deliveryMethod);
        Greeting = findViewById(R.id.client_greetingTitle);
        Recepit_Name = findViewById(R.id.changable_receiptName);
        Receipt_TelNo = findViewById(R.id.changable_receiptTelNo);
        Receipt_Addr = findViewById(R.id.changable_receiptAddr);

        editReceipt = findViewById(R.id.receipt_edit_button);
        finish = findViewById(R.id.receipt_next_button);
    }

    public void showGasRemain() {
        try {
            String Showurl = "http://54.199.33.241/test/Gas_Remain.php";
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("Customer_ID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Customer_ID), "UTF-8")
                    + "&" +
                    URLEncoder.encode("Company_Id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Company_Id), "UTF-8");

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
            Log.i("Gas Exchange result", "[" + result + "]");
            JSONObject responseJSON = new JSONObject(result);
            Gas_Volume = responseJSON.getString("Gas_Volume");
            Log.i("Gas_Volume",Gas_Volume);
        } catch (Exception e) {
            Log.i("GasExchange Exception", e.toString());
        }
    }

}