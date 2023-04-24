package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
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
import java.util.Objects;

public class OrderListFinished extends AppCompatActivity {

    private Button unfinished, finished, order;
    private ListView orderList;
    public int Customer_Id;
    InputStream is = null;
    String line,result = "";
    String[] data,order_Id;
    public static String static_order_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list_finished);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LoginActivity loginActivity = new LoginActivity();
        Customer_Id = loginActivity.getCustomerID();
        unfinished = findViewById(R.id.order_unfinished);
        orderList = (ListView) findViewById(R.id.list_item);
        order = findViewById(R.id.enterSearch);

        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));
        getData("http://10.0.2.2/SQL_Connect/customer_OrderList.php");
        try {
            getOrderList("http://10.0.2.2/SQL_Connect/customer_OrderList.php");
        } catch (Exception e) {
            Log.i("OrderList create Exception", e.toString());
        }
        setAdapter();


       unfinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderListFinished.this, OrderListUnfinished.class);
                startActivity(intent);
                orderList.setAdapter(null);
                getData("http://10.0.2.2/SQL_Connect/customer_UnOrderList.php");
                try {
                    getOrderList("http://10.0.2.2/SQL_Connect/customer_UnOrderList.php");
                } catch (Exception e) {
                    Log.i("UnOrderList create Exception", e.toString());
                }
                setAdapter();
            }
        });
    }

    private void setAdapter() {
        if(data!=null){
            ArrayAdapter<String> adapter=
                    new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data);
            orderList.setAdapter(adapter);
            orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //當備案下時
                    String msg=data[position];
                    Toast.makeText(OrderListFinished.this,msg,Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderListFinished.this, OrderDetail.class);
                    String Id = order_Id[position];
                    static_order_id = Id;
                    startActivity(intent);
                }
            });
        }
        else{
            Toast.makeText(this, "無訂單", Toast.LENGTH_SHORT).show();
        }

    }

    private void getOrderList(String Showurl) throws MalformedURLException {
        try{
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Customer_Id), "UTF-8");
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
            try{
                JSONArray ja = new JSONArray(result);
                JSONObject jo = null;

                data = new String[ja.length()];
                order_Id = new String[ja.length()];

                for(int i = 0; i<ja.length();i++){
                    jo = ja.getJSONObject(i);
                    data[i] = jo.getString("DELIVERY_Phone");
                    jo = ja.getJSONObject(i);
                    data[i] = jo.getString("Order_weight");

                    Log.i("order data",data[i]);
                    order_Id[i] = jo.getString("ORDER_Id");
                }
            }catch(Exception e){
                Log.i("OrderList JSON Exception",e.toString());
            }
        } catch (Exception e) {
            Log.i("GetOrderList Exception", e.toString());
        }

        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);

        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);

        // Perform item selected listener
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//                switch(item.getItemId())
//                {
//                    case R.id.navigation_dashboard:
//                        startActivity(new Intent(getApplicationContext(),UserDashboard.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                    case R.id.navigation_home:
//                        startActivity(new Intent(getApplicationContext(),Homepage.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                    case R.id.navigation_notifications:
//                        startActivity(new Intent(getApplicationContext(),OrderListUnfinished.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                }
//                return false;
//            }
//        });
    }

    private void getData(String URL_Link){
        //還要把worker Id丟過去
        try {
            URL url = new URL(URL_Link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            is = new BufferedInputStream(con.getInputStream());
        }
        catch(Exception e){
            Log.i("OrderList Exception",e.toString());
        }
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            while((line = br.readLine())!=null){
                sb.append(line+"\n");
            }
            is.close();
            result = sb.toString();
            Log.i("OrderList result",result);
        }catch(Exception e){
            Log.i("OrderList Exception2",e.toString());
        }
        try{
            JSONArray ja = new JSONArray(result);
            JSONObject jo = null;

            data = new String[ja.length()];

            for(int i = 0; i<ja.length();i++){
                jo = ja.getJSONObject(i);
                data[i] = jo.getString("Order_Id");
                Log.i("order data",data[i]);
            }
        }catch(Exception e){
            data = null;
            Log.i("OrderList JSON Exception",e.toString());
        }
    }
}
