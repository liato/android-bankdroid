package com.liato.bankdroid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankFactory;

import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class BankdroidApplication extends Application {

    public static final String LOG_KEY_LOCALE = "locale";
    public static final String LOG_KEY_BANKS = "banks";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
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
            Crashlytics.setString(LOG_KEY_BANKS, bankStringBuilder.substring(0, bankStringBuilder.length() - 1));
        }
    }
}
