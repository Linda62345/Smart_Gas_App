package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.HttpHeaders;
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
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Homepage extends AppCompatActivity {
    private NotificationFrequency notificationFrequencyInstance;
    private static final long SESSION_TIMEOUT_MILLISECONDS = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
    private static final String SESSION_TIMEOUT_KEY = "session_timeout";

    private Button point;
    private Button homeLogin;
    private Button moreVol;

    private ImageButton buy;
    private ImageButton search;
    private ImageButton location;
    private ImageButton iot;
    private ImageButton personalBarcode;
    private BottomNavigationView bottomNavigationView;
    private TextView remainGas, showName;
    private ProgressBar progressBar;
    private ViewPager viewPager;
    private ImageAdapter adapter;
    private ArrayList<Bitmap> images;
    public String result = "", Customer_ID;
    public JSONObject responseJSON;
    private Spinner iot1;
    private String selectedSensorId;
    private TokenManager tokenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

//        notificationFrequencyInstance = new NotificationFrequency();
//        try {
//            notificationFrequencyInstance.frequency();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        tokenManager = TokenManager.getInstance(this);

        // Create sample images
        SliderView sliderView;
        int[] images = {R.drawable.logo,
                R.drawable.logo,
                R.drawable.logo};

        progressBar = findViewById(R.id.progressBar);
        iot1 = findViewById(R.id.iotSpinner);
        //point = findViewById(R.id.changable_pointButton);
        //homeLogin = findViewById(R.id.loginFromHome);
        // remainGas = findViewById(R.id.changableVol_progress);
        buy = findViewById(R.id.buyGasButton);
        search = findViewById(R.id.findOrderListButton);
        iot = findViewById(R.id.iotButton);

        //location = findViewById(R.id.companyButton);
        personalBarcode = findViewById(R.id.myIDButton);
        bottomNavigationView = findViewById(R.id.nav_view);
        showName = findViewById(R.id.show_name);

        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // Check if the session has timed out
        if (isSessionTimedOut() || !isLoggedIn) {
            // Handle session timeout or user not logged in, redirect to login screen
            redirectToLogin(savedEmail,savedPassword);
            return;
        }

        // Continue with normal data retrieval and processing
        showName.setText("您好，" + username);

        if (!username.isEmpty() && isLoggedIn) {
            // User is logged in, update UI accordingly
            showName.setText("您好，" + username);
        } else {
            // User is not logged in, handle it accordingly (e.g., redirect to login screen)
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Close the current activity
        }

        LoginActivity loginActivity = new LoginActivity();
        showName.setText("您好，" + loginActivity.Customer_Name);

        sliderView = findViewById(R.id.Slider);

        SliderAdapter sliderAdapter = new SliderAdapter(images);

        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();


        LoginActivity loginActivity1 = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity1.getCustomerID());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Iot Id: ");
        iot1.setAdapter(adapter);

        NetworkTask networkTask1 = new NetworkTask();
        networkTask1.execute();

        iot1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSensor = iot1.getSelectedItem().toString();
                if (!selectedSensor.equals("Iot Id: ")) {
                    // Remove the "Iot Id: " part from the selected sensor ID
                    selectedSensorId = selectedSensor.substring("Iot Id: ".length());
                    NetworkTask networkTask = new NetworkTask();
                    networkTask.execute(selectedSensorId);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, PointPage.class);
                startActivity(intent);
            }
        });*/

//        homeLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Homepage.this, LoginActivity.class);
//                startActivity(intent);
//            }
//        });

//        moreVol.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Homepage.this, userIot.class);
//                startActivity(intent);
//            }
//        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i("customer id", Customer_ID);
                            showData("http://54.199.33.241/test/Customer_Order_Detail_2.php", Customer_ID);
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


        // 訂單查詢
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, OrderListUnfinished.class);
                startActivity(intent);
            }
        });


//        location.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Homepage.this, GasCompanyLocation.class);
//                startActivity(intent);
//            }
//        });

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

      //  makeAuthenticatedApiRequest();
    }

    private boolean isSessionTimedOut() {
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        long sessionStartTime = sharedPref.getLong(SESSION_TIMEOUT_KEY, 0); // 0 means no session start time found
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - sessionStartTime;
        return elapsedTime >= SESSION_TIMEOUT_MILLISECONDS;
    }

    private void redirectToLogin(String savedEmail, String savedPassword) {
        // Clear session data
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(SESSION_TIMEOUT_KEY);
//        editor.remove("email"); // Clear saved email
//        editor.remove("password"); // Clear saved password
        editor.apply();

//        // Save the current activity's class name
//        SharedPreferences activityPref = getSharedPreferences("activity_data", Context.MODE_PRIVATE);
//        SharedPreferences.Editor activityEditor = activityPref.edit();
//        activityEditor.putString("current_activity", getClass().getName());
//        activityEditor.apply();

        // Redirect to the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", savedEmail);
        intent.putExtra("password", savedPassword);
        startActivity(intent);
        finish(); // Close the current activity
    }

    private class NetworkTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String selectedSensorId = null;
                if (params.length > 0) {
                    selectedSensorId = params[0];
                }
                String Showurl = "http://54.199.33.241/test/Iot_Connect.php";
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

                try{
                    JSONArray jsonArray = new JSONArray(result);

                    // Check if selectedSensorId is null or empty
                    if (selectedSensorId == null || selectedSensorId.isEmpty()) {
                        // Get the first sensor ID from the JSON response
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        selectedSensorId = jsonObject.getString("sensorId");
                    }

                    // Find the JSON object with the selected sensor ID
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String sensorId = jsonObject.getString("sensorId");
                        if (sensorId.equals(selectedSensorId)) {

                            progressValue = jsonObject.getInt("SENSOR_Weight");
                            sensorWeight = jsonObject.getDouble("SENSOR_Weight");
                            break;
                        }
                    }
                    Log.i("progressBar: ", String.valueOf(progressValue));
                    Log.i("sensorWeight: ", String.valueOf(sensorWeight));

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                updateUI(progressValue, sensorWeight);

                try {
                    JSONArray jsonArray = new JSONArray(result);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) iot1.getAdapter();
                    adapter.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String sensorId = jsonObject.getString("sensorId");
                        adapter.add("Iot Id: " + sensorId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateUI(int progressValue, double sensorWeight) {
        // Format progressValue and sensorWeight to 2 decimal places
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        String formattedSensorWeight = decimalFormat.format(sensorWeight);

        progressBar.setProgress(progressValue);
        // remainGas.setText(formattedSensorWeight);

        TextView progressText = findViewById(R.id.progress_text);
//        progressText.setText(String.valueOf(decimalFormat.format(progressValue) + "%"));
        progressText.setText(String.valueOf(decimalFormat.format(sensorWeight) + "%"));
    }

    private void saveAccessToken(String accessToken) throws GeneralSecurityException, IOException {
        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "my_secure_prefs",
                masterKeyAlias,  // Your master key alias
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
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
