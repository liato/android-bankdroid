package com.liato.bankdroid;

import com.liato.bankdroid.utils.LoggingUtils;

import android.app.Application;
import android.widget.Toast;

public class BankdroidApplication extends Application {

    private String message = "";

    @Override
    public void onCreate() {
        super.onCreate();
        LoggingUtils.createLogger(this);
    }

    public void setApplicationMessage(String messageText) {
        message = messageText == null ? "" : messageText;
    }

    public void showAndDeleteApplicationMessage() {
        if (!message.isEmpty()) {
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            message = "";
            toast.show();
        }
    }
}
