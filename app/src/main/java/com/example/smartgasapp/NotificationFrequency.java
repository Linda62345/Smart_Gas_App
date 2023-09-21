package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class NotificationFrequency extends AppCompatActivity  {

    private RadioGroup radioGroup;
    private static RadioButton radioButton;
    private static RadioButton two;
    private static RadioButton three;
    public String Customer_Id,Family_Id, result;
    public static int gasVolumeLeft;
    private Spinner spinner;
    private String selectedFrequency;
    private Handler handler;
    private Runnable notificationRunnable;
    public static ArrayList<Integer> family_Id;

    public JSONArray ja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_frequency);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            frequency();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Start the Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, new Intent(this, NotificationForegroundService.class));
        } else {
            startService(new Intent(this, NotificationForegroundService.class));
        }

//        radioGroup = findViewById(R.id.radioGroup);
//        two = findViewById(R.id.weightRadioTwo);
//        three = findViewById(R.id.weightRadioThree);
//        spinner = findViewById(R.id.frequency_spinner);

        Button enterButton = findViewById(R.id.enter);
        enterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                try {
//                   // frequency();
//                    // Create an explicit intent to navigate to the homepage layout
//                    Intent intent = new Intent(NotificationFrequency.this, Homepage.class);
//                    startActivity(intent);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
                Intent intent = new Intent(NotificationFrequency.this, Homepage.class);
                startActivity(intent);
            }
        });
        family_Id = new ArrayList<Integer>();
        startNotificationCheck();
        createNotificationChannel();

//        try {
//            double gasVolume = frequency(); // Retrieve the gasVolume value from the frequency() method
//            //startNotificationCheck(gasVolume); // Pass the gasVolume value to startNotificationCheck()
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        createNotificationChannel();


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
        startNotificationCheck();
    }

    private void startNotificationCheck() {
        handler = new Handler();
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    frequency();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Schedule the next notification check after a specific interval (e.g., every 1 minute)
                handler.postDelayed(this, 60000);
            }
        };

        // Start the initial notification check
        handler.post(notificationRunnable);
    }

//    private void startNotificationCheck(double gasVolume) {
//        handler = new Handler();
//        notificationRunnable = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // frequency();
//                    double gasVolume = frequency(); // Retrieve the gasVolume value from the frequency() method
//                    startNotificationCheck(gasVolume);
//
//                    Data inputData = new Data.Builder()
//                            .putDouble("gasVolume", gasVolume) // Pass the gasVolume value to the Worker
//                            .build();
////                    PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(MyWorker.class, 1, TimeUnit.MINUTES)
////                            .setInputData(inputData)
////                            .build();
//
//                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
//                            .setInputData(new Data.Builder().putDouble("gasVolume", gasVolume).build())
//                            .build();
//
//                    WorkManager.getInstance(NotificationFrequency.this).enqueue(workRequest);
//                    handler.postDelayed(this, 60000);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
////                // Schedule the next notification check after a specific interval (e.g., every 1 minute)
////                handler.postDelayed(this, 60000);
//            }
//        };
//
//        // Start the initial notification check
//        handler.post(notificationRunnable);
//    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    public double frequency() throws IOException {
        try {
            Customer_Id = String.valueOf(LoginActivity.getCustomerID());
            Log.i("finish: ", Customer_Id);
            new SendRequest().execute(Customer_Id);

//            double gasVolume = new SendRequest().execute(Customer_Id).get();
//            Log.i("gasVolume: ", String.valueOf(gasVolume));
//            // startNotificationCheck(gasVolume);
//            if (gasVolume < 3) {
//                startNotificationCheck(gasVolume);
//            }
//            return gasVolume;
        } catch (Exception e) {
            Log.i("Frequency JSON Exception", e.toString());
        }
        return 0;
    }

    private class SendRequest extends AsyncTask<String, Void, Double> {
        protected Double doInBackground(String... params) {

            String response = "";
            double gasVolume = 0.0;
            double fam_id = 0.0;
            try {
                String Customer_Id = params[0];
                String Showurl = "http://54.199.33.241/test/FrequencyNotification.php";
                String ShowUrl1 = "http://54.199.33.241/test/family_member.php";
                String ShowUrl2 = "http://54.199.33.241/test/find_family_id.php";
                URL url = new URL(Showurl);
                URL url1 = new URL(ShowUrl1);
                URL url2 = new URL(ShowUrl2);
                HttpURLConnection httpURLConnection3 = (HttpURLConnection) url2.openConnection();
                httpURLConnection3.setRequestMethod("POST");
                httpURLConnection3.setDoOutput(true);
                httpURLConnection3.setDoInput(true);
                OutputStream outputStream3 = httpURLConnection3.getOutputStream();
                BufferedWriter bufferedWriter3 = new BufferedWriter(new OutputStreamWriter(outputStream3, "UTF-8"));

                String post_data3 = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_Id, "UTF-8");
                Log.i("post_data: ", post_data3);

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

                    JSONObject responseJSON = new JSONObject(String.valueOf(result3));
                    Family_Id = String.valueOf(responseJSON.getInt("Family_Id"));
                    Log.i("Family_ID", Family_Id);


                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    //String post_data = URLEncoder.encode("family_id", "UTF-8") + "=" + URLEncoder.encode(Family_Id, "UTF-8");
                    String post_data1 = URLEncoder.encode("family_id", "UTF-8") + "=" + URLEncoder.encode(Family_Id, "UTF-8");
                    post_data1 += "&" + URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_Id, "UTF-8");
                    Log.i("familyId: ", post_data1);

                    bufferedWriter.write(post_data1);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();


                    statusCode = httpURLConnection.getResponseCode();
                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                        String line = "";
                        StringBuilder result = new StringBuilder();
                        while ((line = bufferedReader.readLine()) != null) {
                            result.append(line);
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                        Log.i("result", "[" + result + "]");


                        JSONObject gasData = new JSONObject(result.toString());
                        JSONArray gasVolumeLeft = gasData.getJSONArray("gasVolumeLeft");
                        for (int i = 0; i < gasVolumeLeft.length(); i++) {
                            gasVolume = gasVolumeLeft.getDouble(i);
                            Log.i("GAS_Weight_Empty", String.valueOf(gasVolume));

                            int customerID = LoginActivity.getCustomerID();

                            if (gasVolumeLeft.length() > 0) {
                                if ( gasVolume < 3) {
                                    //showNotification("Your gas volume is less than 2.5 kg");
                                    checkAndNotifyForFrequency(gasVolume);
                                }
                            } else {
                                Log.e("NotificationFrequency", "No gasVolumeLeft");
                            }
                        }



                        HttpURLConnection httpURLConnection1 = (HttpURLConnection) url1.openConnection();
                        httpURLConnection1.setRequestMethod("POST");
                        httpURLConnection1.setDoOutput(true);
                        httpURLConnection1.setDoInput(true);
                        OutputStream outputStream1 = httpURLConnection1.getOutputStream();
                        BufferedWriter bufferedWriter1 = new BufferedWriter(new OutputStreamWriter(outputStream1, "UTF-8"));

                        String post_data2 = URLEncoder.encode("family_id", "UTF-8") + "=" + URLEncoder.encode(Family_Id, "UTF-8");
                        post_data2 += "&" + URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_Id, "UTF-8");

                        Log.i("post_data1: ", post_data2);
                        bufferedWriter1.write(post_data2);
                        bufferedWriter1.flush();
                        bufferedWriter1.close();
                        outputStream1.close();


                        int statusCode1 = httpURLConnection1.getResponseCode();
                        if (statusCode1 == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream1 = httpURLConnection1.getInputStream();
                            BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1, "iso-8859-1"));
                            String line1 = "";
                            StringBuilder result1 = new StringBuilder();
                            while ((line1 = bufferedReader1.readLine()) != null) {
                                result1.append(line1);
                            }
                            bufferedReader1.close();
                            inputStream1.close();
                            httpURLConnection1.disconnect();
                            Log.i("result3", "[" + result1 + "]");

                            JSONObject gasData1 = new JSONObject(result1.toString());
                            JSONArray family_id = gasData1.getJSONArray("cus_id");
                            int customerID = LoginActivity.getCustomerID();
                            for (int i = 0; i < family_id.length(); i++) {
                                fam_id = family_id.getDouble(i);
                                family_Id.add((int) fam_id);
                                Log.i("Cus_Id", String.valueOf(fam_id));
                                // checkAndNotifyForFrequency(gasVolume);
                            }
                        }
                    }


                } else {
                    Log.e("Frequency HTTP", "HTTP Error: " + statusCode);
                }
            } catch (ProtocolException ex) {
                Log.e("Frequency Exception", "ProtocolException: " + ex.getMessage());
            } catch (MalformedURLException ex) {
                Log.e("Frequency Exception", "MalformedURLException: " + ex.getMessage());
            } catch (UnsupportedEncodingException ex) {
                Log.e("Frequency Exception", "UnsupportedEncodingException: " + ex.getMessage());
            } catch (JSONException ex) {
                Log.e("Frequency Exception", "JSONException: " + ex.getMessage());
            } catch (IOException ex) {
                Log.e("Frequency Exception", "IOException: " + ex.getMessage());
            }

            // Check if the response is the error message

            if (response.startsWith("One or more required variables are not set.")) {
                Log.e("Frequency Response", "Error: " + response);

                // Parse the response as a JSON array
                try {
                    JSONArray responseJSONArray = new JSONArray(response);
                    for (int i = 0; i < responseJSONArray.length(); i++) {
                        // Process the JSON data
                    }
                } catch (JSONException ex) {
                    Log.e("Frequency Exception", "JSONException: " + ex.getMessage());
                }
                return gasVolume;
            }
            return gasVolume;
        }
        protected void onPostExecute(Double gasVolume) {
            // Call the method to check and notify for frequency with the retrieved gasVolume value
            // checkAndNotifyForFrequency(gasVolume);
        }


        private void checkAndNotifyForFrequency(double gasVolume) {
            //if (gasVolume < 3) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            int desiredHour = hour;
            int desiredMinute = minute;

            if (gasVolume < 3 && (hour == 14 && minute == 0 || hour == 18 && minute == 00)) {
                showNotification("您的瓦斯容量小於" + 3 + "kg");
                if (hour >= 14 || (hour == 13 && minute >= 30)) {
                    // Afternoon, schedule the next notification for tomorrow morning
                    desiredHour = 14;
                    desiredMinute = 0;
                    showNotification("您的瓦斯容量小於" +  3 + "公斤");
                } else if (hour >= 8 && minute == 0) {
                    // Morning, schedule the next notification for this afternoon
                    desiredHour = 18;
                    desiredMinute = 00;
                    showNotification("您的瓦斯容量小於" + 3 + "公斤");
                }
            }
            scheduleNotification(desiredHour, desiredMinute, gasVolume);
//                scheduleNotification(14, 0, gasVolume);
//                scheduleNotification(22,03, gasVolume);
        }




        private void scheduleNotification(int desiredHour, int desiredMinute, double gasVolume) {
            // Get the current time and add one minute
            // long notificationTime = System.currentTimeMillis() + 60000;

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, desiredHour);
            calendar.set(Calendar.MINUTE, desiredMinute);
            calendar.set(Calendar.SECOND, 0);

            long notificationTime = calendar.getTimeInMillis();

            // If the desired time has already passed, schedule it for the next day
            if (notificationTime < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                notificationTime = calendar.getTimeInMillis();
            }
            // Create an Intent for the notification
            Intent notificationIntent = new Intent(NotificationFrequency.this, NotificationReceiver.class);
            notificationIntent.putExtra("gasVolume", gasVolume);

            // Create a PendingIntent for the notification
            PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationFrequency.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Get the AlarmManager instance
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Set the notification to trigger at the desired time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
            }
        }

        private void showNotification(String message) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            Intent homepageIntent = new Intent(NotificationFrequency.this, Homepage.class);
            homepageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent homepagePendingIntent = PendingIntent.getActivity(NotificationFrequency.this, 0, homepageIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationFrequency.this, "My Notification");
            builder.setContentTitle("SmartGasApp Notification");
            builder.setContentText(message);
            builder.setSmallIcon(R.drawable.baseline_shopping_cart_24);
            builder.setAutoCancel(true);

            builder.setContentIntent(homepagePendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NotificationFrequency.this);
            if (ActivityCompat.checkSelfPermission(NotificationFrequency.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(1, builder.build());
        }

    }
}
