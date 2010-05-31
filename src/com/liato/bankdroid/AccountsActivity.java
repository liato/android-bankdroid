package com.liato.bankdroid;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class AccountsActivity extends LockableActivity {
	private DBAdapter dba;
	private Cursor c;
	private ListView lv;
	private Resources res;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.banks);
		Button btnNewacc = (Button)findViewById(R.id.btnBanksNewaccount);
		res = this.getResources();
		btnNewacc.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intentAccount = new Intent(AccountsActivity.this, AccountActivity.class);
				startActivity(intentAccount);
			}
		});
	}

	public void onResume() {
		super.onResume();
		lv = (ListView)findViewById(R.id.lstvBanksList);

		refreshView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.showContextMenu();
			}
		});
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
				menu.setHeaderTitle(((TextView)info.targetView.findViewById(R.id.txtListitemBankname)).getText());
				menu.add(0, 0, 0, res.getText(R.string.menu_edit));
				menu.add(0, 1, 0, res.getText(R.string.menu_remove));
			}
		});
	}

	private void refreshView() {
		dba = new DBAdapter(this);
		dba.open();
		c = dba.fetchBanks();
		if (c != null && !c.isLast() && !c.isAfterLast()) {
			findViewById(R.id.txtBanksDesc).setVisibility(View.GONE);
			startManagingCursor(c);
			BanksCursorAdapter bca = new BanksCursorAdapter(this, c);
			lv.setAdapter(bca);
		}
		dba.close();
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		View v = (View)info.targetView;

		switch (item.getItemId()) { 
		case 0: 
			Intent intent = new Intent(AccountsActivity.this, AccountActivity.class);
			intent.putExtra("id", v.getTag().toString());
			startActivity(intent);
			return true; 
		case 1: 
			DBAdapter db = new DBAdapter(this);
			db.open();
			db.deleteBank(new Long(v.getTag().toString()));
			db.close();
			refreshView();
			return true; 
		} 
		return false; 
	}	

	public void onDestroy() {
		if (!c.isClosed()) {
			c.close();
		}
		dba.close();
		super.onDestroy();
	}
	private class BanksCursorAdapter extends CursorAdapter {
		public BanksCursorAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void bindView(View view, Context context, Cursor cursor) {
			ImageView icon = (ImageView)view.findViewById(R.id.imgListitemBanks);
			((TextView)view.findViewById(R.id.txtListitemAccountname)).setText(cursor.getString(cursor.getColumnIndex("username")));
			((TextView)view.findViewById(R.id.txtListitemBankname)).setText(cursor.getString(cursor.getColumnIndex("banktype")));
			icon.setImageResource(getResources().getIdentifier("drawable/"+Helpers.toAscii(cursor.getString(cursor.getColumnIndex("banktype")).toLowerCase()), null, getPackageName()));
			view.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
			ImageView warning = (ImageView)view.findViewById(R.id.imgWarning);
			if (cursor.getInt(cursor.getColumnIndex("disabled")) == 1 ? true : false) {
				warning.setVisibility(View.VISIBLE);
			}
			else {
				warning.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.listitem_banks, parent, false);
			bindView(v, context, cursor);
			return v;
		}
	}
}
