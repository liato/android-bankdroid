package com.liato.bankdroid;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankFactory;

import android.app.Application;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class BankdroidApplication extends Application {

    public static final String LOG_KEY_LOCALE = "locale";

    public static final String LOG_KEY_BANKS = "banks";

    private String message = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());
        Crashlytics.setString(LOG_KEY_LOCALE, Locale.getDefault().toString());
        logBanks();
    }

    private void logBanks() {
        List<Bank> banks = BankFactory.banksFromDb(this, false);
        StringBuilder bankStringBuilder = new StringBuilder();
        if (banks != null && !banks.isEmpty()) {
            for (Bank bank : banks) {
                bankStringBuilder.append(bank.getName())
                        .append(",");
            }
            Crashlytics.setString(LOG_KEY_BANKS,
                    bankStringBuilder.substring(0, bankStringBuilder.length() - 1));
        }
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
