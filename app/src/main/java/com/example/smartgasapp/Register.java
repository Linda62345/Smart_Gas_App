package com.example.smartgasapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button register;
    private RadioGroup radioGroup;
    private EditText etName, etEmail, etAddress, etPhone, etHouseTel, etPassword, etReenterPassword;
    private RadioButton etMale, etFemale, radioButton;
    private TextView tvStatus;
    private Spinner etCity, etArea, etCompanyName;
    ArrayList<String> countryList = new ArrayList<>();
    ArrayList<String> cityList = new ArrayList<>();
    ArrayAdapter<String> countryAdapter;
    ArrayAdapter<String> cityAdapter;
    ArrayList<String> companyList = new ArrayList<>();
    ArrayAdapter<String> companyAdapter;
    private String URL = "http://10.0.2.2/SQL_Connect/customer_register.php";



    private String name, email, address, phone, houseTel, password, reenterPassword, gender, company, city, area;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        radioGroup = findViewById(R.id.radioGroup);
        etName = findViewById(R.id.register_name_input);
        etMale = findViewById(R.id.radioButton_male);
        etFemale = findViewById(R.id.radioButton_female);
        etCity = findViewById(R.id.citySpinner);
        etAddress = findViewById(R.id.register_addr_input);
        etPhone = findViewById(R.id.register_phone_input);
        etHouseTel = findViewById(R.id.register_housephone_input);
        etEmail = findViewById(R.id.register_email_input);
        etPassword = findViewById(R.id.register_pass_input);
        etReenterPassword = findViewById(R.id.register_pass_con_input);
        name = email = password = reenterPassword = address = phone = houseTel = "";
        register = findViewById(R.id.register_next_button);
        tvStatus = findViewById(R.id.tvStatus);

        requestQueue = Volley.newRequestQueue(this);
        etCompanyName = findViewById(R.id.citySpinner);
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
                            companyAdapter = new ArrayAdapter<>(Register.this,
                                    android.R.layout.simple_spinner_item, companyList);
                            companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            etCompanyName.setAdapter(companyAdapter);

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

        // 連到城市的資料庫
        String URL2 = "http://10.0.2.2/SQL_Connect/country.php";
        JsonObjectRequest jsonObjectRequest1;

        {
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    URL2, null, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("country");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String country_name = jsonObject.optString("country_name");
                            countryList.add(country_name);
                            countryAdapter = new ArrayAdapter<>(Register.this,
                                    android.R.layout.simple_spinner_item, countryList);
                            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            etCity.setAdapter(countryAdapter);

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
            etCity.setOnItemSelectedListener(this);
        }

    }
    // 連到地區的資料庫
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getId() == R.id.citySpinner){
            cityList.clear();
            String selectedCountry = adapterView.getSelectedItem().toString();
            String url = "http://10.0.2.2/SQL_Connect/city.php?country_name="+selectedCountry;
            requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("city");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String cityName = jsonObject.optString("city_name");
                            cityList.add(cityName);
                            cityAdapter = new ArrayAdapter<>(Register.this,
                                    android.R.layout.simple_spinner_item, cityList);
                            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            etArea.setAdapter(cityAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void onClick(View v) {
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);

    }

    public void checkButton(View v) {
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);
    }

    public void save(View view) {
        //這裡要加個警告 姓名欄一定要填
        if (etMale.isChecked()) {
            gender = "Male";
        } else {
            gender = "Female";
        }
        city = etCity.getSelectedItem().toString();
        area = etArea.getSelectedItem().toString();
        String companyName = etCompanyName.getSelectedItem().toString();
        company = companyName;
        address = city + area + etAddress.getText().toString().trim();
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        phone = etPhone.getText().toString().trim();
        houseTel = etHouseTel.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        reenterPassword = etReenterPassword.getText().toString().trim();
        if (!password.equals(reenterPassword)) {
            Toast.makeText(this, "Password Mismatch", Toast.LENGTH_SHORT).show();
        } else if (!etMale.isChecked() && !etFemale.isChecked()) {
            Toast.makeText(this, "Gender column must selected one of them.", Toast.LENGTH_SHORT).show();
        } else if (!name.equals("") && !email.equals("") && !password.equals("")) {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Register.this, Homepage.class);
            startActivity(intent);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Register response",response);
                    if (response.equals("success")) {
                        Intent intent = new Intent(Register.this, Homepage.class);
                        startActivity(intent);
                        tvStatus.setText("Successfully registered.");
                        register.setClickable(false);
                    } else if (response.equals("failure")) {
                        tvStatus.setText("Something went wrong!");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                    Log.i("Register error",error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", name);
                    data.put("sex", gender);
                    data.put("phone", phone);
                    data.put("houseTel", houseTel);
                    data.put("email", email);
                    data.put("password", password);
                    data.put("address", address);
                    data.put("company", company);

                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        } else {
            Toast.makeText(this, "Please fill all the field", Toast.LENGTH_SHORT).show();
        }
    }


}