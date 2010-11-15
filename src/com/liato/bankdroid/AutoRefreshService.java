package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoRefreshService extends Service {
	private final static String TAG = "AutoRefreshService";
	final static String BROADCAST_WIDGET_REFRESH = "com.liato.bankdroid.WIDGET_REFRESH";
	final static String BROADCAST_MAIN_REFRESH = "com.liato.bankdroid.MAIN_REFRESH";
    final static String BROADCAST_REMOTE_NOTIFIER = "org.damazio.notifier.service.UserReceiver.USER_MESSAGE";
    final static String BROADCAST_OPENWATCH_TEXT = "com.smartmadsoft.openwatch.action.TEXT";
    final static String BROADCAST_OPENWATCH_VIBRATE = "com.smartmadsoft.openwatch.action.VIBRATE";
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

    private void showNotification(String text, int icon, String title, String bank) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean("notify_on_change", true)) return;
		
        Notification notification = new Notification(icon, text,
                System.currentTimeMillis());
        // Remove notification from statusbar when clicked
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        
        //http://www.freesound.org/samplesViewSingle.php?id=75235
        //http://www.freesound.org/samplesViewSingle.php?id=91924
        Log.d(TAG, "Notification sound: "+prefs.getString("notification_sound", "none"));
        if (prefs.getString("notification_sound", null) != null) {
            notification.sound = Uri.parse(prefs.getString("notification_sound", null));
        }
		if (prefs.getBoolean("notify_with_vibration", true)) {
			long[] vib = {0, 90, 130, 80, 350, 190, 20, 380};
			notification.vibrate = vib;
			//notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        notification.setLatestEventInfo(this, title, text, contentIntent);

        notificationManager.notify(R.id.about, notification);
        
        // Broadcast to Remote Notifier if enabled
        // http://code.google.com/p/android-notifier/
        if (prefs.getBoolean("notify_remotenotifier", false)) {
            Intent i = new Intent(BROADCAST_REMOTE_NOTIFIER);
            i.putExtra("title", String.format("%s (%s)", bank, title));
            i.putExtra("description", text);
            sendBroadcast(i);
        }

        // Broadcast to OpenWatch if enabled
        // http://forum.xda-developers.com/showthread.php?t=554551
        if (prefs.getBoolean("notify_openwatch", false)) {
            Intent i;
            if (prefs.getBoolean("notify_openwatch_vibrate", false)) {
                i = new Intent(BROADCAST_OPENWATCH_TEXT);
            }
            else {
                i = new Intent(BROADCAST_OPENWATCH_VIBRATE);
            }
            i.putExtra("line1", String.format("%s (%s)", bank, title));
            i.putExtra("line2", text);
            sendBroadcast(i);
        }
        
        
    }
    
    private class DataRetrieverTask extends AsyncTask<String, String, Void> {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoRefreshService.this);
    	private ArrayList<String> errors;
    	private Resources res;

    	public DataRetrieverTask() {
    	}
    	protected void onPreExecute() {
    	}

    	protected Void doInBackground(final String... args) {
    		errors = new ArrayList<String>();
    		Boolean refreshWidgets = false;
    		ArrayList<Bank> banks = BankFactory.banksFromDb(AutoRefreshService.this, true);
    		if (banks.isEmpty()) {
    			return null;
    		}
    		DBAdapter db = new DBAdapter(AutoRefreshService.this);
    		db.open();    		
    		BigDecimal currentBalance;
    		BigDecimal diff;
    		HashMap<String, Account> accounts = new HashMap<String, Account>();
    		
    		for (Bank bank : banks) {
                if (prefs.getBoolean("debug_mode", false) && prefs.getBoolean("debug_only_testbank", false)) {
                    Log.d(TAG, "Debug::Only_Testbank is ON. Skipping update for "+bank.getName());
                    continue;
                }
    			if (bank.isDisabled()) {
    				Log.d(TAG, bank.getName()+" ("+bank.getDisplayName()+") is disabled. Skipping refresh.");
    				continue;
    			}
				Log.d(TAG, "Refreshing "+bank.getName()+" ("+bank.getDisplayName()+").");
    			try {
    				currentBalance = bank.getBalance();
    				accounts.clear();
    				for(Account account : bank.getAccounts()) {
    					accounts.put(account.getId(), account);
    				}
    				bank.update();
					diff = currentBalance.subtract(bank.getBalance());
    				if (diff.compareTo(new BigDecimal(0)) != 0) {
    					Account oldAccount;
    					for(Account account : bank.getAccounts()) {
    						oldAccount = accounts.get(account.getId());
    						if (oldAccount != null) {
    							if (account.getBalance().compareTo(oldAccount.getBalance()) != 0) {
    							    boolean notify = false;
    							    switch (account.getType()) {
    							    case Account.REGULAR:
    							        notify = prefs.getBoolean("notify_for_deposit", true);
    							        break;
    							    case Account.FUNDS:
    							        notify = prefs.getBoolean("notify_for_funds", false);
    							        break;
    							    case Account.LOANS:
    							        notify = prefs.getBoolean("notify_for_loans", false);
    							        break;
    							    case Account.CCARD:
    							        notify = prefs.getBoolean("notify_for_ccards", true);
    							        break;
    							    case Account.OTHER:
    							        notify = prefs.getBoolean("notify_for_other", false);
    							        break;
    							    }
    							    Log.d(TAG, "Account type: "+account.getType()+"; notify: "+notify);
    							    if (account.isHidden() || !account.isNotify()) {
    							        notify = false;
    							    }
    		                        if (notify) {
    		                            diff = account.getBalance().subtract(oldAccount.getBalance());
        								showNotification(account.getName()+ ": "+ ((diff.compareTo(new BigDecimal(0)) == 1) ? "+" : "") + Helpers.formatBalance(diff, account.getCurrency()) + " ("+Helpers.formatBalance(account.getBalance(), account.getCurrency())+")",
        												 bank.getImageResource(), bank.getDisplayName(), bank.getName());
                                    }
    								refreshWidgets = true;
    							}
    						}
    					}
    					if (prefs.getBoolean("autoupdates_transactions_enabled", true)) {
    					    bank.updateAllTransactions();    					    
    					}
    				}
    				bank.closeConnection();
    				db.updateBank(bank);
    			} 
    			catch (BankException e) {
    				// Refresh widgets if an update fails
    				Log.d(TAG, "Error while updating bank '"+bank.getDbId()+"'; BankException: "+e.getMessage());
    			} catch (LoginException e) {
                    Log.d(TAG, "Error while updating bank '"+bank.getDbId()+"'; LoginException: "+e.getMessage());
    				refreshWidgets = true;
    				db.disableBank(bank.getDbId());
				}
    		}
    		
			if (refreshWidgets) {
				Intent updateIntent = new Intent(BROADCAST_MAIN_REFRESH);
				sendBroadcast(updateIntent);
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
        Intent updateIntent = new Intent(BROADCAST_WIDGET_REFRESH);
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