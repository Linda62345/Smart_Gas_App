package com.example.smartgasapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

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
import java.util.Objects;

public class PersonalBarcode extends AppCompatActivity {

    private Button backToHome;

    public int CUSTOMER_ID;
    public String CUSTOMER_Name, CUSTOMER_Sex;
    private ImageView imageView;
    private TextView userName,sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_barcode);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView=findViewById(R.id.nav_view);
        backToHome = findViewById(R.id.barcodeBackToHome);
        userName = findViewById(R.id.Changable_UserName_View);
        sex = findViewById(R.id.userGenderDescView);

        LoginActivity loginActivity = new LoginActivity();
        CUSTOMER_ID = loginActivity.getCustomerID();
        Log.i("barcode", String.valueOf(CUSTOMER_ID));

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


        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(PersonalBarcode.this, Homepage.class);
                startActivity(intent);
            }
        });

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

        imageView= findViewById(R.id.changableBarcode);
        userName= findViewById(R.id.Changable_UserName_View);
        sex = findViewById(R.id.userGenderDescView);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            String customerIdString = String.valueOf(CUSTOMER_ID);
            int size = 500; // Adjust this size as needed
            BitMatrix bitMatrix = multiFormatWriter.encode(customerIdString, BarcodeFormat.QR_CODE, size, size);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
//            BitMatrix bitMatrix = multiFormatWriter.encode(String.valueOf(CUSTOMER_ID), BarcodeFormat.CODE_128, imageView.getWidth(),imageView.getHeight() );
//            Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(),imageView.getHeight(), Bitmap.Config.RGB_565);
//            for(int i = 0;i<imageView.getWidth();i++){
//                for (int j = 0;j< imageView.getHeight();j++){
//                    bitmap.setPixel(i,j,bitMatrix.get(i,j)? Color.BLACK:Color.WHITE);
                }
            }

            imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
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
            userName.setText(CUSTOMER_Name);
            CUSTOMER_ID = responseJSON.getInt("CUSTOMER_Id");
            CUSTOMER_Sex = responseJSON.getString("CUSTOMER_Sex");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userName.setText(CUSTOMER_Name);
                    if (CUSTOMER_Sex.equals("1")) {
                        sex.setText("小姐");
                    } else {
                        sex.setText("先生");
                    }
                }
            });

            Log.i("CUSTOMER_Name", CUSTOMER_Name);
        } catch (Exception e) {
            Log.i("Edit Exception", e.toString());
        }
    }

//    public void barcodeButtonClick(View view){
//        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
//
//        try {
//            String customerIdString = String.valueOf(CUSTOMER_ID);
//            BitMatrix bitMatrix = multiFormatWriter.encode(customerIdString, BarcodeFormat.QR_CODE, imageView.getWidth(), imageView.getHeight());
//            int width = bitMatrix.getWidth();
//            int height = bitMatrix.getHeight();
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
////            BitMatrix bitMatrix = multiFormatWriter.encode(String.valueOf(CUSTOMER_ID), BarcodeFormat.CODE_128, imageView.getWidth(),imageView.getHeight() );
////            Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(),imageView.getHeight(), Bitmap.Config.RGB_565);
////            for(int i = 0;i<imageView.getWidth();i++){
////                for (int j = 0;j< imageView.getHeight();j++){
////                    bitmap.setPixel(i,j,bitMatrix.get(i,j)? Color.BLACK:Color.WHITE);
//                }
//            }
//
//            imageView.setImageBitmap(bitmap);
//
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
//
//    }
}