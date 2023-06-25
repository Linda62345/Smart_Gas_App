package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;

public class Homepage extends AppCompatActivity {

    private Button point;
    private Button homeLogin;
    private Button moreVol;

    private ImageButton buy;
    private ImageButton search;


    private ImageButton exchange;
    private ImageButton location;
    private ImageButton iot;
    private ImageButton personalBarcode;
    private BottomNavigationView bottomNavigationView;
    private TextView remainGas;
    private ProgressBar progressBar;
    private ViewPager viewPager;
    private ImageAdapter adapter;
    private ArrayList<Bitmap> images;
    public String result = "", Customer_ID;
    public JSONObject responseJSON;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Create sample images
        SliderView sliderView;
        int[] images = {R.drawable.logo,
                R.drawable.logo,
                R.drawable.logo};

        progressBar = findViewById(R.id.progressBar);
        point = findViewById(R.id.changable_pointButton);
        homeLogin = findViewById(R.id.loginFromHome);
        moreVol = findViewById(R.id.seeMoreButton);
        remainGas = findViewById(R.id.changableVol_progress);
        buy = findViewById(R.id.buyGasButton);
        search = findViewById(R.id.findOrderListButton);
        iot = findViewById(R.id.myIotButton);

        exchange = findViewById(R.id.exchangeGasButton);
        location = findViewById(R.id.companyButton);
        personalBarcode = findViewById(R.id.myIDButton);
        bottomNavigationView = findViewById(R.id.nav_view);



        sliderView = findViewById(R.id.Slider);

        SliderAdapter sliderAdapter = new SliderAdapter(images);

        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();


        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());

        NetworkTask networkTask1 = new NetworkTask();
        networkTask1.execute();

        point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, PointPage.class);
                startActivity(intent);
            }
        });

        homeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        moreVol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, UsageHistory.class);
                startActivity(intent);
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i("customer id", Customer_ID);
                            showData("http://10.0.2.2:80/SQL_Connect/Customer_Order_Detail_2.php", Customer_ID);
                            responseJSON = new JSONObject(result);
                            if (responseJSON.getString("response").equals("success")) {
                                Intent intent = new Intent(Homepage.this, OrderDetail.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Homepage.this, OrderGas.class);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            Log.i("訂單有無", e.toString());
                        }
                    }
                });

                thread.start();
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, OrderListUnfinished.class);
                startActivity(intent);
            }
        });


        exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, GasExchange.class);
                startActivity(intent);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, GasCompanyLocation.class);
                startActivity(intent);
            }
        });

        personalBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, PersonalBarcode.class);
                startActivity(intent);
            }
        });

        iot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, userIot.class);
                startActivity(intent);
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(), UserDashboard.class));
                        overridePendingTransition(0, 0);
                        NetworkTask networkTask = new NetworkTask();
                        networkTask.execute();
                        return true;
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(), Homepage.class));
                        overridePendingTransition(0, 0);
                        NetworkTask networkTask1 = new NetworkTask();
                        networkTask1.execute();
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

    private class NetworkTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String Showurl = "http://10.0.2.2/SQL_Connect/Iot_Connect.php";
                URL url = new URL(Showurl);

                HttpURLConnection httpURLConnection3 = (HttpURLConnection) url.openConnection();
                httpURLConnection3.setRequestMethod("POST");
                httpURLConnection3.setDoOutput(true);
                httpURLConnection3.setDoInput(true);
                OutputStream outputStream3 = httpURLConnection3.getOutputStream();
                BufferedWriter bufferedWriter3 = new BufferedWriter(new OutputStreamWriter(outputStream3, "UTF-8"));

                String post_data3 = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_ID, "UTF-8");
                Log.i("customerID: ", post_data3);

                bufferedWriter3.write(post_data3);
                bufferedWriter3.flush();
                bufferedWriter3.close();
                outputStream3.close();

                int statusCode3 = httpURLConnection3.getResponseCode();
                int statusCode = 0;
                if (statusCode3 == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream3 = httpURLConnection3.getInputStream();
                    BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(inputStream3, "iso-8859-1"));
                    String line3 = "";
                    StringBuilder result3 = new StringBuilder();
                    while ((line3 = bufferedReader3.readLine()) != null) {
                        result3.append(line3);
                    }
                    bufferedReader3.close();
                    inputStream3.close();
                    httpURLConnection3.disconnect();
                    Log.i("result3", "[" + result3 + "]");

                    return result3.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
               int progressValue = 0;
               double sensorWeight = 0.0;
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    progressValue = jsonObject.getInt("Result");
                    sensorWeight = jsonObject.getDouble("SENSOR_Weight");

                    Log.i("progressBar: ", String.valueOf(progressValue));
                    Log.i("sensorWeight: ", String.valueOf(sensorWeight));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setProgress(progressValue);
                remainGas.setText(String.valueOf(sensorWeight));


                TextView progressText = findViewById(R.id.progress_text);
                progressText.setText(String.valueOf(progressValue + "%"));
            }
        }
    }

    public void showData(String Showurl, String id) {
        try {
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
            Log.i("result", "[" + result + "]");
        } catch (Exception e) {
            Log.i("Order detail", e.toString());
        }
    }
}
