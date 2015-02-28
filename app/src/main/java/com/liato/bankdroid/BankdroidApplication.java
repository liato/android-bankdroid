package com.liato.bankdroid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class BankdroidApplication extends Application {

    public static final String LOG_KEY_LOCALE = "locale";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Crashlytics.setString(LOG_KEY_LOCALE, Locale.getDefault().toString());
    }
}
