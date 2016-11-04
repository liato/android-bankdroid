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
import com.liato.bankdroid.liveview.LiveViewService;
import com.liato.bankdroid.utils.LoggingUtils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class AutoRefreshService extends Service {

    public final static String BROADCAST_WIDGET_REFRESH = "com.liato.bankdroid.WIDGET_REFRESH";

    public final static String BROADCAST_MAIN_REFRESH = "com.liato.bankdroid.MAIN_REFRESH";

    public final static String BROADCAST_REMOTE_NOTIFIER
            = "org.damazio.notifier.service.UserReceiver.USER_MESSAGE";

    public final static String BROADCAST_OPENWATCH_TEXT = "com.smartmadsoft.openwatch.action.TEXT";

    public final static String BROADCAST_OPENWATCH_VIBRATE
            = "com.smartmadsoft.openwatch.action.VIBRATE";

    public final static String ACTION_MAIN_SHOW_TRANSACTIONS
            = "com.liato.bankdroid.action.MAIN_SHOW_TRANSACTIONS";

    public final static String BROADCAST_TRANSACTIONS_UPDATED
            = "com.liato.bankdroid.action.TRANSACTIONS";

    public static void showNotification(final Bank bank, final Account account,
            final BigDecimal diff, Context context) {

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("notify_on_change", true)) {
            return;
        }

        String text = String.format("%s: %s%s", account.getName(),
                ((diff.compareTo(new BigDecimal(0)) == 1) ? "+" : ""),
                Helpers.formatBalance(diff, account.getCurrency()));
        if (!prefs.getBoolean("notify_delta_only", false)) {
            text = String.format("%s (%s)", text,
                    Helpers.formatBalance(account.getBalance(), account.getCurrency()));
        }

        final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(bank.getImageResource())
                .setContentTitle(bank.getDisplayName())
                .setContentText(text);

        // Remove notification from statusbar when clicked
        notification.setAutoCancel(true);

        // http://www.freesound.org/samplesViewSingle.php?id=75235
        // http://www.freesound.org/samplesViewSingle.php?id=91924
        if (prefs.getString("notification_sound", null) != null) {
            notification.setSound(Uri.parse(prefs.getString(
                    "notification_sound", null)));
        }
        if (prefs.getBoolean("notify_with_vibration", true)) {
            final long[] vib = {0, 90, 130, 80, 350, 190, 20, 380};
            notification.setVibrate(vib);
        }

        if (prefs.getBoolean("notify_with_led", true)) {
            notification.setLights(prefs.getInt("notify_with_led_color",
                    context.getResources().getColor(R.color.default_led_color)), 700, 200);
        }

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        notification.setContentIntent(contentIntent);

        String numNotifications = prefs.getString("num_notifications", "total");
        int notificationId = (int) (numNotifications.equals("total") ? 0
                : numNotifications.equals("bank") ? bank.getDbId()
                        : numNotifications.equals("account") ? account.getId().hashCode()
                                : SystemClock.elapsedRealtime());
        notificationManager.notify(notificationId, notification.build());

        // Broadcast to Remote Notifier if enabled
        // http://code.google.com/p/android-notifier/
        if (prefs.getBoolean("notify_remotenotifier", false)) {
            final Intent i = new Intent(BROADCAST_REMOTE_NOTIFIER);
            i.putExtra("title", String.format("%s (%s)", bank.getName(), bank.getDisplayName()));
            i.putExtra("description", text);
            context.sendBroadcast(i);
        }

        // Broadcast to OpenWatch if enabled
        // http://forum.xda-developers.com/showthread.php?t=554551
        if (prefs.getBoolean("notify_openwatch", false)) {
            Intent i;
            if (prefs.getBoolean("notify_openwatch_vibrate", false)) {
                i = new Intent(BROADCAST_OPENWATCH_VIBRATE);
            } else {
                i = new Intent(BROADCAST_OPENWATCH_TEXT);
            }
            i.putExtra("line1", String.format("%s (%s)", bank.getName(), bank.getDisplayName()));
            i.putExtra("line2", text);
            context.sendBroadcast(i);
        }

        // Broadcast to LiveView if enabled
        // http://www.sonyericsson.com/cws/products/accessories/overview/liveviewmicrodisplay
        if (prefs.getBoolean("notify_liveview", false)) {
            final Intent i = new Intent(context, LiveViewService.class);
            i.putExtra(LiveViewService.INTENT_EXTRA_ANNOUNCE, true);
            i.putExtra(LiveViewService.INTENT_EXTRA_TITLE,
                    String.format("%s (%s)", bank.getName(), bank.getDisplayName()));
            i.putExtra(LiveViewService.INTENT_EXTRA_TEXT, text);
            context.startService(i);
        }

    }

    public static void broadcastTransactionUpdate(final Context context,
            final long bankId, final String accountId) {
        final Intent i = new Intent(BROADCAST_TRANSACTIONS_UPDATED);
        i.putExtra("accountId", Long.toString(bankId) + "_" + accountId);
        context.sendBroadcast(i);
    }

    public static void sendWidgetRefresh(final Context context) {
        // Send intent to BankdroidWidgetProvider
        final Intent updateIntent = new Intent(BROADCAST_WIDGET_REFRESH);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (final CanceledException e) {
            Timber.w(e, "Problem occurred while updating widget");
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        handleStart();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleStart();
        return START_NOT_STICKY;
    }

    private void handleStart() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null &&
                ni.isConnected() &&
                shouldUpdateOnRoaming(ni)) {
            if (InsideUpdatePeriod()) {
                new DataRetrieverTask(this).execute();
            } else {
                Timber.v("Skipping update due to not in update period.");
                stopSelf();
            }
        }
    }

    private boolean shouldUpdateOnRoaming(NetworkInfo ni) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (prefs.getBoolean("disable_during_roaming", false) && ni.isRoaming()) {
            return false;
        }
        return true;
    }

    private boolean InsideUpdatePeriod() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        int start = prefs.getInt("refresh_start_minutes", 0);
        int stop = prefs.getInt("refresh_stop_minutes", 0);

        // If start is bigger than stop we always update. It should perhaps
        // be possible to set start to 17:00 and stop to 07:00 and have to
        // updates working from 17 to 07 around midnight
        if (start >= stop) {
            return true;
        }

        Date now = new Date();
        int minutesSinceMidnight = now.getHours() * 60 + now.getMinutes();
        return minutesSinceMidnight > start && minutesSinceMidnight < stop;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    static class DataRetrieverTask extends AsyncTask<String, String, Void> {

        private final SharedPreferences prefs;

        private ArrayList<String> errors;
        protected final AutoRefreshService autoRefreshService;

        private Resources res;

        // This constructor is for unit testing only
        protected DataRetrieverTask(AutoRefreshService autoRefreshService, SharedPreferences prefs) {
            this.autoRefreshService = autoRefreshService;
            this.prefs = prefs;
        }

        public DataRetrieverTask(AutoRefreshService autoRefreshService) {
            this(autoRefreshService,
                    PreferenceManager.getDefaultSharedPreferences(autoRefreshService));
        }

        @Override
        protected void onPreExecute() {
        }

        protected List<Bank> getBanks() {
            return BankFactory.banksFromDb(autoRefreshService, true);
        }

        @NonNull
        protected DBAdapter getDBAdapter() {
            return new DBAdapter(autoRefreshService);
        }

        protected void sendWidgetRefresh() {
            final Intent updateIntent = new Intent(BROADCAST_MAIN_REFRESH);
            autoRefreshService.sendBroadcast(updateIntent);
            AutoRefreshService.sendWidgetRefresh(autoRefreshService);
        }

        @Override
        protected Void doInBackground(final String... args) {
            errors = new ArrayList<>();
            Boolean refreshWidgets = false;
            final List<Bank> banks = getBanks();
            if (banks.isEmpty()) {
                return null;
            }
            final DBAdapter db = getDBAdapter();
            BigDecimal currentBalance;
            BigDecimal diff;
            BigDecimal minDelta = new BigDecimal(prefs.getString("notify_min_delta", "0"));

            final HashMap<String, Account> accounts = new HashMap<>();

            for (final Bank bank : banks) {
                if (prefs.getBoolean("debug_mode", false)
                        && prefs.getBoolean("debug_only_testbank", false)) {
                    Timber.d(
                            "Only_Testbank is ON. Skipping update for %s",
                            bank.getName());
                    continue;
                }
                if (bank.isDisabled()) {
                    LoggingUtils.logDisabledBank(bank);
                    continue;
                }
                try {
                    currentBalance = bank.getBalance();
                    accounts.clear();
                    for (final Account account : bank.getAccounts()) {
                        accounts.put(account.getId(), account);
                    }
                    bank.update();

                    diff = currentBalance.subtract(bank.getBalance());

                    if (diff.compareTo(BigDecimal.ZERO) != 0) {
                        refreshWidgets = true;
                    }

                    if (diff.compareTo(new BigDecimal(0)) != 0
                            && diff.abs().compareTo(minDelta) != -1) {
                        Account oldAccount;
                        for (final Account account : bank.getAccounts()) {
                            oldAccount = accounts.get(account.getId());
                            if (oldAccount != null) {
                                if (account.getBalance().compareTo(
                                        oldAccount.getBalance()) != 0) {
                                    boolean notify = false;
                                    switch (account.getType()) {
                                        case Account.REGULAR:
                                            notify = prefs.getBoolean(
                                                    "notify_for_deposit", true);
                                            break;
                                        case Account.FUNDS:
                                            notify = prefs.getBoolean(
                                                    "notify_for_funds", false);
                                            break;
                                        case Account.LOANS:
                                            notify = prefs.getBoolean(
                                                    "notify_for_loans", false);
                                            break;
                                        case Account.CCARD:
                                            notify = prefs.getBoolean(
                                                    "notify_for_ccards", true);
                                            break;
                                        case Account.OTHER:
                                            notify = prefs.getBoolean(
                                                    "notify_for_other", false);
                                            break;
                                    }
                                    if (account.isHidden()
                                            || !account.isNotify()) {
                                        notify = false;
                                    }
                                    if (notify) {
                                        diff = account.getBalance().subtract(
                                                oldAccount.getBalance());
                                        showNotification(bank, account, diff,
                                                autoRefreshService);
                                    }
                                }
                            }
                        }
                        if (prefs.getBoolean(
                                "autoupdates_transactions_enabled", true)) {
                            bank.updateAllTransactions();
                            LoggingUtils.logBankUpdate(bank, true);
                        } else {
                            LoggingUtils.logBankUpdate(bank, false);
                        }
                    }
                    bank.closeConnection();
                    db.updateBank(bank);

                    // Send update for all accounts since we're overwriting the
                    // database transaction history
                    if (prefs.getBoolean("content_provider_enabled", false)) {
                        for (final Account account : bank.getAccounts()) {
                            broadcastTransactionUpdate(autoRefreshService.getBaseContext(),
                                    bank.getDbId(), account.getId());
                        }
                    }
                } catch (final BankException e) {
                    // Refresh widgets if an update fails
                    Timber.e(e, "Could not update bank %s", bank.getName());
                } catch (final LoginException e) {
                    Timber.d(e, "Invalid credentials for bank %s", bank.getName());
                    refreshWidgets = true;
                    db.disableBank(bank.getDbId());
                } catch (BankChoiceException e) {
                    Timber.w(e, "BankChoiceException");
                } catch (Exception e) {
                    Timber.e(e, "An unexpected error occurred while updating bank %s", bank.getName());
                }
            }

            if (refreshWidgets) {
                sendWidgetRefresh();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final String... args) {
        }

        @Override
        protected void onPostExecute(final Void unused) {
            if ((this.errors != null) && !this.errors.isEmpty()) {
                final StringBuilder errormsg = new StringBuilder();
                errormsg.append(res.getText(R.string.accounts_were_not_updated)
                        + ":\n");
                for (final String err : errors) {
                    errormsg.append(err);
                    errormsg.append("\n");
                }
            }
            Editor edit = prefs.edit();
            edit.putLong("autoupdates_last_update", System.currentTimeMillis());
            edit.apply();
            autoRefreshService.stopSelf();
        }
    }
}
