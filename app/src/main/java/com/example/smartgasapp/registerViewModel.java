package com.example.smartgasapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import com.example.smartgasapp.data.LoginRepository;
import com.example.smartgasapp.data.Result;
import com.example.smartgasapp.data.model.LoggedInUser;
import com.example.smartgasapp.R;
import com.example.smartgasapp.ui.login.LoggedInUserView;
import com.example.smartgasapp.ui.login.LoginFormState;
import com.example.smartgasapp.ui.login.LoginResult;

public class registerViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    registerViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

//    public void login(String username, String password) {
//        // can be launched in a separate asynchronous job
//        Result<LoggedInUser> result = loginRepository.login(username, password);
//
//        if (result instanceof Result.Success) {
//            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
//            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
//        } else {
//            loginResult.setValue(new LoginResult(R.string.login_failed));
//        }
//    }

    public void loginDataChanged(String register_pass_con_input, String register_pass_input) {
        if (!isPasswordValid(register_pass_input)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else if (!isPasswordValid(register_pass_con_input)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
//    private boolean isUserNameValid(String username) {
//        if (username == null) {
//            return false;
//        }
//        if (username.contains("@")) {
//            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
//        } else {
//            return !username.trim().isEmpty();
//        }
//    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
