package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.liato.bankdroid.AccountsAdapter.Group;
import com.liato.bankdroid.AccountsAdapter.Item;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends LockableActivity {
	/** Called when the activity is first created. */
	private DBAdapter dba;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.main);
		Button btnRefresh = (Button)findViewById(R.id.btnAccountsRefresh);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new DataRetrieverTask(MainActivity.this).execute();
			}
		});		
	}

	public void onResume() {
		super.onResume();
		refreshView();
	}

	public void refreshView() {
		dba = new DBAdapter(this);
		dba.open();
		Cursor curBanks = dba.fetchBanks();
		Cursor curAccounts;

		if (curBanks != null && !curBanks.isLast() && !curBanks.isAfterLast()) {
			findViewById(R.id.txtAccountsDesc).setVisibility(View.GONE);
			findViewById(R.id.btnAccountsRefresh).setVisibility(View.VISIBLE);
			startManagingCursor(curBanks);
			ListView lv = (ListView)findViewById(R.id.lstAccountsList);
			AccountsAdapter adapter = new AccountsAdapter(this);

			int clmId = curBanks.getColumnIndex("_id"); 
			int clmBanktype = curBanks.getColumnIndex("banktype");
			int clmUsername = curBanks.getColumnIndex("username");
			int clmBankbalance = curBanks.getColumnIndex("balance");
			int clmDisabled = curBanks.getColumnIndex("disabled");
			List<Item> items;
			Group bank;

			while (!curBanks.isLast() && !curBanks.isAfterLast()) {
				items = new ArrayList<Item>();
				curBanks.moveToNext();
				curAccounts = dba.fetchAccounts(curBanks.getLong(clmId));
				int clmBalance = curAccounts.getColumnIndex("balance"); 
				int clmName = curAccounts.getColumnIndex("name");
				int clmAccId = curAccounts.getColumnIndex("id");
				int acc_count = curAccounts.getCount();
				if (curAccounts != null && acc_count != 0) {
					while (!curAccounts.isLast()) {
						curAccounts.moveToNext();
						items.add(new Item(curAccounts.getString(clmName), curAccounts.getDouble(clmBalance),curAccounts.getString(clmAccId)));
					}
				}
				bank = new Group(curBanks.getString(clmUsername), curBanks.getString(clmBanktype), curBanks.getDouble(clmBankbalance), items, (curBanks.getInt(clmDisabled) == 1 ? true : false));
				adapter.addGroup(bank);
				curAccounts.close();
			}
			lv.setAdapter(adapter);
		}
		curBanks.close();
		dba.close();
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.menu, menu);
		return true;
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.about);
		dialog.setTitle(getString(R.string.about));
		return dialog;
	}

	public boolean onOptionsItemSelected (MenuItem item){
		Intent intent;
		switch (item.getItemId()) {
		case R.id.exit:
			this.finish();
			return true;
		case R.id.accounts:
			intent = new Intent(this, AccountsActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.settings:
			intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.about:
			showDialog(0);
			return true;
		}
		return false;
	}

	public void onDestroy() {
		super.onDestroy();
	}



}