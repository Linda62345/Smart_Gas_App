package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class EventPage extends AppCompatActivity {

    private ListView event_text_holder;
    ArrayList<String> announcementList = new ArrayList<>();
    ArrayAdapter<String> announcementAdapter;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        event_text_holder=findViewById(R.id.eventList);


        Announcement();


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
            String URL1 = "http://10.0.2.2/SQL_Connect/event_button.php";
            JsonObjectRequest jsonObjectRequest;

            {
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        URL1, null, new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        Log.i("EventPage",response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("announcement");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String announcementName = jsonObject.optString("ANNOUNCEMENT_Name");
                                announcementList.add(announcementName);
                                announcementAdapter = new ArrayAdapter<>(EventPage.this,
                                        android.R.layout.simple_spinner_item, announcementList);

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
        catch(Exception e){
            Log.i("Event Page exception",e.toString());
        }

    }
}