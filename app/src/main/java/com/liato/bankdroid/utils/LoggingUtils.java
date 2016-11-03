package com.liato.bankdroid.utils;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.liato.bankdroid.BuildConfig;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class LoggingUtils {

    private static final boolean IS_CRASHLYTICS_ENABLED = isCrashlyticsEnabled();
    private static final String DEFAULT_TAG = "Bankdroid";

    private static Class<Timber> initializedLoggingClass = null;

    private LoggingUtils() {
    }

    public static void createLogger(Context context) {
       Timber.Tree tree =  IS_CRASHLYTICS_ENABLED ?
               new CrashlyticsTree(context) :
               new LocalTree();

        if (initializedLoggingClass != Timber.class) {
            initializedLoggingClass = Timber.class;
            Timber.plant(tree);
            Timber.v("Logging tree planted: %s", tree.getClass());
        }
    }

    private static boolean isCrashlyticsEnabled() {
        return EmulatorUtils.RUNNING_ON_ANDROID &&
                !EmulatorUtils.RUNNING_ON_EMULATOR;
    }

    public static void logCustom(CustomEvent event) {
        if (!isCrashlyticsEnabled()) {
            return;
        }

        event.putCustomAttribute("App Version", BuildConfig.VERSION_NAME);
        Answers.getInstance().logCustom(event);
    }

    public static void logDisabledBank(Bank bank) {
        if (!isCrashlyticsEnabled()) {
            return;
        }

        logCustom(new CustomEvent("Disabled Bank").
                putCustomAttribute("Name", bank.getName()));
    }

    public static void logBankUpdate(Bank bank, boolean withTransactions) {
        if (!isCrashlyticsEnabled()) {
            return;
        }

        logCustom(new CustomEvent("Bank Updated").
                putCustomAttribute("Name", bank.getName()).
                putCustomAttribute("With Transactions", Boolean.toString(withTransactions)));

        boolean hasTransactions = false;
        for (Account account : bank.getAccounts()) {
            if (account.getTransactions() != null && !account.getTransactions().isEmpty()) {
                hasTransactions = true;
            }
        }
        if (withTransactions && !hasTransactions) {
            logCustom(new CustomEvent("Bank Without Transactions").
                    putCustomAttribute("Name", bank.getName()));
        }
    }

    private static class CrashlyticsTree extends Timber.Tree {
        CrashlyticsTree(Context context) {
            Fabric.with(context, new Crashlytics());
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (BuildConfig.DEBUG) {
                tag = "DEBUG";
            } else if (TextUtils.isEmpty(tag)) {
                tag = DEFAULT_TAG;
            }

            // This call logs to *both* Crashlytics and LogCat, and will log the Exception backtrace
            // to LogCat on exceptions.
            Crashlytics.log(priority, tag, message);

            if (t != null) {
                Crashlytics.logException(t);
            }
        }
    }

    private static class LocalTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (BuildConfig.DEBUG) {
                tag = "DEBUG";
            } else if (TextUtils.isEmpty(tag)) {
                tag = DEFAULT_TAG;
            }

            // Empirical evidence shows any exception stack trace is already part of the message, so
            // no need to print the exception explicitly here.
            Log.println(priority, tag, message);
        }
    }
}
