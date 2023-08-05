package com.example.smartgasapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.Result;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ScanReceiptQRCode extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView previewView;
    private EditText enterNewIot;
    public String Customer_ID, Sensor_ID,result;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button qrCodeFoundButton;
    private String qrCode;
    String[] data;
    public JSONObject responseJSON;
    public JSONArray ja;
    public ListView IOTlistView;
    ArrayList<CustomerOrderDetail> customerOrderDetails;

    //public static ArrayList<CustomerOrderDetail> customerOrderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_receipt_qrcode);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        previewView = findViewById(R.id.receiptScanner);
        enterNewIot= findViewById(R.id.mannuallyEnterReceiptCode);

        enterNewIot.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                Sensor_ID = enterNewIot.getText().toString().trim();
                Log.i ("sensor_id", Sensor_ID);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }});

//        IOTlistView = findViewById(R.id.IOTlist);

        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());

        customerOrderDetails = new ArrayList<CustomerOrderDetail>();
        CustomerOrderDetail od = new CustomerOrderDetail("編號","重量","電量");
        customerOrderDetails.add(od);

        qrCodeFoundButton = findViewById(R.id.qrCodeFoundButton);
        qrCodeFoundButton.setVisibility(View.VISIBLE);

        qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIOT();
               // enterNewIot.setText(""); // Clear the EditText
//                enterNewIot.setText(qrCode);
//                Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();
//                Log.i(ScanReceiptQRCode.class.getSimpleName(), "QR Code Found: " + qrCode);
            }
        });

        //ShowDataDetail();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();

    }

//    public void ShowDataDetail(){
//        Thread thread = new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("iot here","iot here");
//                        showIOT("http://54.199.33.241/SQL_Connect/Show_IOT.php");
//                        //這裡要做修正
//                        if (result.contains("\"\"response\":\"0\"")) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(ScanReceiptQRCode.this, "無IoT連結", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                        try{
//                            ja = new JSONArray(result);
//                            if(ja!=null){
//                                JSONObject jo = null;
//                                data = new String[ja.length()];
//                                customerOrderDetails = new ArrayList<CustomerOrderDetail>();
//                                CustomerOrderDetail od = new CustomerOrderDetail("編號","重量","電量");
//                                customerOrderDetails.add(od);
//
//                                for(int i = 0;i<ja.length();i++){
//                                    jo = ja.getJSONObject(i);
//                                    CustomerOrderDetail od1 = new CustomerOrderDetail(jo.getString("SENSOR_Id"),jo.getString("SENSOR_Weight"),jo.getString("SENSOR_Battery"));
//                                    customerOrderDetails.add(od1);
//                                }
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        CustomerOrderDetailAdapterList adapter = new CustomerOrderDetailAdapterList (getApplicationContext(), R.layout.adapter_view_layout, customerOrderDetails);
//                                        Log.i("order detail", String.valueOf(customerOrderDetails.size()));
//                                        IOTlistView.setAdapter(null);
//                                        IOTlistView.setAdapter(adapter);
//                                    }
//                                });
//                            }
//                        }
//                        catch (Exception e){
//                            Log.i("ListView iot exception",e.toString());
//                        }
//                    }
//                }
//        );
//        thread.start();
//    }

    public void saveIOT(){
        try {
            String URL = "http://54.199.33.241/SQL_Connect/Save_IOT.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("iot response", response);
                    if (response.contains("success")) {
                        Toast.makeText(getApplicationContext(), "IOT新增成功", Toast.LENGTH_LONG).show();
                        enterNewIot.setText("");
                        showIOT("http://54.199.33.241/SQL_Connect/Show_IOT.php");
                    } else if (response.contains("Duplicate")) {
                        Toast.makeText(getApplicationContext(), "新增失敗，此IOT已在資料庫中", Toast.LENGTH_LONG).show();

                    } else if (response.contains("failure")) {
                        Toast.makeText(getApplicationContext(), "新增失敗", Toast.LENGTH_LONG).show();
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
                    data.put("sensor_id", Sensor_ID);
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            Log.i("save iot Exception", e.toString());
        }
    }
    public void showIOT(String Showurl){
        try{
            URL url = new URL(Showurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Customer_ID, "UTF-8");
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
            Log.i("iot result", "["+result+"]");

            if (result.contains("\"\"response\":\"0\"")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanReceiptQRCode.this, "無IoT連結", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            try {
                ja = new JSONArray(result);
                if (ja != null) {
                    JSONObject jo = null;
                    data = new String[ja.length()];
                    customerOrderDetails.clear(); // Clear the existing list

                    CustomerOrderDetail od = new CustomerOrderDetail("編號", "重量", "電量");
                    customerOrderDetails.add(od);

                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);
                        CustomerOrderDetail od1 = new CustomerOrderDetail(jo.getString("SENSOR_Id"), jo.getString("SENSOR_Weight"), jo.getString("SENSOR_Battery"));
                        customerOrderDetails.add(od1);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CustomerOrderDetailAdapterList adapter = new CustomerOrderDetailAdapterList(getApplicationContext(), R.layout.adapter_view_layout, customerOrderDetails);
                            Log.i("order detail", String.valueOf(customerOrderDetails.size()));
                            IOTlistView.setAdapter(adapter);
                        }
                    });
                }
            } catch (Exception e) {
                Log.i("ListView iot exception", e.toString());
            }
        } catch (Exception e) {
            Log.i("show iot Exception", e.toString());
        }
//       }
//        catch (Exception e){
//            Log.i("show iot Exception",e.toString());
//        }
    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(ScanReceiptQRCode.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

//        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
//            @Override
//            public void onQRCodeFound(String _qrCode) {
//                qrCode = _qrCode;
//                qrCodeFoundButton.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void qrCodeNotFound() {
//                qrCodeFoundButton.setVisibility(View.INVISIBLE);
//            }
//        }));

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {
                qrCode = _qrCode;
                if (qrCode.length() == 15) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            enterNewIot.setText(qrCode);
                        }
                    });
                    Log.i(ScanReceiptQRCode.class.getSimpleName(), "QR Code Found: " + qrCode);
                }
                else {
                    Log.i(ScanReceiptQRCode.class.getSimpleName(), "QR Code ID Length Incorrect");
                }
            }

            @Override
            public void qrCodeNotFound() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(ScanReceiptQRCode.class.getSimpleName(), "QR Not Found");
//                        enterNewIot.setText(""); // Clear the EditText when QR code is not found
                    }
                });
            }
        }));
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
}