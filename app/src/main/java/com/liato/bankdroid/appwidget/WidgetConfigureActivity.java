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

package com.liato.bankdroid.appwidget;

import com.liato.bankdroid.LockableActivity;
import com.liato.bankdroid.R;
import com.liato.bankdroid.adapters.AccountsAdapter;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankFactory;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

public class WidgetConfigureActivity extends LockableActivity {

    private static final String WIDGET_PREFIX = "widget_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private AccountsAdapter adapter;

    public WidgetConfigureActivity() {
        super();

    }

    public static void setAccountBankId(Context context, int appWidgetId, String accountId,
            long bankId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0).edit();
        prefs.putString(WIDGET_PREFIX + appWidgetId, accountId);
        prefs.putLong(WIDGET_PREFIX + appWidgetId + "_bankid", bankId);
        prefs.apply();
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
        prefs.apply();
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
        ((View) findViewById(R.id.layMainMenu)).setVisibility(View.GONE);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        ListView lv = (ListView) findViewById(R.id.lstAccountsList);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItemViewType(position) != AccountsAdapter.VIEWTYPE_ACCOUNT) {
                    return;
                }
                final Context context = WidgetConfigureActivity.this;
                Account account = (Account) parent.getItemAtPosition(position);
                Bank bank = account.getBank();
                WidgetConfigureActivity
                        .setAccountBankId(context, mAppWidgetId, account.getId(), bank.getDbId());
                SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs", 0)
                        .edit();
                prefs.putBoolean("transperant_background" + mAppWidgetId,
                        ((CheckBox) findViewById(R.id.chkTransperantBackground)).isChecked());
                prefs.apply();
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

    private void refreshView() {
        ArrayList<Bank> banks = BankFactory.banksFromDb(this, true);

        if (banks.size() > 0) {
            findViewById(R.id.chkTransperantBackground).setVisibility(View.VISIBLE);
            findViewById(R.id.txtAccountsDesc).setVisibility(View.GONE);
            ListView lv = (ListView) findViewById(R.id.lstAccountsList);
            adapter = new AccountsAdapter(this, false);
            adapter.setGroups(banks);
            lv.setAdapter(adapter);
        }
    }


    @Override
    public boolean shouldShowActionBar() {
        return false;
    }
}
