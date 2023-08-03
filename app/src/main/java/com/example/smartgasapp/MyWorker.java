package com.example.smartgasapp;

import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.work.WorkManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smartgasapp.ui.login.LoginActivity;

import java.io.IOException;
import java.util.Calendar;

public class MyWorker extends Worker {
    private final Context context;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            double gasVolume = getInputData().getDouble("gasVolume", 0.0);

            // Show the notification
            checkAndNotifyForFrequency(gasVolume);
           // showNotification("您的瓦斯容量小於" + 3 + "公斤");
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            // Return failure if the task encounters an error
            return Result.failure();
        }
    }

    private void checkAndNotifyForFrequency(double gasVolume) {
        if (gasVolume < 3) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            int desiredHour = hour;
            int desiredMinute = minute;

            if (gasVolume < 3 && (hour == 14 && minute == 00 || hour == 18 && minute == 00)) {
                showNotification("您的瓦斯容量小於" + 3 + "kg");
                if (hour >= 14 || (hour == 13 && minute >= 30)) {
                    // Afternoon, schedule the next notification for tomorrow morning
                    desiredHour = 14;
                    desiredMinute = 00;
                    showNotification("您的瓦斯容量小於" +  3 + "公斤");
                } else if (hour >= 8 && minute == 0) {
                    // Morning, schedule the next notification for this afternoon
                    desiredHour = 18;
                    desiredMinute = 00;
                    showNotification("您的瓦斯容量小於" + 3 + "公斤");
                }
            }
            scheduleNotification(desiredHour, desiredMinute, gasVolume);
        }
    }

    private void scheduleNotification(int desiredHour, int desiredMinute, double gasVolume) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("gasVolume", gasVolume);

        // Create a PendingIntent for the notification
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the notification to trigger at the desired time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    private Object getSystemService(String alarmService) {
        return null;
    }

    private void showNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent homepageIntent = new Intent(context, Homepage.class);
        homepageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent homepagePendingIntent = PendingIntent.getActivity(context, 0, homepageIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent repeating_intent = new Intent(context, NotificationFrequency.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, repeating_intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "My Notification");
        builder.setContentTitle("SmartGasApp Notification");
        builder.setContentText("您的瓦斯容量小於" + 3 + "公斤");
        builder.setSmallIcon(R.drawable.baseline_shopping_cart_24);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        builder.setContentIntent(pendingIntent);
        builder.setContentIntent(homepagePendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

