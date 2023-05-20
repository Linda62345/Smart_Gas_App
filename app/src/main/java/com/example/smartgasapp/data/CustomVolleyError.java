package com.example.smartgasapp.data;

import com.android.volley.VolleyError;

public class CustomVolleyError extends VolleyError {
    private String customMessage;

    public CustomVolleyError(String customMessage) {
        this.customMessage = customMessage;
    }

    @Override
    public String getMessage() {
        return customMessage;
    }
}
