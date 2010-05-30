package com.liato.bankdroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

public class BankdroidWidgetProvider extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.d("Widget", "onUpdate: "+i);
            // Create an Intent to launch ExampleActivity
            //Intent intent = new Intent(context, ExampleActivity.class);
            //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_layout);
            //views.setOnClickPendingIntent(R.id.button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            //appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, AccountsAdapter.Item item) {
		Log.d("Widget", "Widget created");
		AccountsAdapter.Group group = item.getGroup();
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		views.setTextViewText(R.id.txtWidgetAccountname, item.getName());
		views.setTextViewText(R.id.txtWidgetAccountbalance, Helpers.formatBalance(item.getBalance()));
		views.setImageViewResource(R.id.imgWidgetIcon, context.getResources().getIdentifier("drawable/"+Helpers.toAscii(group.getType().toLowerCase()), null, context.getPackageName()));

        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.txtWidgetAccountbalance, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Log.d("BankdroidWigetProvider", "Updating widget: "+appWidgetId);
		String accountId = WidgetConfigureActivity.getAccountId(context, appWidgetId);
		if (accountId == null) {
			Log.d("BankdroidWidgetProvider", "Widget not found. ID: "+appWidgetId);
			return;
		}
		DBAdapter dba = new DBAdapter(context);
		dba.open();
		Cursor c = dba.getAccount(accountId);
		if (c == null) {
			Log.d("BankdroidWidgetProvider", "Account not found in db: "+accountId);
			return;
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
		c = dba.getBank(bankId);
		if (c == null) {
			Log.d("BankdroidWidgetProvider", "Bank not found: " + bankId);
			return;
		}
		
		int clmType = c.getColumnIndex("banktype");
		clmBalance = c.getColumnIndex("balance");
		clmName = c.getColumnIndex("username");
		name = c.getString(clmName);
		String type = c.getString(clmType);
		balance = c.getDouble(clmBalance);
		AccountsAdapter.Group group = new AccountsAdapter.Group(name, type, balance, account);
		account.setGroup(group);
		c.close();
		dba.close();
		updateAppWidget(context, appWidgetManager,
				appWidgetId, account);
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

		
		Log.d("BankdroidWidgetProvider", "intent=" + intent);
		
		if (action.equals(AutoRefreshService.WIDGET_REFRESH)) {
            AppWidgetManager appWM = AppWidgetManager.getInstance(context);
        	int[] appWidgetIds = appWM.getAppWidgetIds(new ComponentName(context, BankdroidWidgetProvider.class));
        	final int N = appWidgetIds.length;
    		for (int i = 0; i < N; i++) {
    			int appWidgetId = appWidgetIds[i];
    			updateAppWidget(context, appWM, appWidgetId);
    		}
		}
    }	

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			WidgetConfigureActivity.delAccountId(context, appWidgetIds[i]);
		}
	}

}
