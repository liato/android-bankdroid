package com.liato.bankdroid;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoRefreshService extends Service {
	private final static String TAG = "AutoRefreshService";
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
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
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
    	private ArrayList<String> errors;
    	private Resources res;

    	public DataRetrieverTask() {
    	}
    	protected void onPreExecute() {
    	}

    	protected Void doInBackground(final String... args) {
    		Log.d("doinback", "round");
    		errors = new ArrayList<String>();
    		Boolean refreshWidgets = false;
    		ArrayList<Bank> banks = BankFactory.banksFromDb(AutoRefreshService.this, false);
    		if (banks.isEmpty()) {
    			return null;
    		}
    		DBAdapter db = new DBAdapter(AutoRefreshService.this);
    		db.open();    		
    		Double currentBalance;
    		Double diff;
    		
    		for (Bank bank : banks) {
    			if (bank.isDisabled()) {
    				Log.d("AA", bank.getName()+" ("+bank.getUsername()+") is disabled. Skipping refresh.");
    				continue;
    			}
				Log.d("AA", "Refreshing "+bank.getName()+" ("+bank.getUsername()+").");
    			try {
    				currentBalance = bank.getBalance().doubleValue();
    				bank.update();
					diff =  bank.getBalance().doubleValue() - currentBalance;
    				if (diff != 0) {
    					showNotification(bank.getName()+ ": "+ ((diff > 0) ? "+" : "") + Helpers.formatBalance(diff) + " ("+Helpers.formatBalance(bank.getBalance())+")");
    					refreshWidgets = true;
    					bank.updateAllTransactions();
    				}
    				bank.closeConnection();
    				db.updateBank(bank);
    			} 
    			catch (BankException e) {
    				// Refresh widgets if an update fails
    				Log.d(TAG, "Error while updating bank '"+bank.getDbId()+"'; "+e.getMessage());
    			} catch (LoginException e) {
    				refreshWidgets = true;
    				db.disableBank(bank.getDbId());
				}
    		}
    		
			if (refreshWidgets) {
				sendWidgetRefresh(AutoRefreshService.this);
			}
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