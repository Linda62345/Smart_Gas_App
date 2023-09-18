package com.example.smartgasapp.ui.login;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ForgetPassword1;
import com.example.smartgasapp.Homepage;
import com.example.smartgasapp.R;
import com.example.smartgasapp.Register;
import com.example.smartgasapp.TokenManager;
import com.example.smartgasapp.databinding.ActivityLoginBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import android.content.Context;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    private Button register;
    private Button forget;

    private String email, password;
    EditText username;
    EditText Password;
    public static int CUSTOMER_ID;
    public static int COMPANY_Id;
    public static String Customer_Name;
    private TokenManager tokenManager;
    private static final long SESSION_TIMEOUT_MILLISECONDS = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
    private static final String SESSION_TIMEOUT_KEY = "session_timeout";




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        long sessionStartTime = System.currentTimeMillis();
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(SESSION_TIMEOUT_KEY, sessionStartTime);
        editor.apply();


        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

//        SharedPreferences activityPref = getSharedPreferences("activity_data", Context.MODE_PRIVATE);
//        String currentActivityName = activityPref.getString("current_activity", "");
//
//        // Check if the current activity is not empty and is not the LoginActivity itself
//        if (!currentActivityName.isEmpty() && !currentActivityName.equals(LoginActivity.class.getName())) {
//            // Redirect to the previously visited activity
//            try {
//                Class<?> previousActivity = Class.forName(currentActivityName);
//                Intent intent = new Intent(this, previousActivity);
//                startActivity(intent);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }

        tokenManager = TokenManager.getInstance(this);

        // Check login status from SharedPreferences
        SharedPreferences sharedPref1 = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref1.getBoolean("isLoggedIn", false);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        register = findViewById(R.id.login_register_button);
        forget = findViewById(R.id.forget_button);

        //logIn
        email = password = "";
        username = findViewById(R.id.username);
        Password = findViewById(R.id.password);

        createNotificationChannel();

        // Retrieve email and password extras, if available
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String emailExtra = extras.getString("email");
            String passwordExtra = extras.getString("password");
            if (emailExtra != null && passwordExtra != null) {
                // Automatically fill the email and password fields
                username.setText(email);
                Password.setText(password);
            }
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Register.class);
                startActivity(intent);
            }
        });

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPassword1.class);
                startActivity(intent);
            }
        });


        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }

            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    saveLoginData(email, password);
                    //setLoggedInStatus(true);
                    login();
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                CustomerID();
                                //Your code goes here
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();

                }
                setResult(Activity.RESULT_OK);


            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        // Check if there is saved login data, if yes, log in automatically
        if (hasSavedLoginData()) {
            SharedPreferences sharedPref2 = getSharedPreferences("login_data", Context.MODE_PRIVATE);
            String savedEmail = sharedPref2.getString("email", "");
            String savedPassword = sharedPref1.getString("password", "");
            username.setText(savedEmail);
            Password.setText(savedPassword);
            loginViewModel.login(savedEmail, savedPassword);
        }

    }

    private boolean isSessionTimedOut() {
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        long sessionStartTime = sharedPref.getLong(SESSION_TIMEOUT_KEY, 0); // 0 means no session start time found
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - sessionStartTime;
        return elapsedTime >= SESSION_TIMEOUT_MILLISECONDS;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public void login() {
        String url = "http://54.199.33.241/test/customer_login.php";
        email = username.getText().toString().trim();
        password = Password.getText().toString().trim();
        if (!email.equals("") && !password.equals("")) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("res", response);
                    if (response.contains("success")) {
                        // Save user data to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", email); // Replace 'email' with the actual username
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        String accessToken = "smartGas7890"; // Extract the token from the response
                        long expirationTimestamp = 0; // Extract the expiration timestamp
                        tokenManager.setAccessToken(accessToken, expirationTimestamp);
                        Log.d("TokenUsage", "Access token set successfully: " + accessToken);
                       // saveAccessToken(accessToken);

                        Intent intent = new Intent(LoginActivity.this, Homepage.class);
                        //要把email傳過去
                        intent.putExtra("email", email);
                        saveLoginData(email, password);
                        startActivity(intent);
                        finish();
                    } else if (response.contains("failure")) {
                        Toast.makeText(LoginActivity.this, "帳號或密碼有誤", Toast.LENGTH_SHORT).show();
                    }
                    if (isSessionTimedOut()) {
                        // Session has timed out, prompt the user to log in again or perform necessary actions
                        Toast.makeText(LoginActivity.this, "Session has timed out. Please log in again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        // Bad request error
                        Toast.makeText(LoginActivity.this, "Invalid request. Please check your input.", Toast.LENGTH_SHORT).show();
                    } else if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        // Unauthorized error
                        Toast.makeText(LoginActivity.this, "Invalid login credentials.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Other error
                        Toast.makeText(LoginActivity.this, "Invalid login input", Toast.LENGTH_SHORT).show();
                    }
                    Log.i("login error", error.toString());
                }
            })

            {
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("email", email);
                    data.put("password", password); //php,本地端
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        } else {
            Toast.makeText(this, "帳號密碼欄位必填", Toast.LENGTH_SHORT).show();
        }

    }

    private void logoutAndRedirectToLogin() {
        // Clear session data
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(SESSION_TIMEOUT_KEY);
        editor.remove("email"); // Clear saved email
        editor.remove("password"); // Clear saved password
        editor.apply();

        // Redirect to the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }


    private void saveAccessToken(String accessToken) throws GeneralSecurityException, IOException {
        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "my_secure_prefs",
                masterKeyAlias,  // Your master key alias
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
    }

    public void CustomerID() {
        if (email == null) {
            Log.i("null Customer Id", String.valueOf(CUSTOMER_ID));
        } else {
            try {
                email = username.getText().toString().trim();
                String Findurl = "http://54.199.33.241/test/Find_Customer_ID.php";
                URL url = new URL(Findurl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
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
                Log.i("result", "[" + result + "]");
                JSONObject responseJSON = new JSONObject(result);
                CUSTOMER_ID = responseJSON.getInt("CUSTOMER_Id");
                Customer_Name = responseJSON.getString("CUSTOMER_Name");
                Log.i("Customer_ID", String.valueOf(CUSTOMER_ID));
                COMPANY_Id = responseJSON.getInt("COMPANY_Id");
                Log.i("Company_id",String.valueOf(COMPANY_Id));
            } catch (Exception e) {
                if (e.toString().contains("Failed to connect to")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "請連接網路", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                Log.i("Here Exception", e.toString());
            }
        }
    }

    private void saveLoginData(String email, String password) {
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    private boolean hasSavedLoginData() {
        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
        return sharedPref.contains("email") && sharedPref.contains("password");
    }

//    private static void setLoggedInStatus(boolean isLoggedIn) {
//        SharedPreferences sharedPref = getSharedPreferences("login_data", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean("isLoggedIn", isLoggedIn);
//        editor.apply();
//    }




    public static int getCustomerID() {
        int customer_id = CUSTOMER_ID;
        return customer_id;
    }


}
