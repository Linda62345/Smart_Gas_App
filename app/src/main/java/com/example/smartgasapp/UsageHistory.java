package com.example.smartgasapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class UsageHistory extends AppCompatActivity {
    public Spinner iot;
    public String Customer_Id,selectedSensorId,result;
    public TextView iot_gas1,iot_gas2;
    LineChart lineChart;
    JSONArray history;
    ArrayList<String> iotList = new ArrayList<>();
    ArrayList<String> xAxisValues = new ArrayList<>();
    ArrayAdapter<String> iotAdapter;
    ListView sensorlistView;
    ArrayList<String> sensorListString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        iot =findViewById(R.id.usageOption_Spinner);
        iot_gas1 = findViewById(R.id.changable_gas_specification);
        iot_gas2 = findViewById(R.id.changable_gas_remains);
        //lineChart = findViewById(R.id.getTheGraph);
        sensorlistView = findViewById(R.id.sensorlist);

        LoginActivity loginActivity = new LoginActivity();
        Customer_Id = String.valueOf(loginActivity.getCustomerID());
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    getData("http://54.199.33.241/test/iot.php",Customer_Id);
                    //處理手機上運行畫面問題
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                //選擇sensor_id
                                JSONArray ja = new JSONArray(result);
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jsonObject = ja.getJSONObject(i);
                                    String SENSOR_Id = jsonObject.optString("SENSOR_Id");
                                    //String SENSOR_Weight = jsonObject.optString("SENSOR_Weight");
                                    iotList.add("感應器ID: "+SENSOR_Id);
                                    iotAdapter = new ArrayAdapter<>(UsageHistory.this,
                                            android.R.layout.simple_spinner_item, iotList);
                                    iotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    iot.setAdapter(iotAdapter);
                                }
                                String selectedSensor = iot.getSelectedItem().toString();
                                String[] selectedSensorParts = selectedSensor.split(" ");
                                selectedSensorId = selectedSensorParts[1];
                                Log.i("選擇id",selectedSensorId.toString());
                            }
                            catch (Exception e){
                                Log.i("歷史用量UI Exception", e.toString());
                            }
                        }
                    });


                    //瓦斯桶記錄sensor_history
                    getData("http://54.199.33.241/test/iot_history.php",selectedSensorId);
                    sensorlist(selectedSensorId);

                    iot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedSensor = iot.getSelectedItem().toString();
                            String[] selectedSensorParts = selectedSensor.split(" ");
                            selectedSensorId = selectedSensorParts[1];
                            new GetHistoryTask().execute(selectedSensorId);
                            sensorlist(selectedSensorId);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // 未選擇任何項目時的處理
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();



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
    private class GetHistoryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String selectedSensorId = params[0];

            // Call getData and perform the network operation
            getData("http://54.199.33.241/test/iot_history.php", selectedSensorId);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Process the result or update the UI if needed
            // For example, you can call the sensorlist method here
            sensorlist(selectedSensorId);
        }
    }
    public void getData(String Showurl,String id) {
        try {

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
            Log.i("result", "[" + result + "]");

            // Process the retrieved data
            history = new JSONArray(result);

        } catch (Exception e) {
            Log.i("get data Exception", e.toString());
        }
    }

    public void sensorlist(String sensorid) {
        try {
            sensorListString = new ArrayList<String>();
            sensorlistView = findViewById(R.id.sensorlist);
            sensorlistView.setAdapter(null);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        // Check if the response is empty or not
                        if (!result.isEmpty()) {
                            JSONArray ja = new JSONArray(result);
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject jsonObject = ja.getJSONObject(i);
                                String SENSOR_Time = jsonObject.optString("SENSOR_Time");
                                String SENSOR_Weight = jsonObject.optString("SENSOR_Weight");
                                //根據sensor_id 去 iot table 拿空桶重

//                                if(){
//                                    String Gas_Weight_Empty =
//                                }
//                                else{
//                                    Toast.makeText(getApplicationContext(), "此重量尚未扣除空桶重", Toast.LENGTH_LONG).show();
//                                }
                                sensorListString.add("時間: " + SENSOR_Time + " 流量: " + SENSOR_Weight);
                                //目前最後一筆資料
                                iot_gas1.setText(SENSOR_Weight);
                                iot_gas2.setText(SENSOR_Weight);
                            }

                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(UsageHistory.this,
                                android.R.layout.simple_list_item_1, sensorListString);
                        sensorlistView.setAdapter(adapter);
                    }
                    catch (Exception e){
                        Log.i("歷史用量記錄UI Exception",e.toString());
                    }
                }
            });
        } catch (Exception e) {
            Log.i("sensor list exception", e.toString());
        }
    }

}