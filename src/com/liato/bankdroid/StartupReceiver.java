package com.liato.bankdroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver{
    private final static String TAG = "StartupReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		//Set alarms for auto updates on boot, package update, package replace and package new
		Log.d("StartupReceiever", "Intent action: "+intent.getAction());
		setAlarm(context);
	}
	
	public static void setAlarm(Context context) {
		PendingIntent alarmSender;
		alarmSender = PendingIntent.getService(context, 0, new Intent(context, AutoRefreshService.class), 0);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoUpdatesEnabled = prefs.getBoolean("autoupdates_enabled", true);
        Integer refreshRate = Integer.parseInt(prefs.getString("refresh_rate", "0")); 
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (!autoUpdatesEnabled) {
        	am.cancel(alarmSender);
        	Log.d(TAG,"Automatic updates have been disabled.");
        }
        else {
	        long firstTime = SystemClock.elapsedRealtime();
	        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime+refreshRate*1*1000, refreshRate*1*1000, alarmSender);
        	Log.d(TAG,"Automatic updates set to "+refreshRate.toString()+" minutes.");
        }
	
	}
}