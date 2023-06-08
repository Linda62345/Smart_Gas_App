package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
    public ListView listView;
    public ArrayList<String> name;
    public static ArrayList<Integer> family_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_invitation_code);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listview);
        name = new ArrayList<String>();
        family_Id = new ArrayList<Integer>();

        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());
        myId = findViewById(R.id.changable_userID);
        myId.setText("ID: "+Customer_ID);

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
                //還要去確認是否有這個id存在

                //為了讓加入的時候有反應
                name = new ArrayList<String>();
                family_Id = new ArrayList<Integer>();
                SaveFamilyMember();
                dataonlist();
            }
        });

        dataonlist();

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
                showData("http://10.0.2.2/SQL_Connect/Show_FamilyMember.php",Customer_ID);
                try {
                    ja = new JSONArray(result);
                    if(ja!=null){
                        JSONObject jo = null;
                        Log.i("JA Size", String.valueOf(ja.length()));
                        for(int i = 0;i<ja.length();i++){
                            jo = ja.getJSONObject(i);
                            name.add(jo.getString("Customer_Name"));
                            family_Id.add(jo.getInt("Dep_Cus_Id"));
                        }
                        name.add(ja.getJSONObject(0).getString("Customer_Id_Name"));
                        family_Id.add(ja.getJSONObject(0).getInt("Customer_Id"));
                    }
                    Log.i("name size", String.valueOf(name.size()));
                    Log.i("family id size",String.valueOf(family_Id.size()));

                    if(name.size()>0){
                        FamilyMemberAdapterList adapterList = new FamilyMemberAdapterList(getApplicationContext(),R.layout.adapter_family_member,name);
                        listView.setAdapter(adapterList);
                    }
                }
                catch (Exception e){
                    Log.i("JA Exception",e.toString());
                }
            }
        });
        thread.start();
    }
    public void SaveFamilyMember(){
        //customer, id
        //totalVolume
        //update + or insert
        try {
            String URL = "http://10.0.2.2/SQL_Connect/Save_FamilyMember.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equals("success")) {
                        Log.i("Save Family Member", "Successfully store family member.");
                        Toast.makeText(getApplicationContext(), "家人新增成功", Toast.LENGTH_LONG).show();
                        FamilyMember.setText("");
                        //如果新增成功 要更新listview的資料
                        showData("http://10.0.2.2/SQL_Connect/Show_FamilyMember.php",Customer_ID);
                    } else if (response.equals("failure")) {
                        Log.i("Family Member failure", "Something went wrong!");
                    }
                }}, new Response.ErrorListener() {
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
        }
        catch (Exception e){
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