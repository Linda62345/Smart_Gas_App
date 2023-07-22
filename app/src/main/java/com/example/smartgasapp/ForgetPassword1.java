package com.example.smartgasapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ForgetPassword1 extends AppCompatActivity {

    private Button newPass;
    public EditText email,number;
    private String URL = "http://54.199.33.241/test/index.php";
    public String verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password1);

        email = findViewById(R.id.enterPhone_ForgetPass);
        newPass = findViewById(R.id.setNewPassButton);
        number = findViewById(R.id.enterVerivication);

        newPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(newPass.getText().toString().trim().contains("發送")){
                        checkAccount();
                    }
                    else{
                        String Input_number = number.getText().toString().trim();
                        if(verify.equals(Input_number)){
                            Intent intent = new Intent(ForgetPassword1.this, ForgetPassword2.class);
                            intent.putExtra("email", email.getText().toString());
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(ForgetPassword1.this, "驗證碼錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                catch (Exception e){
                    Log.i("forgetpassword",e.toString());
                }
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
    private void checkAccount(){
        String String_email = email.getText().toString().trim();
        if(String_email==""){
            Toast.makeText(this, "請輸入電子郵件", Toast.LENGTH_SHORT).show();
        }
        else{
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("res", response);
                    if (response.contains("success")) {
                        verify = response.substring(7,response.length());
                        newPass.setText("確認驗證碼");
                        Log.i("response",response);
                    } else if (response.contains("failure")) {
                        Toast.makeText(ForgetPassword1.this, "此電子郵件尚未註冊 或是 電子郵件有錯誤", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    email.setText(error.toString().trim());
                    Toast.makeText(ForgetPassword1.this, error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }) {
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    Log.i("email",String_email);
                    data.put("email", String_email); //php,本地端
                    return data;
                }
            };
            //NetworkUtility.log Slow Requests: HTTP response for request=<[ ]
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }

    }
}