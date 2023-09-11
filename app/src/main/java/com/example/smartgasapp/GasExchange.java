package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
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

public class GasExchange extends AppCompatActivity {
    public Spinner Company, Spinner_Weight, Spinner_Type;
    ArrayList<String> companyList = new ArrayList<>();
    ArrayAdapter<String> companyAdapter;
    RequestQueue requestQueue;
    public static String Gas_Type, Gas_Weight, Gas_Volume;
    public String Customer_ID, Company_Id, Company_Name;
    public static int Gas_Quantity;
    public TextView volume, name, value;
    public EditText Get_Quantity;
    public Button next;
    int totalValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_exchange);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        //Company = findViewById(R.id.gasCompany_spinner);
        requestQueue = Volley.newRequestQueue(this);
        //瓦斯行選擇
        //Company();

        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());
        Company_Id = String.valueOf(loginActivity.COMPANY_Id);
        volume = findViewById(R.id.curentGasVolume);
        name = findViewById(R.id.gasCompanyTitle);
        //value = findViewById(R.id.exchangePriceTitle);

        Gas_Quantity = 0;

        //顯示殘氣資料
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    showGasRemain();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        //Spinner_Type = findViewById(R.id.exchange_option_dropdown);
//        Spinner_Weight = findViewById(R.id.exchange_weight_dropdown);
//        Get_Quantity = findViewById(R.id.editTextNumber);
//        next = findViewById(R.id.confirm_exchange_button);
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getValue();
//                if (totalValue > Integer.parseInt(Gas_Volume)) {
//                    Toast.makeText(getApplicationContext(), "殘氣不足", Toast.LENGTH_LONG).show();
//                } else {
//                    Intent intent = new Intent(GasExchange.this, OrderDetail.class);
//                    startActivity(intent);
//                }
//            }
//        });
//        Get_Quantity.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String quantityStr = Get_Quantity.getText().toString().trim();
//                totalValue = 0;
//                if (!quantityStr.isEmpty()) {
//                    Gas_Quantity = Integer.parseInt(quantityStr);
//                    Gas_Weight = Spinner_Weight.getSelectedItem().toString();
//                    Gas_Weight = Gas_Weight.substring(0, Gas_Weight.length() - 2);
//                    totalValue = Integer.parseInt(Gas_Weight) * Gas_Quantity;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            value.setText("兌換價值: " + String.valueOf(totalValue));
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // TODO Auto-generated method stub
//            }
//        });
//        Spinner_Weight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // 在這裡處理 Spinner 選擇的變化
//                Gas_Weight = Spinner_Weight.getSelectedItem().toString();
//                // 做你需要的處理
//                Gas_Weight = Gas_Weight.substring(0, Gas_Weight.length() - 2);
//                totalValue = Integer.parseInt(Gas_Weight) * Gas_Quantity;
//                value.setText("兌換價值: " + String.valueOf(totalValue));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // 未選擇任何項目時的處理
//            }
//        });


        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
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
            Company_Name = responseJSON.getString("Company_Name");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    volume.setText("目前累積殘氣: " + Gas_Volume + "公斤");
                    name.setText("瓦斯行: " + Company_Name);
                }
            });

        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (e.toString().contains("No value for Gas_Volume")) {
                        Toast.makeText(getApplicationContext(), "無殘氣", Toast.LENGTH_LONG).show();
                    }
                }
            });
            Log.i("GasExchange Exception", e.toString());
        }
    }

    public void getValue() {
        Gas_Weight = Spinner_Weight.getSelectedItem().toString();
        Gas_Weight = Gas_Weight.substring(0, Gas_Weight.length() - 2);
        Log.i("Exchange Weight", Gas_Weight);
        Gas_Quantity = Integer.parseInt(Get_Quantity.getText().toString().trim());
        Log.i("Exchange Quantity", Get_Quantity.getText().toString().trim());
    }
}