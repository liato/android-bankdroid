package com.liato.bankdroid;

import java.net.URI;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public abstract class BankdroidWidgetProvider extends AppWidgetProvider {
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, AccountsAdapter.Item item) {
		RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId, item);
		if (views != null) appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
		if (views != null) appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	static RemoteViews buildAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Log.d("BankdroidWigetProvider", "Updating widget: "+appWidgetId);
		String accountId = WidgetConfigureActivity.getAccountId(context, appWidgetId);
		if (accountId == null) {
			Log.d("BankdroidWidgetProvider", "Widget not found. ID: "+appWidgetId);
			return null;
		}
		DBAdapter dba = new DBAdapter(context);
		dba.open();
		Cursor c = dba.getAccount(accountId);
		if (c == null) {
			Log.d("BankdroidWidgetProvider", "Account not found in db: "+accountId);
			return null;
		}
		int clmBalance = c.getColumnIndex("balance");
		int clmId = c.getColumnIndex("id");
		int clmName = c.getColumnIndex("name");
		int clmBankId = c.getColumnIndex("bankid");
		Double balance = c.getDouble(clmBalance);
		String name = c.getString(clmName);
		String id = c.getString(clmId);
		Long bankId = c.getLong(clmBankId);
		AccountsAdapter.Item account = new AccountsAdapter.Item(name, balance, id);
		c.close();
		c = dba.getBank(bankId);
		if (c == null) {
			Log.d("BankdroidWidgetProvider", "Bank not found: " + bankId);
			return null;
		}

		int clmType = c.getColumnIndex("banktype");
		int clmDisabled = c.getColumnIndex("disabled");
		clmBalance = c.getColumnIndex("balance");
		clmName = c.getColumnIndex("username");
		name = c.getString(clmName);
		String type = c.getString(clmType);
		balance = c.getDouble(clmBalance);
		Log.d("dbdisabled", ""+c.getInt(clmDisabled));
		Boolean isDisabled = (c.getInt(clmDisabled) == 1 ? true : false);

		AccountsAdapter.Group group = new AccountsAdapter.Group(name, type, balance, account, isDisabled);
		account.setGroup(group);
		c.close();
		dba.close();
		return buildAppWidget(context, appWidgetManager,
				appWidgetId, account);
	}	



	static RemoteViews buildAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, AccountsAdapter.Item item) {
		Log.d("Widget", "Building widget: "+appWidgetId);
		AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		int layoutId = (providerInfo == null) ? R.layout.widget : providerInfo.initialLayout;
		AccountsAdapter.Group group = item.getGroup();
		RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
		Log.d("buildAppWidget", "WidgetLayout: "+layoutId);
		views.setTextViewText(R.id.txtWidgetAccountname, item.getName());
		views.setTextViewText(R.id.txtWidgetAccountbalance, Helpers.formatBalance(item.getBalance()));
		views.setImageViewResource(R.id.imgWidgetIcon, context.getResources().getIdentifier("drawable/"+Helpers.toAscii(group.getType().toLowerCase()), null, context.getPackageName()));
		Log.d("Disabled", ""+group.getDisabled());
		if (group.getDisabled()) {
			views.setViewVisibility(R.id.frmWarning, View.VISIBLE);
		}
		else {
			views.setViewVisibility(R.id.frmWarning, View.INVISIBLE);
		}
		Intent intent = new Intent(context, LoginActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.txtWidgetAccountbalance, pendingIntent);
		views.setOnClickPendingIntent(R.id.layWidgetContainer, pendingIntent);

		intent = new Intent(context, AccountsActivity.class);
		pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.imgWarning, pendingIntent);
		
		intent = new Intent(context, WidgetService.class);
		intent.setAction(AutoRefreshService.WIDGET_REFRESH);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.setData(Uri.parse("rofl://copter/"+appWidgetId+"/"+System.currentTimeMillis()));
		pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.imgWidgetIcon, pendingIntent);

		//appWidgetManager.updateAppWidget(appWidgetId, views); 
		return views;
	}
	
	
	
	
	public void onReceive(Context context, Intent intent) {
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt( AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		}
		else {
			super.onReceive(context, intent);
		}


		Log.d("BankdroidWidgetProvider", "intent=" + intent+"; action="+action);
		if (action.equals(AutoRefreshService.WIDGET_REFRESH) || action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
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
			Log.d("Widget", "Widget deleted: " + appWidgetIds[i]);
			WidgetConfigureActivity.delAccountId(context, appWidgetIds[i]);
		}
	}

	public static class WidgetService extends Service {
		@Override
		public void onStart(Intent intent, int startId) {
			int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			Log.d("WidgetService", "Updating widget: " + appWidgetId);

			String action = intent.getAction();
			if (action.equals(AutoRefreshService.WIDGET_REFRESH)) {
				Context context = getApplicationContext();
				new WidgetUpdateTask(context, AppWidgetManager.getInstance(context), appWidgetId).execute();
			}
		}

		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		protected class WidgetUpdateTask extends AsyncTask<Void, Void, Void> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
				if (views != null) {
					views.setViewVisibility(R.id.frmProgress, View.VISIBLE);
					appWidgetManager.updateAppWidget(appWidgetId, views);
				}
			}

			private Context context;
			private AppWidgetManager appWidgetManager;
			private int appWidgetId;

			public WidgetUpdateTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
				this.context = context;
				this.appWidgetManager = appWidgetManager;
				this.appWidgetId = appWidgetId;
			}

			@Override
			protected Void doInBackground(Void... params) {
				String accountId = WidgetConfigureActivity.getAccountId(context, appWidgetId);
				if (accountId == null) {
					Log.d("WidgetService", "Widget not found in db: "+appWidgetId);
					return null;
				}
				DBAdapter db = new DBAdapter(context);
				db.open();
				String bankId = accountId.split("_")[0];
				Cursor c = db.getBank(bankId);
				if (c == null) {
					return null;
				}

				int clmId = c.getColumnIndex("_id");
				int clmBanktype = c.getColumnIndex("banktype");
				int clmBalance = c.getColumnIndex("balance");
				int clmUsername = c.getColumnIndex("username");
				int clmPassword = c.getColumnIndex("password");
				int clmDisabled = c.getColumnIndex("disabled");
				try {
					if (c.getInt(clmDisabled) == 0) {
						Class cls = Class.forName("com.liato.bankdroid.Bank"+Helpers.toAscii(c.getString(clmBanktype)));
						Bank bank = (Bank) cls.newInstance();
						bank.update(c.getString(clmUsername), c.getString(clmPassword), context);
						db.updateBank(bank, new Long(c.getString(clmId)));
					}
					else {
						Log.d("BankdroidWidgetProvider", "Bank is disabled, skipping refresh on "+c.getLong(clmId));
					}
				} 
				catch (BankException e) {
					Log.d("", "Disabling bank: "+c.getLong(clmId));
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
				finally {
					c.close();
					db.close();
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
