package com.example.smartgasapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity {

    private Button point;
    private Button homeLogin;
    private Button moreVol;

    private ImageButton buy;
    private ImageButton search;


    private ImageButton exchange;
    private ImageButton location;
    private ImageButton personalBarcode;


    private ViewPager viewPager;
    private ImageAdapter adapter;
    private ArrayList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Create sample images
        images = new ArrayList<>();
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.gasspec));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.ourproduct));

        // Set adapter to ViewPager
        adapter = new ImageAdapter(this, images);
        //viewPager.setAdapter(adapter);

        point = findViewById(R.id.changable_pointButton);
        homeLogin = findViewById(R.id.loginFromHome);
        moreVol = findViewById(R.id.seeMoreButton);

        buy = findViewById(R.id.buyGasButton);
        search = findViewById(R.id.findOrderListButton);

        exchange = findViewById(R.id.exchangeGasButton);
        location = findViewById(R.id.companyButton);
        personalBarcode = findViewById(R.id.myIDButton);

        //ViewPager viewPager = findViewById(R.id.viewPager2);
        //ImageAdapter adapter = new ImageAdapter(this, images);
        //viewPager.setAdapter(adapter);




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
                Intent intent = new Intent(Homepage.this, OrderDetail.class);
                startActivity(intent);
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




        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

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