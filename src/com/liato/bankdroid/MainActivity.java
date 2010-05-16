package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
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

public class MainActivity extends Activity {
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

	private void refreshView() {
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
			List<Item> items;
			Group bank;

			while (!curBanks.isLast() && !curBanks.isAfterLast()) {
				items = new ArrayList<Item>();
				curBanks.moveToNext();
				curAccounts = dba.fetchAccounts(curBanks.getLong(clmId));
				int clmBalance = curAccounts.getColumnIndex("balance"); 
				int clmName = curAccounts.getColumnIndex("name");
				int acc_count = curAccounts.getCount();
				if (curAccounts != null && acc_count != 0) {
					while (!curAccounts.isLast()) {
						curAccounts.moveToNext();
						items.add(new Item(curAccounts.getString(clmName), curAccounts.getDouble(clmBalance)));
					}
				}
				bank = new Group(curBanks.getString(clmUsername), curBanks.getString(clmBanktype), curBanks.getDouble(clmBankbalance), items);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Omnomnomnom")
			.setCancelable(true)
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return false;
	}

	public void onDestroy() {
		super.onDestroy();
	}

	private class AccountsAdapter extends BaseAdapter {
		private ArrayList<Group> groups;
		private Context context;

		public AccountsAdapter(Context context) {
			this.context = context;
			this.groups = new ArrayList<Group>();
		}

		public void addGroup(Group group) {
			groups.add(group);
		}

		public View newGroupView(Group group, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.listitem_accounts_group, parent, false);
			ImageView icon = (ImageView)v.findViewById(R.id.imgListitemAccountsGroup);
			((TextView)v.findViewById(R.id.txtListitemAccountsGroupAccountname)).setText(group.getName());
			((TextView)v.findViewById(R.id.txtListitemAccountsGroupBankname)).setText(group.getType());
			((TextView)v.findViewById(R.id.txtListitemAccountsGroupTotal)).setText(Helpers.formatBalance(group.getTotal()));
			icon.setImageResource(getResources().getIdentifier("drawable/"+group.getType().toLowerCase(), null, getPackageName()));
			return v;
		}

		public View newItemView(Item item, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.listitem_accounts_item, parent, false);
			((TextView)v.findViewById(R.id.txtListitemAccountsItemAccountname)).setText(item.getName());
			((TextView)v.findViewById(R.id.txtListitemAccountsItemBalance)).setText(Helpers.formatBalance(item.getBalance()));
			return v;
		}

		@Override
		public int getCount() {
			int c = 0;
			for(Group g : groups) {
				c += g.getItems().size()+1;
			}
			return c;
		}

		@Override
		public Object getItem(int position) {
			if (groups.size() == 0) {
				return null;
			}
			if (position == 0) {
				return groups.get(0);
			}

			int i = 0;
			for (Group g : groups) {
				if (position == i) {
					return g;
				}
				else if (position <= (g.getItems().size()+i)) {
					return g.getItems().get(position-i-1);
				}
				i += g.getItems().size()+1;
			}

			return(null);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Object item = getItem(position);
			if (item == null) {
				return null;
			}
			if (item instanceof Group) {
				return newGroupView((Group)item, parent);
			}
			else if (item instanceof Item) {
				return newItemView((Item)item, parent);
			}
			return null;
		}

		public boolean isEnabled(int position) {
			return false;
		}		
	}

	private class DataRetrieverTask extends AsyncTask<String, String, Void> {
		private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		private Class<?> cls;
		private ArrayList<String> errors;
		private Bank bank;
		private MainActivity parent;
		private int bankcount;
		private Resources res;

		public DataRetrieverTask(MainActivity parent) {
			this.parent = parent;
			this.res = parent.getResources();
		}
		protected void onPreExecute() {
			this.dialog.setMessage(res.getText(R.string.updating_account_balance)+"\n ");
			this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		protected Void doInBackground(final String... args) {
			errors = new ArrayList<String>();
			DBAdapter db;
			Cursor c;
			db = new DBAdapter(parent);
			db.open();
			c = db.fetchBanks();
			if (c == null) {
				return null;
			}
			bankcount = c.getCount();
			this.dialog.setMax(bankcount);
			int clmId = c.getColumnIndex("_id");
			int clmBanktype = c.getColumnIndex("banktype");
			int clmBalance = c.getColumnIndex("balance");
			int clmUsername = c.getColumnIndex("username");
			int clmPassword = c.getColumnIndex("password");
			int i = 0; 
			while (!c.isLast() && !c.isAfterLast()) {
				c.moveToNext();
				publishProgress(new String[] {new Integer(i).toString(), c.getString(clmBanktype)+" ("+c.getString(clmUsername)+")"});
				try {
					cls = Class.forName("com.liato.bankdroid.Bank"+c.getString(clmBanktype));
					bank = (Bank) cls.newInstance();
					bank.update(c.getString(clmUsername), c.getString(clmPassword));
					db.updateBank(bank, new Long(c.getString(clmId)));
					i++;
				} 
				catch (BankException e) {
					this.errors.add(c.getString(clmBanktype)+" ("+c.getString(clmUsername));
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
			publishProgress(new String[] {new Integer(i).toString(), ""});
			c.close();
			db.close();
			return null;
		}

		protected void onProgressUpdate(String... args) {
			this.dialog.setProgress(new Integer(args[0]));
			this.dialog.setMessage(res.getText(R.string.updating_account_balance)+"\n"+args[1]);
		}
		protected void onPostExecute(final Void unused) {
			parent.refreshView();
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			
			if (this.errors != null && !this.errors.isEmpty()) {
				StringBuilder errormsg = new StringBuilder();
				errormsg.append(res.getText(R.string.acounts_were_not_updated));
				for (String err : errors)
				{
				  errormsg.append(err);
				  errormsg.append("\n");
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(errormsg.toString()).setTitle(res.getText(R.string.errors_when_updating))
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}	
	
	private class Group {
		private String name;
		private String type;
		private BigDecimal total;
		private List<Item> items;
		public Group(String name, String type, Double total, List<Item> items) {
			this.name = name;
			this.type = type;
			this.items = items;
			this.total = new BigDecimal(total);
		}
		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
		public BigDecimal getTotal() {
			return total;
		}
		public List<Item> getItems() {
			return items;
		}

	}

	private class Item {
		private String name;
		private BigDecimal balance;
		public Item (String name, Double balance) {
			this.name = name;
			this.balance = new BigDecimal(balance);
		}
		public String getName() {
			return name;
		}
		public BigDecimal getBalance() {
			return balance;
		}
	}

}