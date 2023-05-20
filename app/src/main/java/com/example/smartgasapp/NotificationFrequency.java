package com.example.smartgasapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

public class NotificationFrequency extends AppCompatActivity {

    private RadioGroup radioGroup;
    private static RadioButton radioButton;
    private static RadioButton two;
    private static RadioButton three;
    public String Customer_Id;
    public static int gasVolumeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_frequency);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        radioGroup = findViewById(R.id.radioGroup);
        two = findViewById(R.id.weightRadioTwo);
        three = findViewById(R.id.weightRadioThree);

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

    private class SendRequest extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {
            String response = "";
            try {
                String Customer_Id = params[0];
                String Showurl = "http://10.0.2.2/SQL_Connect/FrequencyNotification.php";
                URL url = new URL(Showurl);
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

//                    JSONArray responseJSONArray = new JSONArray(result.toString());
                    JSONObject gasData = new JSONObject(result.toString());
                    gasVolumeLeft = gasData.getInt("gasVolumeLeft");
                    Log.i("GAS_Weight_Empty", String.valueOf(gasVolumeLeft));

                    if (gasVolumeLeft > 0) {
                        double gasVolume = Double.parseDouble(String.valueOf(gasVolumeLeft));
                        if (radioButton == two && gasVolume < 2.5) {
                            showNotification("Your gas volume is less than 2.5 kg");
                        } else if (radioButton == three && gasVolume < 3.5) {
                            showNotification("Your gas volume is less than 3.5 kg");
                        }
                    } else {
                        Log.e("NotificationFrequency", "No gasVolumeLeft");
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

                return null;
            }
            return null;
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
