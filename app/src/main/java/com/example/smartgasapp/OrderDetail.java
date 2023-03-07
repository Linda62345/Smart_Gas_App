package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class OrderDetail extends AppCompatActivity {

    private Button exchange;
    private Button editReceipt;
    private Button finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        exchange = findViewById(R.id.receipt_exchange_status);
        editReceipt = findViewById(R.id.receipt_edit_button);
        finish = findViewById(R.id.receipt_next_button);

        exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetail.this, GasExchange.class);
                startActivity(intent);
            }
        });
        editReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetail.this, OrderGas.class);
                startActivity(intent);
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetail.this, OrderListUnfinished.class);
                startActivity(intent);
            }
        });
    }
}