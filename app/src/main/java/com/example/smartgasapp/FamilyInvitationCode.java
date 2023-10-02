package com.example.smartgasapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FamilyInvitationCode extends AppCompatActivity {
    public String Customer_ID, Family_Member_Email,result;
    public TextView myId;
    public EditText FamilyMember;
    public Button save;
    public JSONObject responseJSON;
    public JSONArray ja;
    public static ListView listView;
    public static ArrayList<String> name;
    public static ArrayList<Integer> family_Id;
    static LoginActivity loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_invitation_code);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listview);
        name = new ArrayList<String>();
        family_Id = new ArrayList<Integer>();

        loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());
        myId = findViewById(R.id.changable_userID);
        myId.setText(Customer_ID);

        FamilyMember = findViewById(R.id.requestID_Input);
        FamilyMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                Family_Member_Email = FamilyMember.getText().toString().trim();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
        });


        save = findViewById(R.id.confirm_exchange_button);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = new ArrayList<String>();
                family_Id = new ArrayList<Integer>();
                if(Family_Member_Email != null && !Family_Member_Email.equals("")){
                    SaveFamilyMember();
                }
                else{
                    Toast.makeText(getApplicationContext(), "請輸入家人手機號碼", Toast.LENGTH_LONG).show();
                }
                listView = findViewById(R.id.listview);
            }
        });

        //Update Ad
        dataonlist();

        //delete


        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

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

    public void dataonlist(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                showData("http://54.199.33.241/test/Show_FamilyMember_2.php",Customer_ID);
                try {
                    Log.i("family result",result);
                    if(result.equals("failure")){
                        Toast.makeText(getApplicationContext(), "無關聯家人", Toast.LENGTH_LONG).show();
                    }
                    else if(result!=null){
                        ja = new JSONArray(result);
                        if(ja!=null){
                            JSONObject jo = null;
                            Log.i("JA Size", String.valueOf(ja.length()));
                            for(int i = 0;i<ja.length();i++){
                                jo = ja.getJSONObject(i);
                                Log.i("customer name",jo.getString("Customer_Name"));
                                name.add(new String(jo.getString("Customer_Name").getBytes("ISO-8859-1"), "UTF-8"));
                                family_Id.add(jo.getInt("Customer_Id"));
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (name.size() > 0) {
                                    FamilyMemberAdapterList adapterList = new FamilyMemberAdapterList(getApplicationContext(), R.layout.adapter_family_member, name);
                                    listView.setAdapter(adapterList);
                                }
                            }
                        });
                    }
                }
                catch (Exception e){
                    Log.i("JA Exception",e.toString());
                }
            }
        });
        thread.start();

    }


    public void SaveFamilyMember() {
        try {
            String URL = "http://54.199.33.241/test/Save_FamilyMember_2.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Family response", response);
                    if (response.contains("success")) {
                        Log.i("Save Family Member", "Successfully store family member.");
                        Toast.makeText(getApplicationContext(), "家人新增成功", Toast.LENGTH_LONG).show();
                        FamilyMember.setText("");
                        //如果新增成功 要更新listview的資料
                        Log.i("Save Family", response);
                        //畫面更新
                        dataonlist(); // Refresh the ListView data
                    } else if (response.contains("failure")) {
                        Toast.makeText(getApplicationContext(), "錯誤", Toast.LENGTH_LONG).show();
                    } else if (response.contains("No Customer")) {
                        Toast.makeText(getApplicationContext(), "此號碼不存在", Toast.LENGTH_LONG).show();
                    } else if (response.contains("Duplicate entry")) {
                        Toast.makeText(getApplicationContext(), "此號碼已加入群組", Toast.LENGTH_LONG).show();
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
                    data.put("id", Customer_ID);
                    data.put("Family_Phone", Family_Member_Email);
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            Log.i("Family Member Exception", e.toString());
        }
    }

    public void showData(String Showurl,String id){
        try{
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String line = "";
            result = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            Log.i("Family result", "["+result+"]");
        }
        catch (Exception e){
            Log.i("show family Exception",e.toString());
        }
    }

}