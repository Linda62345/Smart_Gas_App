package com.example.smartgasapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class EventPage extends AppCompatActivity {

    private ListView event_holder;
    RequestQueue requestQueue;
    int Companyid;
    String result;
    public JSONArray ja;
    ArrayList<AnnouncementList> announcementListArrayList;
    Dialog popDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        event_holder=findViewById(R.id.eventList);

        requestQueue = Volley.newRequestQueue(this);

        LoginActivity loginActivity = new LoginActivity();
        Companyid = loginActivity.COMPANY_Id;

        announcementListArrayList = new ArrayList<AnnouncementList>();

        popDialog = new Dialog(this);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Announcement();
            }
        });
        thread.start();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
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

    public void Announcement(){
        try{
            Log.i("announcent","annnn");
            String URL1 = "http://54.199.33.241/test/event_button.php";
            URL url = new URL(URL1);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Companyid), "UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
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
            Log.i("event result", "["+result+"]");

            AnnouncementArray();
        }
        catch (Exception e){
            Log.i("event Exception",e.toString());
        }
    }

    public void AnnouncementArray(){
        try{
            if(result.contains("failure")){
                Toast.makeText(getApplicationContext(), "無活動公告", Toast.LENGTH_LONG).show();
            }
            else if(result!=null){
                ja = new JSONArray(result);
                if(ja!=null) {
                    JSONObject jo = null;
                    Log.i("JA Size", String.valueOf(ja.length()));
                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);
                        String name, type, des;
                        //轉成中文
                        name = new String(jo.getString("Announcement_Name").getBytes("ISO-8859-1"), "UTF-8");
                        type = new String(jo.getString("Announcement_Type").getBytes("ISO-8859-1"), "UTF-8");
                        des = new String(jo.getString("Announcement_Description").getBytes("ISO-8859-1"), "UTF-8");
                        AnnouncementList announcementList1 = new AnnouncementList(name, type, des);
                        announcementListArrayList.add(announcementList1);

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(announcementListArrayList.size()>0){
                                //adapter
                                EventAdapter adapterList = new EventAdapter(getApplicationContext(), R.layout.adapter_event_list, announcementListArrayList);
                                event_holder.setAdapter(adapterList);
                                event_holder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        //當備案下時
                                        String name = announcementListArrayList.get(position).getName();
                                        String des = announcementListArrayList.get(position).getDescription();

                                        //pop up
                                        TextView T_name,T_des,T_diss;
                                        popDialog.setContentView(R.layout.event_pop_up);
                                        T_name = popDialog.findViewById(R.id.name);
                                        T_des = popDialog.findViewById(R.id.des);
                                        T_diss = popDialog.findViewById(R.id.diss);
                                        T_name.setText(name);
                                        T_des.setText(des);
                                        popDialog.show();

                                        T_diss.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                popDialog.dismiss();
                                            }
                                        });

                                    }});
                            }
                        }});
                }
            }
        }
        catch (Exception e){
            Log.i("event array exception",e.toString());
        }

    }

}