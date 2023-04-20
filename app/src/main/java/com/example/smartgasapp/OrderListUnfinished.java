package com.example.smartgasapp;

import android.content.Intent;
import android.os.Bundle;
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

public class OrderListUnfinished extends AppCompatActivity {

    private Button finished, order;
    public String Customer_Id;
    private ListView orderList;
    private String[] data,order_Id;
    public static String static_order_id;
    InputStream is = null;
    String line,result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list_unfinished);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        finished = findViewById(R.id.order_finished);
        order = findViewById(R.id.enterSearch);

       order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderListUnfinished.this, OrderListFinished.class);
                startActivity(intent);
            }
        });

        LoginActivity loginActivity = new LoginActivity();
        Customer_Id = String.valueOf(loginActivity.getCustomerID());
        Log.i("Unfinish: ",Customer_Id);

        orderList = (ListView)findViewById(R.id.list_item);
        getData();
        try {
            getOrderList();
        } catch (Exception e) {
            Log.i("OrderList create Exception",e.toString());
        }
        setAdapter();

        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //當備案下時
                String msg=data[position];
                Toast.makeText(OrderListUnfinished.this,msg,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OrderListUnfinished.this, OrderDetail.class);
                String Id = order_Id[position];
                static_order_id = Id;
                //intent.putExtra("order_Id", Id);
                startActivity(intent);
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
                    Toast.makeText(OrderListUnfinished.this,msg,Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderListUnfinished.this, OrderDetail.class);
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

    private void getOrderList() throws MalformedURLException {
        try{
            String Showurl = "http://10.0.2.2/SQL_Connect/Customer_UnOrderList.php";
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

    private void getData(){
        //還要把customer Id丟過去
        try {
            String dataurl = "http://10.0.2.2/SQL_Connect/Customer_UnOrderList.php";
            URL url = new URL(dataurl);
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
            Log.i("UnOrderList result",result);
        }catch(Exception e){
            Log.i("UnOrderList Exception2",e.toString());
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
            Log.i("UnOrderList JSON Exception",e.toString());
        }
    }

}
