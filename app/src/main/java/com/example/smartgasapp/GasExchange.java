package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class GasExchange extends AppCompatActivity {
    public Spinner Company;
    ArrayList<String> companyList = new ArrayList<>();
    ArrayAdapter<String> companyAdapter;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_exchange);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        Company = findViewById(R.id.gasCompany_spinner);
        requestQueue = Volley.newRequestQueue(this);
        Company();

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
    public void Company(){
        String URL1 = "http://10.0.2.2/SQL_Connect/company.php";
        JsonObjectRequest jsonObjectRequest;

        {
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    URL1, null, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("company");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String companyName = jsonObject.optString("COMPANY_Name");
                            companyList.add(companyName);
                            companyAdapter = new ArrayAdapter<>(GasExchange.this,
                                    android.R.layout.simple_spinner_item, companyList);
                            companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            Company.setAdapter(companyAdapter);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }
}