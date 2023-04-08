package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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

public class GasCompanyLocation extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button confirm;
    private TextView changableGasCompNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_company_location);

        // get reference to the RadioGroup and TextView
        confirm = findViewById(R.id.confirmCompany);
        radioGroup = findViewById(R.id.radioGroup);
        changableGasCompNameView = findViewById(R.id.Changable_GasCompName_View);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // add listener to the RadioGroup to change the text in the TextView when a RadioButton is selected
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        TextView gasCompNameView = findViewById(R.id.Changable_GasCompName_View);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                String selectedText = radioButton.getText().toString();
                gasCompNameView.setText(selectedText);
            }
        });


        // set listener to the Confirm Button
        Button confirmCompanyButton = findViewById(R.id.confirmCompany);
        confirmCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle the confirmation logic here
            }
        });

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
                            RadioButton radioButton = new RadioButton(GasCompanyLocation.this);
                            radioButton.setText(companyName);
                            radioButton.setId(i);
                            radioGroup.addView(radioButton);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });



            requestQueue.add(jsonObjectRequest);

        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GasCompanyLocation.this, Homepage.class);
                startActivity(intent);
            }
        });


    }
}

