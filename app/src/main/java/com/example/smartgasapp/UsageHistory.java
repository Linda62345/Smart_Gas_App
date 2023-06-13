package com.example.smartgasapp;

import android.content.Intent;
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
    //GraphView graphView;
    //LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        iot =findViewById(R.id.usageOption_Spinner);
        iot_gas1 = findViewById(R.id.changable_gas_specification);
        iot_gas2 = findViewById(R.id.changable_gas_remains);
        lineChart = findViewById(R.id.getTheGraph);
        //graphView = findViewById(R.id.getTheGraph2);
        //series = new LineGraphSeries<>(getDatePoint());

        LoginActivity loginActivity = new LoginActivity();
        Customer_Id = String.valueOf(loginActivity.getCustomerID());
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //選擇sensor_id
                    getData("http://10.0.2.2/SQL_Connect/iot.php",Customer_Id);
                    JSONArray ja = new JSONArray(result);
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jsonObject = ja.getJSONObject(i);
                        String SENSOR_Id = jsonObject.optString("SENSOR_Id");
                        String SENSOR_Weight = jsonObject.optString("SENSOR_Weight");
                        iotList.add("感應器ID: "+SENSOR_Id+" 重量: "+SENSOR_Weight);
                        iotAdapter = new ArrayAdapter<>(UsageHistory.this,
                                android.R.layout.simple_spinner_item, iotList);
                        iotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        iot.setAdapter(iotAdapter);
                    }
                    String selectedSensor = iot.getSelectedItem().toString();
                    String[] selectedSensorParts = selectedSensor.split(" ");
                    selectedSensorId = selectedSensorParts[1];
                    Log.i("seonsor_id",selectedSensorId);

                    //瓦斯桶容量
                    iot_gas1.setText(selectedSensorParts[3]);
                    iot_gas2.setText(selectedSensorParts[3]);

                    //瓦斯桶記錄sensor_history
                    getData("http://10.0.2.2/SQL_Connect/iot_history.php",selectedSensorId);
                    Log.i("iot history",result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        iot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSensor = iot.getSelectedItem().toString();
                String[] selectedSensorParts = selectedSensor.split(" ");
                selectedSensorId = selectedSensorParts[1];
                Log.i("seonsor_id",selectedSensorId);
                iot_gas1.setText(selectedSensorParts[3]);
                iot_gas2.setText(selectedSensorParts[3]);
                //更新圖表
                //瓦斯桶記錄sensor_history
                getData("http://10.0.2.2/SQL_Connect/iot_history.php",selectedSensorId);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 未選擇任何項目時的處理
            }
        });


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

            // Update the graph
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataValue();
                }
            });
        } catch (Exception e) {
            Log.i("iot Exception", e.toString());
        }
    }
    private void dataValue(){
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        try{
            xAxisValues = new ArrayList<>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd");

            for (int i = 0; i < history.length(); i++) {
                JSONObject jsonObject = history.getJSONObject(i);
                String SENSOR_Weight = jsonObject.optString("SENSOR_Weight");
                String SENSOR_Time = jsonObject.optString("SENSOR_Time");

                Date date = inputFormat.parse(SENSOR_Time);
                String formattedDate = outputFormat.format(date);
                Log.i("formattedDate", formattedDate);

                Entry entry = new Entry(i, Float.parseFloat(SENSOR_Weight));
                dataVals.add(entry);

                xAxisValues.add(formattedDate);  // Add formattedDate to the x-axis labels list
            }

            LineDataSet lineDataSet = new LineDataSet(dataVals,"瓦斯容量");
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);
            LineData data = new LineData(dataSets);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

            lineChart.setData(data);
            lineChart.invalidate();
        }
        catch (Exception e){
            Log.i("history array exception",e.toString());
        }
    }
}