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

package com.liato.bankdroid;

import com.liato.bankdroid.appwidget.AutoRefreshService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class StartupReceiver extends BroadcastReceiver {

    public static void setAlarm(Context context) {
        PendingIntent alarmSender;
        alarmSender = PendingIntent
                .getService(context, 0, new Intent(context, AutoRefreshService.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoUpdatesEnabled = prefs.getBoolean("autoupdates_enabled", true);
        Integer refreshRate = Integer.parseInt(prefs.getString("refresh_rate", "0"));
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (!autoUpdatesEnabled) {
            am.cancel(alarmSender);
        } else {
            long firstTime = SystemClock.elapsedRealtime();
            int secondsInMinute = 60;
            if (prefs.getBoolean("debug_mode", false) && prefs
                    .getBoolean("debug_refreshrate_in_seconds", false)) {
                secondsInMinute = 1;
            }
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    firstTime + refreshRate * secondsInMinute * 1000,
                    refreshRate * secondsInMinute * 1000, alarmSender);
        }

    }

    public static void updateNow(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoUpdatesEnabled = prefs.getBoolean("autoupdates_enabled", true);
        long lastUpdate = prefs.getLong("autoupdates_last_update", 0);
        Integer refreshRate = Integer.parseInt(prefs.getString("refresh_rate", "0"));
        if (autoUpdatesEnabled
                && System.currentTimeMillis() - lastUpdate > refreshRate * 60 * 1000) {
            context.startService(new Intent(context, AutoRefreshService.class));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Set alarms for auto updates on boot, package update, package replace and package new
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            updateNow(context);
        } else {
            setAlarm(context);
        }
    }
}
