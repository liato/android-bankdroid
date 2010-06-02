package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoRefreshService extends Service {
	final static String WIDGET_REFRESH = "com.liato.bankdroid.WIDGET_REFRESH";
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        new DataRetrieverTask().execute();
    }

    @Override
    public void onDestroy() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(String text) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean("notify_on_change", true)) return;
		
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());
		if (prefs.getBoolean("notify_with_sound", true)) {
	        notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if (prefs.getBoolean("notify_with_vibration", true)) {
			//long[] vib = {23,28,27,143,20,30,26,364,22,26,28,26,28,26,28,26,29,25,27,27,27,27,28,28,28,28,28,27,27,26,27};
			//long[] vib = {46, 56, 54, 286, 40, 60, 52, 728, 44, 52, 56, 52, 56, 52, 56, 52, 58, 50, 54, 54, 54, 54, 56, 56, 56, 56, 56, 54, 54, 52, 54};
			//notification.vibrate = vib;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        notification.setLatestEventInfo(this,  this.getString(R.string.app_name), text, contentIntent);

        notificationManager.notify(R.id.about, notification);
    }
    
    private class DataRetrieverTask extends AsyncTask<String, String, Void> {
    	private Class<?> cls;
    	private ArrayList<String> errors;
    	private Bank bank;
    	private int bankcount;
    	private Resources res;

    	public DataRetrieverTask() {
    	}
    	protected void onPreExecute() {
    	}

    	protected Void doInBackground(final String... args) {
    		Log.d("doinback", "round");
    		errors = new ArrayList<String>();
    		Boolean refreshWidgets = false;
    		DBAdapter db;
    		Cursor c;
    		db = new DBAdapter(AutoRefreshService.this);
    		db.open();
    		c = db.fetchBanks();
    		if (c == null) {
    			return null;
    		}
    		bankcount = c.getCount();
    		
    		Double currentBalance;
    		int clmId = c.getColumnIndex("_id");
    		int clmBanktype = c.getColumnIndex("banktype");
    		int clmBalance = c.getColumnIndex("balance");
    		int clmUsername = c.getColumnIndex("username");
    		int clmPassword = c.getColumnIndex("password");
    		int clmDisabled = c.getColumnIndex("disabled");
    		int i = 0; 
    		while (!c.isLast() && !c.isAfterLast()) {
    			c.moveToNext();
    			//publishProgress(new String[] {new Integer(i).toString(), c.getString(clmBanktype)+" ("+c.getString(clmUsername)+")"});
    			//showNotification("Uppdaterar "+c.getString(clmBanktype));
    			if (c.getInt(clmDisabled) == 1) {
    				Log.d("AA", c.getString(clmBanktype)+" ("+c.getString(clmUsername)+") is disabled. Skipping refresh.");
    				continue;
    			}
				Log.d("AA", "Refreshing "+c.getString(clmBanktype)+" ("+c.getString(clmUsername)+").");
    			try {
    				currentBalance = c.getDouble(clmBalance);
    				cls = Class.forName("com.liato.bankdroid.Bank"+Helpers.toAscii(c.getString(clmBanktype)));
    				bank = (Bank) cls.newInstance();
    				bank.update(c.getString(clmUsername), c.getString(clmPassword), AutoRefreshService.this);
    				Log.d("aa",bank.getBalance().toString());
					Double diff =  bank.getBalance().doubleValue() - currentBalance;
    				if (diff != 0) {
    					showNotification(c.getString(clmBanktype)+ ": "+ ((diff > 0) ? "+" : "") + Helpers.formatBalance(diff) + " ("+Helpers.formatBalance(bank.getBalance())+")");
    					refreshWidgets = true;
    				}
    				db.updateBank(bank, c.getLong(clmId));
    				i++;
    			} 
    			catch (BankException e) {
    				db.disableBank(c.getLong(clmId));
    			}
    			catch (ClassNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IllegalAccessException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (InstantiationException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
    		}
    		
			if (refreshWidgets) {
				sendWidgetRefresh(AutoRefreshService.this);
			}
    		publishProgress(new String[] {new Integer(i).toString(), ""});
    		c.close();
    		db.close();
    		return null;
    	}

    	protected void onProgressUpdate(String... args) {
    	}
    	protected void onPostExecute(final Void unused) {
    		if (this.errors != null && !this.errors.isEmpty()) {
    			StringBuilder errormsg = new StringBuilder();
    			errormsg.append(res.getText(R.string.accounts_were_not_updated)+":\n");
    			for (String err : errors)
    			{
    			  errormsg.append(err);
    			  errormsg.append("\n");
    			}
    			Log.d("aa", errormsg.toString());
    		}
    		AutoRefreshService.this.stopSelf();
    	}
    }	

    
    public static void sendWidgetRefresh(Context context) {
    	//Send intent to BankdroidWidgetProvider
        Intent updateIntent = new Intent(WIDGET_REFRESH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
        		context, 0, updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
            try {
				pendingIntent.send();
			} catch (CanceledException e) {
				// TODO Auto-generated catch block
				Log.e("", e.getMessage(), e);
			}
    }    
    
}