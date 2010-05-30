package com.liato.bankdroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WidgetConfigureActivity extends Activity {
	private static final String WIDGET_PREFIX = "widget_";
	private DBAdapter dba;
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private String access_code;
	private SharedPreferences prefs;

	public WidgetConfigureActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		access_code = prefs.getString("access_code", "");
	}	

	public void onResume() {
		super.onResume();
		if (access_code.length() > 0) {
			handleLogin();
		}
		else {
			handleSelect();
		}
	}

	private void handleLogin() {
		setContentView(R.layout.login);
		((Button)findViewById(R.id.btnLogin)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((EditText)findViewById(R.id.edtAccessCode)).getText().toString().equals(access_code)) {
					handleSelect();
				}
				else {
					TextView txtResult = (TextView)findViewById(R.id.txtResult);
					txtResult.setVisibility(TextView.VISIBLE);
					AlphaAnimation anim = new AlphaAnimation(1, 0);
					anim.setFillAfter(true);
					anim.setStartOffset(5000);
					anim.setDuration(1000);
					txtResult.startAnimation(anim);
				}
			}
		});	
	}


	private void handleSelect() {
		setContentView(R.layout.main);
		this.setTitle(this.getString(R.string.choose_an_account));
		setResult(RESULT_CANCELED);
		((Button)findViewById(R.id.btnAccountsRefresh)).setVisibility(View.GONE);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}		

		ListView lv = (ListView)findViewById(R.id.lstAccountsList);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("!", "CLICK!");

				final Context context = WidgetConfigureActivity.this;
				AccountsAdapter.Item item = (AccountsAdapter.Item)parent.getItemAtPosition(position);

				WidgetConfigureActivity.setAccountId(context, mAppWidgetId, item.getId());

				// Push widget update to surface with newly set prefix
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				BankdroidWidgetProvider.updateAppWidget(context, appWidgetManager,
						mAppWidgetId, item);

				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
		refreshView();

	}
	public static String getAccountId(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
		return prefs.getString(WIDGET_PREFIX + appWidgetId, null);
	}

	public static void setAccountId(Context context, int appWidgetId, String value) {
		SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0).edit();
		prefs.putString(WIDGET_PREFIX + appWidgetId, value);
		prefs.commit();
	}

	public static void delAccountId(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0).edit();
		prefs.remove(WIDGET_PREFIX + appWidgetId);
		prefs.commit();
	}


	private void refreshView() {
		dba = new DBAdapter(this);
		dba.open();
		Cursor curBanks = dba.fetchBanks();
		Cursor curAccounts;

		if (curBanks != null && !curBanks.isLast() && !curBanks.isAfterLast()) {
			findViewById(R.id.txtAccountsDesc).setVisibility(View.GONE);
			startManagingCursor(curBanks);
			ListView lv = (ListView)findViewById(R.id.lstAccountsList);
			AccountsAdapter adapter = new AccountsAdapter(this);

			int clmId = curBanks.getColumnIndex("_id"); 
			int clmBanktype = curBanks.getColumnIndex("banktype");
			int clmUsername = curBanks.getColumnIndex("username");
			int clmBankbalance = curBanks.getColumnIndex("balance");
			List<AccountsAdapter.Item> items;
			AccountsAdapter.Group bank;

			while (!curBanks.isLast() && !curBanks.isAfterLast()) {
				items = new ArrayList<AccountsAdapter.Item>();
				curBanks.moveToNext();
				curAccounts = dba.fetchAccounts(curBanks.getLong(clmId));
				int clmBalance = curAccounts.getColumnIndex("balance"); 
				int clmName = curAccounts.getColumnIndex("name");
				int clmAccId = curAccounts.getColumnIndex("id");
				int acc_count = curAccounts.getCount();
				if (curAccounts != null && acc_count != 0) {
					while (!curAccounts.isLast()) {
						curAccounts.moveToNext();
						items.add(new AccountsAdapter.Item(curAccounts.getString(clmName), curAccounts.getDouble(clmBalance), curAccounts.getString(clmAccId)));
					}
				}
				bank = new AccountsAdapter.Group(curBanks.getString(clmUsername), curBanks.getString(clmBanktype), curBanks.getDouble(clmBankbalance), items);
				adapter.addGroup(bank);
				curAccounts.close();
			}
			lv.setAdapter(adapter);
		}
		curBanks.close();
		dba.close();
	}

	public void onDestroy() {
		super.onDestroy();
	}


}
