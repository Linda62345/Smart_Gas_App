package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class userIot extends AppCompatActivity {

    public String Customer_ID, Sensor_ID,GasWeightEmptyString,result;
    public EditText E_Sensor_ID,gasWeightEmpty,gasBottleSpec;
    public ListView IOTlistView;
    public Button B_addIOT;
    public ImageButton scanner;
    String[] data;
    public JSONObject responseJSON;
    public JSONArray ja;

    public static ArrayList<CustomerOrderDetail> customerOrderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_iot);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());

        customerOrderDetails = new ArrayList<CustomerOrderDetail>();
        CustomerOrderDetail od = new CustomerOrderDetail("編號","重量","電量");
        customerOrderDetails.add(od);

        E_Sensor_ID = findViewById(R.id.sensor_Id);

        IOTlistView = findViewById(R.id.IOTlist);

        scanner = findViewById(R.id.qrPage);
        gasBottleSpec = findViewById(R.id.GasBottleSpec);

        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(userIot.this, ScanIotQRCode.class);
                startActivity(intent);
            }
        });

        B_addIOT = findViewById(R.id.ButtonAddIOT);
        B_addIOT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sensorIDInput = E_Sensor_ID.getText().toString().trim();
                String gasWeightInput = gasWeightEmpty.getText().toString().trim();
                String gasBottleSpecInput = gasBottleSpec.getText().toString().trim();

                if (sensorIDInput.isEmpty()) {
                    Toast.makeText(userIot.this, "請輸入IOT號碼", Toast.LENGTH_SHORT).show();
                } else if (sensorIDInput.length() != 15) {
                    Toast.makeText(userIot.this, "請輸入正確IOT號碼", Toast.LENGTH_SHORT).show();
                } else if (gasWeightInput.isEmpty()) {
                    Toast.makeText(userIot.this, "請輸入瓦斯桶空桶重", Toast.LENGTH_SHORT).show();
                } else if (gasBottleSpecInput.isEmpty()) {
                    Toast.makeText(userIot.this, "請輸入瓦斯桶規格", Toast.LENGTH_SHORT).show();
                } else {
                    saveIOT();
                }
            }
        });

        ShowDataDetail();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

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

        gasWeightEmpty = findViewById(R.id.GasBottleWeightEmpty);

    }
    public void ShowDataDetail(){
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.i("iot here","iot here");
                        showIOT("http://54.199.33.241/test/Show_IOT.php");
                        //這裡要做修正
                        if (result.contains("\"\"response\":\"0\"")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(userIot.this, "無IoT連結", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        try{
                            ja = new JSONArray(result);
                            if(ja!=null){
                                JSONObject jo = null;
                                data = new String[ja.length()];
                                customerOrderDetails = new ArrayList<CustomerOrderDetail>();
                                CustomerOrderDetail od = new CustomerOrderDetail("編號","重量","電量");
                                customerOrderDetails.add(od);

                                for(int i = 0;i<ja.length();i++){
                                    jo = ja.getJSONObject(i);
                                    CustomerOrderDetail od1 = new CustomerOrderDetail(jo.getString("SENSOR_Id"),jo.getString("SENSOR_Weight"),jo.getString("SENSOR_Battery"));
                                    customerOrderDetails.add(od1);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CustomerOrderDetailAdapterList adapter = new CustomerOrderDetailAdapterList (getApplicationContext(), R.layout.adapter_view_layout, customerOrderDetails);
                                        Log.i("order detail", String.valueOf(customerOrderDetails.size()));
                                        IOTlistView.setAdapter(null);
                                        IOTlistView.setAdapter(adapter);
                                    }
                                });
                            }
                        }
                        catch (Exception e){
                            Log.i("ListView iot exception",e.toString());
                        }
                    }
                }
        );
        thread.start();
    }
    public void saveIOT(){
        try {
            String URL = "http://54.199.33.241/test/Save_IOT.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("iot response", response);
                    if (response.contains("success")) {
                        Toast.makeText(getApplicationContext(), "IOT新增成功", Toast.LENGTH_LONG).show();
                        E_Sensor_ID.setText("");
                        //畫面更新
                        ShowDataDetail();
                    } else if (response.contains("Duplicate")) {
                        Toast.makeText(getApplicationContext(), "新增失敗，此IOT已在資料庫中", Toast.LENGTH_LONG).show();

                    } else if (response.contains("failure")) {
                        Toast.makeText(getApplicationContext(), "新增失敗", Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("gasEmptyWeight",gasWeightEmpty.getText().toString().trim());
                    data.put("id", Customer_ID);
                    data.put("sensor_id", E_Sensor_ID.getText().toString().trim());
                    data.put("gasSpecWeight",gasBottleSpec.getText().toString().trim());
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            Log.i("save iot Exception", e.toString());
        }
    }
    public void showIOT(String Showurl){
        try{
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_ID, "UTF-8");
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
            Log.i("iot result", "["+result+"]");
        }
        catch (Exception e){
            Log.i("show iot Exception",e.toString());
        }
    }
}