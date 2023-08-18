package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSuccess extends AppCompatActivity {
    public Button check;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchange_succed);

        check = findViewById(R.id.exchangeSuccessNext);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderSuccess.this, Homepage.class);
                startActivity(intent);
            }
        });
    }
}
