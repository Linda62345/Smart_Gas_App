package com.example.smartgasapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditPersonalInfo extends AppCompatActivity {

    public int CUSTOMER_ID;
    public String CUSTOMER_Name, CUSTOMER_Address, CUSTOMER_Email, COMPANY_Id;
    public int CUSTOMER_Tel,CUSTOMER_Phone;
    private EditText Name, Address, Email, Tel, Phone;
    private Button save;
    public String customer_name="",phone="",tel="",address="",email="", company="";

    public Homepage homepage;
    private Spinner etCompanyName;
    ArrayList<String> companyList = new ArrayList<>();
    ArrayAdapter<String> companyAdapter;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);
        Name = findViewById(R.id.editName);
        Address = findViewById(R.id.editCompanyAddress);
        Email = findViewById(R.id.editEmail);
        Tel = findViewById(R.id.editHousePhone);
        Phone = findViewById(R.id.editPhoneNo);
        save = findViewById(R.id.saveInfo_button);

        LoginActivity loginActivity = new LoginActivity();
        CUSTOMER_ID = loginActivity.getCustomerID();
        Log.i("editInfo", String.valueOf(CUSTOMER_ID));

        requestQueue = Volley.newRequestQueue(this);
        etCompanyName = findViewById(R.id.company);
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
                            companyList.add(companyName);
                            companyAdapter = new ArrayAdapter<>(EditPersonalInfo.this,
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

        //因為呼叫API(Internet) 所以要用thread避免等待時間過長
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    showData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Name = findViewById(R.id.editName);
                Address = findViewById(R.id.editCompanyAddress);
                Email = findViewById(R.id.editEmail);
                Tel = findViewById(R.id.editHousePhone);
                Phone = findViewById(R.id.editPhoneNo);
                etCompanyName = findViewById(R.id.company);
                saveProfile();
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }
    public void showData() throws MalformedURLException {
        try{
            String Showurl = "http://54.199.33.241/test/Show_Customer_Profile.php";
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(CUSTOMER_ID), "UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String result = "";
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            Log.i("result", "["+result+"]");
            JSONObject responseJSON = new JSONObject(result);
            CUSTOMER_Name = responseJSON.getString("CUSTOMER_Name");
            Name.setText(CUSTOMER_Name);
            CUSTOMER_Phone = responseJSON.getInt("CUSTOMER_Phone");
            Phone.setText(String.valueOf(CUSTOMER_Phone));
            CUSTOMER_Tel = responseJSON.getInt("CUSTOMER_Tel");
            Tel.setText(String.valueOf(CUSTOMER_Tel));
            CUSTOMER_Address = responseJSON.getString("CUSTOMER_Address");
            Address.setText(CUSTOMER_Address);
            CUSTOMER_Email = responseJSON.getString("CUSTOMER_Email");
            Email.setText(CUSTOMER_Email);

            Log.i("CUSTOMER_Name",CUSTOMER_Name);
        } catch (Exception e) {
            Log.i("Edit Exception", e.toString());
        }
    }

    public void saveProfile(){
        try {
            String URL = "http://54.199.33.241/test/Save_Customer_Profile.php";
            customer_name = Name.getText().toString().trim();
            phone = Phone.getText().toString().trim();
            tel = Tel.getText().toString().trim();
            address = Address.getText().toString().trim();
            email = Email.getText().toString().trim();
            String companyName = etCompanyName.getSelectedItem().toString();
            company = companyName;
            if (customer_name.equals("") || phone.equals("") || tel.equals("") || address.equals("") || email.equals("") || company.equals("") ) {
                Toast.makeText(this, "以上不可為空白", Toast.LENGTH_SHORT).show();
            } else {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Log.i("saveProfile", "Successfully registered.");
                            Intent intent = new Intent(EditPersonalInfo.this, Homepage.class);
                            intent.putExtra("email", Email.getText().toString());
                            startActivity(intent);
                            save.setClickable(false);
                        } else if (response.equals("failure")) {
                            Log.i("saveProfile", "Something went wrong!");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> data = new HashMap<>();
                        data.put("id", String.valueOf(CUSTOMER_ID));
                        data.put("name", customer_name);
                        data.put("phone", phone);
                        data.put("houseTel", tel);
                        data.put("email", email);
                        data.put("address", address);
                        data.put("company", company);

                        return data;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);
            }
        }
        catch (Exception e){
            Log.i("Save Exception", e.toString());
        }
    }
}