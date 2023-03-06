package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserDashboard extends AppCompatActivity {

    private Button userInfo;
    private Button volumeInfo;
    private Button search;
    private Button usageHistory;
    private Button notification;
    private Button exchange;
    private Button eventOrAct;
    private Button famCoupon;
    private Button logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        userInfo = findViewById(R.id.go_edit_profile);
        volumeInfo = findViewById(R.id.seeMoreButtoninfo);
        search = findViewById(R.id.search);
        usageHistory = findViewById(R.id.usageHistory);

        notification = findViewById(R.id.notificationFrequency);
        exchange = findViewById(R.id.volExchange);
        eventOrAct = findViewById(R.id.activityButton);

        famCoupon = findViewById(R.id.familyCodeButton);
        logout = findViewById(R.id.logout_button);



        userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, EditPersonalInfo.class);
                startActivity(intent);
            }
        });
        volumeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, UsageHistory.class);
                startActivity(intent);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, OrderListUnfinished.class);
                startActivity(intent);
            }
        });
        usageHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, UsageHistory.class);
                startActivity(intent);
            }
        });
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, NotificationFrequency.class);
                startActivity(intent);
            }
        });
        exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, GasExchange.class);
                startActivity(intent);
            }
        });
        eventOrAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, EventPage.class);
                startActivity(intent);
            }
        });
        famCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, FamilyInvitationCode.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(),UserDashboard.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(),Homepage.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(getApplicationContext(),OrderListUnfinished.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });




    }
}