package com.example.smartgasapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;


public class ScanIotQRCode extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView previewView;
    private EditText enterNewIot;
    public String Customer_ID, Sensor_ID, result;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button qrCodeFoundButton;
    private String qrCode;
    private DecoratedBarcodeView barcodeView;
    String[] data;
    public JSONObject responseJSON;
    public JSONArray ja;
    public ListView IOTlistView;
    ArrayList<CustomerOrderDetail> customerOrderDetails;

    //public static ArrayList<CustomerOrderDetail> customerOrderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_iot_qrcode);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        barcodeView = findViewById(R.id.receiptScanner);
        barcodeView.decodeContinuous(callback);
        enterNewIot = findViewById(R.id.mannuallyEnterReceiptCode);


        enterNewIot.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                Sensor_ID = enterNewIot.getText().toString().trim();
                Log.i("sensor_id", Sensor_ID);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
        });

//        IOTlistView = findViewById(R.id.IOTlist);

        LoginActivity loginActivity = new LoginActivity();
        Customer_ID = String.valueOf(loginActivity.getCustomerID());

        customerOrderDetails = new ArrayList<CustomerOrderDetail>();
        CustomerOrderDetail od = new CustomerOrderDetail("編號", "重量", "電量");
        customerOrderDetails.add(od);

        qrCodeFoundButton = findViewById(R.id.qrCodeFoundButton);
        qrCodeFoundButton.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }

        qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIOT();
//                startQRCodeScanner();
//                IntentIntegrator integrator;
//                integrator = new IntentIntegrator(ScanIotQRCode.this);
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE); // You can customize the barcode formats here
//                integrator.setPrompt("Scan a QR Code");
//                integrator.setCameraId(0); // Rear camera
//                integrator.initiateScan();

                // enterNewIot.setText(""); // Clear the EditText
//                enterNewIot.setText(qrCode);
//                Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();
//                Log.i(ScanIotQRCode.class.getSimpleName(), "QR Code Found: " + qrCode);
            }
        });

        //ShowDataDetail();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // bindCameraPreview(cameraProvider);
            } catch (Exception e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
        requestCamera();

    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {


            String qrCodeText = result.getText();
            if (qrCodeText != null && !qrCodeText.isEmpty() && qrCodeText.length() == 15 ) {
                enterNewIot.setText(qrCodeText);
                Log.i("Scanned QR Code", qrCodeText);
            } else {
                Log.i("Scanned QR Code length invallid ", qrCodeText);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Handle possible result points
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR code");
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    public void saveIOT() {
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

    public void showIOT(String Showurl) {
        try {
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
            Log.i("iot result", "[" + result + "]");

            if (result.contains("\"\"response\":\"0\"")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanIotQRCode.this, "無IoT連結", Toast.LENGTH_SHORT).show();
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
                ActivityCompat.requestPermissions(ScanIotQRCode.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
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
                // bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

//    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
//        barcodeView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);
//
//        Preview preview = new Preview.Builder()
//                .build();
//
//        CameraSelector cameraSelector = new CameraSelector.Builder()
//                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                .build();
//
//        preview.setSurfaceProvider(barcodeView.createSurfaceProvider());
//
//        ImageAnalysis imageAnalysis =
//                new ImageAnalysis.Builder()
//                        .setTargetResolution(new Size(1280, 720))
//                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .build();
//
////        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
////            @Override
////            public void onQRCodeFound(String _qrCode) {
////                qrCode = _qrCode;
////                qrCodeFoundButton.setVisibility(View.VISIBLE);
////            }
////
////            @Override
////            public void qrCodeNotFound() {
////                qrCodeFoundButton.setVisibility(View.INVISIBLE);
////            }
////        }));
//
//        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
//            @Override
//            public void onQRCodeFound(String _qrCode) {
//                qrCode = _qrCode;
//                if (qrCode.length() == 15) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            enterNewIot.setText(qrCode);
//                        }
//                    });
//                    Log.i(ScanIotQRCode.class.getSimpleName(), "QR Code Found: " + qrCode);
//                } else {
//                    Log.i(ScanIotQRCode.class.getSimpleName(), "QR Code ID Length Incorrect");
//                }
//            }
//
//            @Override
//            public void qrCodeNotFound() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i(ScanIotQRCode.class.getSimpleName(), "QR Not Found");
////                        enterNewIot.setText(""); // Clear the EditText when QR code is not found
//                    }
//                });
//            }
//        }));
//        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null) {
//            if (result.getContents() == null) {
//                // Handle no result
//            } else {
//                String scannedData = result.getContents(); // The scanned barcode data
//                // Process the scanned data
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the request code matches the ZXing scanner's request code
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                String scannedData = result.getContents(); // The scanned QR code data

                // Update the EditText with the scanned QR code data
                enterNewIot.setText(scannedData);
                Log.i("Scanned QR Code", scannedData);
            }
        }
    }

}
