package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class cylinder_gas_menu extends AppCompatActivity {

    private Button composite;

    private Button order;
    private Button delivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cylinder_gas_menu);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        composite = findViewById(R.id.compositeGas);
        order = findViewById(R.id.cylinderGas);
        delivery = findViewById(R.id.ChooseDeliverMethod);



        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cylinder_gas_menu.this, OrderGas.class);
                startActivity(intent);
            }
        });

        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cylinder_gas_menu.this, DeliveryMethod.class);
                startActivity(intent);
            }
        });

        composite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cylinder_gas_menu.this, CompositeGasMenu.class);
                startActivity(intent);
            }
        });

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
