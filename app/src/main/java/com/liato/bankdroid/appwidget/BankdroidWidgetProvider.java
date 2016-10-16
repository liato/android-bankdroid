/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liato.bankdroid.appwidget;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.MainActivity;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankFactory;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.db.DBAdapter;
import com.liato.bankdroid.utils.NetworkUtils;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;

import timber.log.Timber;

public abstract class BankdroidWidgetProvider extends AppWidgetProvider {

    private final static String TAG = "BankdroidWidgetProvider";

    private final static String ACTION_WIDGET_BLUR = "com.liato.bankdroid.action.WIDGET_BLUR";

    private final static String ACTION_WIDGET_UNBLUR = "com.liato.bankdroid.action.WIDGET_UNBLUR";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Account account) {
        RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId, account);
        if (views != null) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
        if (views != null) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    static void unblurAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
        Editor e = prefs.edit();
        e.putBoolean("widget_unblurred_" + appWidgetId, true);
        e.apply();

        RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
        if (views != null) {
            views.setViewVisibility(R.id.imgBalanceblur, View.GONE);
            views.setViewVisibility(R.id.txtWidgetAccountnameBlur, View.GONE);
            views.setViewVisibility(R.id.txtWidgetAccountbalance, View.VISIBLE);
            views.setViewVisibility(R.id.txtWidgetAccountname, View.VISIBLE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    static void blurAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
        Editor e = prefs.edit();
        e.remove("widget_unblurred_" + appWidgetId);
        e.apply();
        RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
        if (views != null) {
            views.setViewVisibility(R.id.imgBalanceblur, View.VISIBLE);
            views.setViewVisibility(R.id.txtWidgetAccountnameBlur, View.VISIBLE);
            views.setViewVisibility(R.id.txtWidgetAccountbalance, View.GONE);
            views.setViewVisibility(R.id.txtWidgetAccountname, View.GONE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    static RemoteViews buildAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        String accountId = WidgetConfigureActivity.getAccountId(context, appWidgetId);
        long bankId = WidgetConfigureActivity.getBankId(context, appWidgetId);
        if (accountId == null) {
            Timber.w("Widget not found. ID: %s", appWidgetId);
            return disableAppWidget(context, appWidgetManager,
                    appWidgetId);
        }
        Account account = BankFactory.accountFromDb(context, bankId + "_" + accountId, false);
        if (account == null) {
            Timber.w("Account not found in database");
            return disableAppWidget(context, appWidgetManager,
                    appWidgetId);

        }

        Bank bank = BankFactory.bankFromDb(account.getBankDbId(), context, false);
        if (bank == null) {
            Timber.w("Bank not found in database");
            return disableAppWidget(context, appWidgetManager,
                    appWidgetId);
        }

        account.setBank(bank);
        return buildAppWidget(context, appWidgetManager,
                appWidgetId, account);
    }


    static RemoteViews buildAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Account account) {
        AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int layoutId = (providerInfo == null) ? R.layout.widget : providerInfo.initialLayout;
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
        SharedPreferences defprefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("transperant_background" + appWidgetId, false) && (providerInfo
                != null)) {
            if (providerInfo.initialLayout == R.layout.widget_large) {
                layoutId = R.layout.widget_large_transparent;
            } else {
                layoutId = R.layout.widget_transparent;
            }
        }
        Bank bank = account.getBank();
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        views.setTextViewText(R.id.txtWidgetAccountname, account.getName().toUpperCase());
        views.setTextViewText(R.id.txtWidgetAccountnameBlur, account.getName().toUpperCase());
        views.setTextViewText(R.id.txtWidgetAccountbalance,
                Helpers.formatBalance(account.getBalance(), account.getCurrency(),
                        defprefs.getBoolean("round_widget_balance", false),
                        bank.getDecimalFormatter(),
                        bank.isDisabled()));
        views.setImageViewResource(R.id.imgWidgetIcon, bank.getImageResource());
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent;

        //intent = new Intent(context, AccountsActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.imgWarning, pendingIntent);

        intent = new Intent(context, WidgetService.class);
        intent.setAction(AutoRefreshService.BROADCAST_WIDGET_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(
                Uri.parse("rofl://copter/" + appWidgetId + "/" + System.currentTimeMillis()));
        pendingIntent = PendingIntent
                .getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imgWidgetIcon, pendingIntent);
        views.setOnClickPendingIntent(R.id.hitBox, pendingIntent);

        intent = new Intent(context, WidgetService.class);
        intent.setAction(BankdroidWidgetProvider.ACTION_WIDGET_UNBLUR);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(
                "rofl://copter/widgetunblur/" + appWidgetId + "/" + System.currentTimeMillis()));
        pendingIntent = PendingIntent
                .getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.imgBalanceblur, pendingIntent);

        if (defprefs.getBoolean("widget_blur_balance", false) && !prefs
                .getBoolean("widget_unblurred_" + appWidgetId, false)) {
            views.setViewVisibility(R.id.imgBalanceblur, View.VISIBLE);
            views.setViewVisibility(R.id.txtWidgetAccountnameBlur, View.VISIBLE);
            views.setViewVisibility(R.id.txtWidgetAccountbalance, View.GONE);
            views.setViewVisibility(R.id.txtWidgetAccountname, View.GONE);
            views.setOnClickPendingIntent(R.id.layWidgetContainer, pendingIntent);
        } else {
            views.setViewVisibility(R.id.imgBalanceblur, View.GONE);
            views.setViewVisibility(R.id.txtWidgetAccountnameBlur, View.GONE);
            views.setViewVisibility(R.id.txtWidgetAccountbalance, View.VISIBLE);
            views.setViewVisibility(R.id.txtWidgetAccountname, View.VISIBLE);

            intent = new Intent(context, MainActivity.class);
            intent.setAction(AutoRefreshService.ACTION_MAIN_SHOW_TRANSACTIONS);
            intent.setData(Uri.parse("rofl://copter/showtransactions/" + appWidgetId + "/" + System
                    .currentTimeMillis()));
            intent.putExtra("bank", bank.getDbId());
            intent.putExtra("account", account.getId());
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.txtWidgetAccountbalance, pendingIntent);
            views.setOnClickPendingIntent(R.id.layWidgetContainer, pendingIntent);
        }

        return views;
    }


    static RemoteViews disableAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int layoutId = (providerInfo == null) ? R.layout.widget : providerInfo.initialLayout;
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
        if (prefs.getBoolean("transperant_background" + appWidgetId, false) && (providerInfo
                != null)) {
            if (providerInfo.initialLayout == R.layout.widget_large) {
                layoutId = R.layout.widget_large_transparent;
            } else {
                layoutId = R.layout.widget_transparent;
            }
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        views.setTextViewText(R.id.txtWidgetAccountname, "");
        views.setTextViewText(R.id.txtWidgetAccountbalance, "ERROR");
        views.setImageViewResource(R.id.imgWidgetIcon, R.drawable.icon_large);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.txtWidgetAccountbalance, pendingIntent);
        views.setOnClickPendingIntent(R.id.layWidgetContainer, pendingIntent);

        return views;
    }


    public void onReceive(Context context, Intent intent) {
        // v1.5 fix that doesn't call onDelete Action
        final String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                this.onDeleted(context, new int[]{appWidgetId});
            }
        } else {
            super.onReceive(context, intent);
        }

        if (action.equals(AutoRefreshService.BROADCAST_WIDGET_REFRESH) || action
                .equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWM = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWM.getAppWidgetIds(intent.getComponent());
            final int N = appWidgetIds.length;
            for (int i = 0; i < N; i++) {
                int appWidgetId = appWidgetIds[i];
                updateAppWidget(context, appWM, appWidgetId);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            WidgetConfigureActivity.delAccountId(context, appWidgetIds[i]);
        }
    }

    public static class WidgetService extends Service {

        @Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);
            handleStart(intent, startId);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            handleStart(intent, startId);
            return START_NOT_STICKY;
        }

        public void handleStart(Intent intent, int startId) {
            if (intent == null) {
                return;
            }
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            Context context = getApplicationContext();
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(AutoRefreshService.BROADCAST_WIDGET_REFRESH)) {
                new WidgetUpdateTask(context, AppWidgetManager.getInstance(context), appWidgetId)
                        .execute();
            } else if (action.equals(BankdroidWidgetProvider.ACTION_WIDGET_UNBLUR)) {
                unblurAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);

                Handler blurHandler = new Handler();
                class BlurRunnable implements Runnable {

                    private int mAppWidgetId;

                    public BlurRunnable(int appWidgetId) {
                        this.mAppWidgetId = appWidgetId;
                    }

                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        blurAppWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
                    }

                }

                SharedPreferences defprefs = PreferenceManager.getDefaultSharedPreferences(context);
                Integer unblurTimeout = 1000 * Integer
                        .parseInt(defprefs.getString("widget_blur_balance_timeout", "5"));
                blurHandler.postDelayed(new BlurRunnable(appWidgetId), unblurTimeout);
            } else if (action.equals(BankdroidWidgetProvider.ACTION_WIDGET_BLUR)) {
                blurAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        }

        @Override
        public IBinder onBind(Intent arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        protected class WidgetUpdateTask extends AsyncTask<Void, Void, Void> {

            private Context context;

            private AppWidgetManager appWidgetManager;

            private int appWidgetId;

            private SharedPreferences prefs;

            public WidgetUpdateTask(Context context, AppWidgetManager appWidgetManager,
                    int appWidgetId) {
                this.context = context;
                this.appWidgetManager = appWidgetManager;
                this.appWidgetId = appWidgetId;
                this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
                if (views != null) {
                    views.setViewVisibility(R.id.frmProgress, View.VISIBLE);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                String accountId = WidgetConfigureActivity.getAccountId(context, appWidgetId);
                if (accountId == null) {
                    Timber.w("Widget not found %d", appWidgetId);
                    return null;
                }
                long bankId = WidgetConfigureActivity.getBankId(context, appWidgetId);
                Bank bank = BankFactory.bankFromDb(bankId, context, true);
                if (bank == null) {
                    return null;
                }

                try {
                    if (!bank.isDisabled()) {
                        bank.update();
                        if (prefs.getBoolean("widget_updates_transactions", false)) {
                            bank.updateAllTransactions();
                        }
                        bank.closeConnection();
                        DBAdapter.save(bank, context);
                    }

                } catch (BankException e) {
                    Timber.e(e, "Could not update bank %s", bank.getShortName());
                } catch (LoginException e) {
                    Timber.w(e, "Invalid credentials for bank %s", bank.getShortName());
                    DBAdapter.disable(bank, context);
                } catch (BankChoiceException e) {
                } catch (IOException e) {
                    if (NetworkUtils.isInternetAvailable()) {
                        Timber.e(e, "Could not update bank %s", bank.getShortName());
                    }
                }
                BankdroidWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
                if (views != null) {
                    views.setViewVisibility(R.id.frmProgress, View.INVISIBLE);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
                WidgetService.this.stopSelf();
            }


        }
    }
}
