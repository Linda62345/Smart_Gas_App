package com.example.smartgasapp;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.smartgasapp.ui.login.LoginActivity;

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
import android.util.Pair;

public class NotificationFrequency extends AppCompatActivity {

    private RadioGroup radioGroup;
    private static RadioButton radioButton;
    private static RadioButton two;
    private static RadioButton three;
    public String Customer_Id, result;
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

        radioGroup = findViewById(R.id.radioGroup);
        two = findViewById(R.id.weightRadioTwo);
        three = findViewById(R.id.weightRadioThree);
        spinner = findViewById(R.id.frequency_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.notificationFrequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedFrequency = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedFrequency = "";
            }
        });

        Button enterButton = findViewById(R.id.enter);
        enterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    frequency();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        family_Id = new ArrayList<Integer>();
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

    public void frequency() throws IOException {
        try {
            Customer_Id = String.valueOf(LoginActivity.getCustomerID());
            Log.i("finish: ", Customer_Id);

            int radioId = radioGroup.getCheckedRadioButtonId();
            radioButton = findViewById(radioId);
            if (radioButton == two || radioButton == three) {
                new SendRequest().execute(Customer_Id);
            }

        } catch (Exception e) {
            Log.i("Frequency JSON Exception", e.toString());
        }
    }

    private class SendRequest extends AsyncTask<String, Void, Double> {
        protected Double doInBackground(String... params) {

            String response = "";
            double gasVolume = 0.0;
            double dep_cus_id = 0.0;
            try {
                String Customer_Id = params[0];
                String Showurl = "http://10.0.2.2/SQL_Connect/FrequencyNotification.php";
                String ShowUrl1 = "http://10.0.2.2/SQL_Connect/show_Dep_Cus_Id.php";
                URL url = new URL(Showurl);
                URL url1 = new URL(ShowUrl1);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_Id, "UTF-8");
                Log.i("post_data: ", post_data);

                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                int statusCode = httpURLConnection.getResponseCode();
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
                            if (radioButton == two && gasVolume < 2.5) {
                                //showNotification("Your gas volume is less than 2.5 kg");
                                checkAndNotifyForFrequency(gasVolume);
                            } else if (radioButton == three && gasVolume < 3.5) {
                                //showNotification("Your gas volume is less than 3.5 kg");
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

                    String post_data1 = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_Id, "UTF-8");
                    Log.i("post_data1: ", post_data1);

                    bufferedWriter1.write(post_data1);
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
                        Log.i("result", "[" + result1 + "]");

                    JSONObject gasData1 = new JSONObject(result1.toString());
                    JSONArray dep_id = gasData1.getJSONArray("dep_id");
                    int customerID = LoginActivity.getCustomerID();
                    for (int i = 0; i < dep_id.length(); i++) {
                        dep_cus_id = dep_id.getDouble(i);
                        family_Id.add((int) dep_cus_id);
                        Log.i("Dep_Cus_Id", String.valueOf(dep_cus_id));
                        if (customerID == dep_cus_id) {
                            checkAndNotifyForFrequency(gasVolume);
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
            checkAndNotifyForFrequency(gasVolume);
        }


        private void checkAndNotifyForFrequency(double gasVolume) {

                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    int desiredHour = hour;
                    int desiredMinute = minute;

                    double threshold = 0.0;
                    if (radioButton == two) {
                        threshold = 2.5;
                    } else if (radioButton == three) {
                        threshold = 3.5;
                    }

                    switch (selectedFrequency) {
                        case "早中":
                            if (gasVolume < threshold && (hour == 20 && minute == 22 || hour == 20 && minute == 00)) {
                                showNotification("您的瓦斯容量小於" + threshold + "kg");
                                if (hour >= 14 || (hour == 13 && minute >= 30)) {
                                    // Afternoon, schedule the next notification for tomorrow morning
                                    desiredHour = 20;
                                    desiredMinute = 22;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                } else if (hour >= 8 && minute == 0) {
                                    // Morning, schedule the next notification for this afternoon
                                    desiredHour = 20;
                                    desiredMinute = 00;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                }
                            }
                            break;
                        case "早晚":
                            if (gasVolume < threshold && (hour == 8 && minute == 00 || hour == 20 && minute == 00)) {
                                showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                if (hour >= 19 || (hour == 18 && minute >= 55)) {
                                    // Evening, schedule the next notification for tomorrow morning
                                    desiredHour = 8;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                } else if (hour >= 8) {
                                    // Morning, schedule the next notification for this evening
                                    desiredHour = 20;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                }
                            }
                            break;
                        case "中晚":
                            if (gasVolume < threshold && (hour == 14 && minute == 00 || hour == 20 && minute == 00)) {
                                showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                if (hour >= 20) {
                                    // Evening, schedule the next notification for tomorrow morning
                                    desiredHour = 14;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                } else if (hour >= 14) {
                                    // Afternoon, schedule the next notification for this evening
                                    desiredHour = 20;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                }
                            }
                            break;
                        case "早中晚":
                            if (gasVolume < threshold && (hour == 8 && minute == 00 || hour == 14 && minute == 00 || hour == 20 && minute == 00)) {
                                showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                if (hour >= 20 || hour < 8) {
                                    // Evening or early morning, schedule the next notification for this morning
                                    desiredHour = 8;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                } else if (hour >= 14) {
                                    // Afternoon, schedule the next notification for this evening
                                    desiredHour = 20;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                } else {
                                    // Morning, schedule the next notification for this afternoon
                                    desiredHour = 14;
                                    desiredMinute = 0;
                                    showNotification("您的瓦斯容量小於" + threshold + "公斤");
                                }
                            }
                            break;
                    }
                    scheduleNotification(desiredHour, desiredMinute, gasVolume);

            }
        private void scheduleNotification(int desiredHour, int desiredMinute, double gasVolume) {
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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationFrequency.this, "My Notification");
            builder.setContentTitle("SmartGasApp Notification");
            builder.setContentText(message);
            builder.setSmallIcon(R.drawable.baseline_shopping_cart_24);
            builder.setAutoCancel(true);

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
