package com.example.smartgasapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

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

public class NotificationForegroundService extends Service {
    private static final String CHANNEL_ID = "NotificationForegroundServiceChannel";
    private Handler handler;
    private Runnable notificationRunnable;
    private double gasVolume;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        scheduleNotificationCheck();
    }

    public void setGasVolume(double gasVolume) {
        this.gasVolume = gasVolume;
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

        // Schedule the first notification check using AlarmManager
        scheduleNotificationCheck();
        //NotificationForegroundService.setGasVolume(gasVolume);
    }


    private void frequency() throws IOException {
        try {

            // Check if gasVolume is less than 3 and show the notification if needed
            if (gasVolume < 3) {
                showNotification("您的瓦斯容量小於" + 3 + "kg"); // Adjust the message as per your requirement
            }

        } catch (Exception e) {
            Log.i("Frequency JSON Exception", e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification();
        startForeground(1, notification);
        // Schedule the task to run periodically using AlarmManager
        scheduleTask();
        // Start the notification check task here
        return START_STICKY;
    }

    private void scheduleTask() {
        // Create an Intent for your task
        Intent taskIntent = new Intent(this, NotificationReceiver.class);

        // Create a PendingIntent to be triggered by the AlarmManager
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                taskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE
        );
        // Get the AlarmManager and set the periodic alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long intervalMillis = 60 * 1000; // 1 minute
        long triggerAtMillis = System.currentTimeMillis();

        // Schedule the alarm to repeat at the specified interval
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartGasApp Notification")
                .setContentText("您的瓦斯容量小於" + 3 + "kg")
                .setSmallIcon(R.drawable.baseline_shopping_cart_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false) // Make the notification dismissible
                .build();

        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void showNotification(String message) {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartGasApp Notification")
                .setContentText("您的瓦斯容量小於" + 3 + "kg")
                .setSmallIcon(R.drawable.baseline_shopping_cart_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        startForeground(1, notification);
    }

    private void scheduleNotificationCheck() {
        // Use AlarmManager to schedule the next notification check
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long intervalMillis = 60000; // 1 minute (adjust this interval as per your requirement)
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}
