package com.liato.bankdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class WidgetConfigureActivity extends LockableActivity {
    private static final String WIDGET_PREFIX = "widget_";
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static final int LOGIN_ID = 1;
	private AccountsAdapter adapter;
	
	public WidgetConfigureActivity() {
		super();
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}	
	
	public void onResume() {
		super.onResume();

        setContentView(R.layout.main);
        this.setTitle(this.getString(R.string.choose_an_account));
        setResult(RESULT_CANCELED);
        ((View)findViewById(R.id.layMainMenu)).setVisibility(View.GONE);

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
                if (adapter.getItemViewType(position) != AccountsAdapter.VIEWTYPE_ACCOUNT) return;
                final Context context = WidgetConfigureActivity.this;
                Account account = (Account)parent.getItemAtPosition(position);
                Bank bank = account.getBank();
                WidgetConfigureActivity.setAccountBankId(context, mAppWidgetId, account.getId(), bank.getDbId());
                SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0).edit();
                prefs.putBoolean("transperant_background" + mAppWidgetId, ((CheckBox)findViewById(R.id.chkTransperantBackground)).isChecked());
                prefs.commit();             
                // Push widget update to surface with newly set prefix
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                BankdroidWidgetProvider.updateAppWidget(context, appWidgetManager,
                        mAppWidgetId, account);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        
        refreshView();
    }
	
	public static void setAccountBankId(Context context, int appWidgetId, String accountId, long bankId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0).edit();
        prefs.putString(WIDGET_PREFIX + appWidgetId, accountId);
        prefs.putLong(WIDGET_PREFIX + appWidgetId + "_bankid", bankId);
        prefs.commit();
	}

    public static String getAccountId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
        return prefs.getString(WIDGET_PREFIX + appWidgetId, null);
    }

    public static long getBankId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
        return prefs.getLong(WIDGET_PREFIX + appWidgetId + "_bankid", -1);
    }
	
	public static void delAccountId(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0).edit();
        prefs.remove(WIDGET_PREFIX + appWidgetId);
        prefs.remove(WIDGET_PREFIX + appWidgetId + "_bankid");
        prefs.commit();
	}
	
	
	private void refreshView() {
		ArrayList<Bank> banks = BankFactory.banksFromDb(this, true);

		if (banks.size() > 0) {
			findViewById(R.id.chkTransperantBackground).setVisibility(View.VISIBLE);
			findViewById(R.id.txtAccountsDesc).setVisibility(View.GONE);
			ListView lv = (ListView)findViewById(R.id.lstAccountsList);
			adapter = new AccountsAdapter(this, false);
			adapter.setGroups(banks);
			lv.setAdapter(adapter);
		}
	}


}
