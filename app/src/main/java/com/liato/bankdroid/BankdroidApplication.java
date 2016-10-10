package com.liato.bankdroid;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class BankdroidApplication extends Application {

    public static final String LOG_KEY_LOCALE = "locale";

    public static final String LOG_KEY_BANKS = "banks";

    private String message = "";

    @Override
    public void onCreate() {
        super.onCreate();

        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.plant(new CrashlyticsTree());
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

    public class CrashlyticsTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

            if (t == null) {
                Crashlytics.logException(new Exception(message));
            } else {
                Crashlytics.logException(t);
            }
        }
    }
}
