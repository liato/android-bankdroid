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
	@Override
	public void onReceive(Context context, Intent intent) {
		//Set alarms for auto updates on boot
		Log.d("StartupReceiever", "Intent action: "+intent.getAction());
		setAlarm(context);
	}
	
	public static void setAlarm(Context context) {
		PendingIntent alarmSender;
		alarmSender = PendingIntent.getService(context, 0, new Intent(context, AutoRefreshService.class), 0);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Integer refreshrate = prefs.getInt("refreshrate", -1);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (refreshrate < 0) {
        	am.cancel(alarmSender);
        	Log.d("","Alarm disabled.");
        }
        else {
	        long firstTime = SystemClock.elapsedRealtime();
	        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime+refreshrate*60*1000, refreshrate*60*1000, alarmSender);
        	Log.d("StartupReceiever.SetAlarm","Alarm set to "+refreshrate.toString()+" minutes.");
        }
	
	}
}