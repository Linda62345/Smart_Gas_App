package com.example.smartgasapp;

import static com.example.smartgasapp.R.id.navigation_dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GasCompanyLocation extends AppCompatActivity {

    public int CUSTOMER_ID;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button confirm;
    private TextView changableGasCompNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_company_location);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        LoginActivity loginActivity = new LoginActivity();
        CUSTOMER_ID = loginActivity.getCustomerID();
        Log.i("editInfo", String.valueOf(CUSTOMER_ID));

        // get reference to the RadioGroup and TextView
        confirm = findViewById(R.id.confirmCompany);
        radioGroup = findViewById(R.id.radioGroup);
        changableGasCompNameView = findViewById(R.id.Changable_GasCompName_View);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // add listener to the RadioGroup to change the text in the TextView when a RadioButton is selected

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                String selectedText = radioButton.getText().toString();
                changableGasCompNameView.setText(selectedText);
            }
        });




        String URL1 = "http://54.199.33.241/test/company.php";
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





        // set listener to the Confirm Button
        Button confirmCompanyButton = findViewById(R.id.confirmCompany);
        confirmCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GasCompanyLocation.this, Homepage.class);
                startActivity(intent);

                String URL = "http://54.199.33.241/test/customer_gasLocation.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Log.i("saveGasLocation", "Success.");
                            Intent intent = new Intent(GasCompanyLocation.this, Homepage.class);
                            intent.putExtra("company", changableGasCompNameView.getText().toString());
                            startActivity(intent);
                        } else if (response.equals("failure")) {
                            Log.i("gasLocation", "Something went wrong!");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> data = new HashMap<>();
                        data.put("company", changableGasCompNameView.getText().toString());
                        data.put("id", String.valueOf(CUSTOMER_ID));
                        return data;
                    }
                };

                requestQueue.add(stringRequest);


            }
        });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(), UserDashboard.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(), Homepage.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(getApplicationContext(), OrderListUnfinished.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

    }
}


