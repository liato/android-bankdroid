package com.liato.bankdroid;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends LockableActivity {
	private final static String TAG = "MainActivity";
	protected AccountsAdapter adapter = null;
	private static Bank selected_bank = null;

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
		Button btnAddBank = (Button)findViewById(R.id.btnAddBank);
		btnAddBank.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intentAccount = new Intent(MainActivity.this, AccountActivity.class);
				startActivity(intentAccount);
			}
		});

		ListView lv = (ListView)findViewById(R.id.lstAccountsList);
		adapter = new AccountsAdapter(this);
		ArrayList<Bank> banks = new ArrayList<Bank>();//BankFactory.banksFromDb(this, true);
		adapter.setGroups(banks);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//view.showContextMenu();
				Log.d("itemclick", "Parent: "+parent+ "; View: "+view+"; Pos: "+position+"; ID: "+id);
				if (adapter.getItem(position) instanceof Bank) {
					selected_bank = (Bank) adapter.getItem(position);
					PopupMenu pmenu = new PopupMenu(view, MainActivity.this);
					pmenu.showLikeQuickAction(0, 12);					
				}
				else {
					Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
					Account account = (Account) adapter.getItem(position);
					intent.putExtra("account", account.getId());
					intent.putExtra("bank", account.getBankDbId());
					MainActivity.this.startActivity(intent);
				}
			}
		});
	}

	public void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(AutoRefreshService.BROADCAST_MAIN_REFRESH));		
		refreshView();
	}

	private BroadcastReceiver receiver=new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			refreshView();
		}
	};	
	
	public void refreshView() {
		Log.d(TAG, "refreshView()");
		ArrayList<Bank> banks = BankFactory.banksFromDb(this, true);
		Log.d(TAG, "Bank count: "+banks.size());
		if (banks.size() > 0) {
			findViewById(R.id.txtAccountsDesc).setVisibility(View.GONE);
			//findViewById(R.id.btnAccountsRefresh).setClickable(true);
		}
		else {
			findViewById(R.id.txtAccountsDesc).setVisibility(View.VISIBLE);
			//findViewById(R.id.btnAccountsRefresh).setClickable(false);
		}

		adapter.setGroups(banks);
		adapter.notifyDataSetChanged();
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
		PackageInfo pInfo;
		String version = "v1.x.x";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			version = pInfo.versionName;		
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		((TextView)dialog.findViewById(R.id.txtVersion)).setText(getText(R.string.version).toString().replace("$version", version));
		return dialog;
	}

	public boolean onOptionsItemSelected (MenuItem item){
		Intent intent;
		switch (item.getItemId()) {
		case R.id.exit:
			this.finish();
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

	/**
	 * Extends {@link BetterPopupWindow}
	 * <p>
	 * Overrides onCreate to create the view and register the button listeners
	 * 
	 * @author qbert
	 * 
	 */
	private static class PopupMenu extends BetterPopupWindow implements OnClickListener {
		MainActivity parent = null;
		public PopupMenu(View anchor, MainActivity parent) {
			super(anchor);
			this.parent = parent;
		}

		@Override
		protected void onCreate() {
			// inflate layout
			LayoutInflater inflater =
				(LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			ViewGroup root = (ViewGroup) inflater.inflate(R.layout.popup, null);
			root.findViewById(R.id.btnRefresh).setOnClickListener(this);
			root.findViewById(R.id.btnWWW).setOnClickListener(this);
			root.findViewById(R.id.btnEdit).setOnClickListener(this);
			root.findViewById(R.id.btnRemove).setOnClickListener(this);


			this.setContentView(root);
		}

		@Override
		public void onClick(View v) {
			final Context context = this.anchor.getContext();
			int id = v.getId();
			switch (id) {
			case R.id.btnWWW:
				if (selected_bank != null) {
					Uri uri = Uri.parse(selected_bank.getURL());
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					context.startActivity(intent);
				}
				this.dismiss();
				return; 
			case R.id.btnEdit:
				Intent intent = new Intent(context, AccountActivity.class);
				intent.putExtra("id", selected_bank.getDbId());
				context.startActivity(intent);
				this.dismiss();
				return; 
			case R.id.btnRefresh:
				this.dismiss();
				new DataRetrieverTask(parent, selected_bank.getDbId()).execute();
				return; 
			case R.id.btnRemove:
				this.dismiss();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				//builder.setMessage(getText(R.string.passwords_mismatch)).setTitle(getText(R.string.passwords_mismatch_title))
				builder.setMessage(context.getText(R.string.remove_bank_msg)).setTitle(context.getText(R.string.remove_bank_title))
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(context.getText(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						DBAdapter db = new DBAdapter(context);
						db.open();
						db.deleteBank(selected_bank.getDbId());
						db.close();
						dialog.cancel();
						parent.refreshView();
					}
				})
				.setNegativeButton(context.getText(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});				
				AlertDialog alert = builder.create();
				alert.show();
				return;
			}

		}
	}

}