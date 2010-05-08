package com.liato.bankdroid;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class AccountActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	private String SELECTED_BANK;
	private String BANKID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bank);
		ArrayList<String> items = new ArrayList<String>();
		for(Banks bank: Banks.values()) {
			items.add(bank.toString());
		}
		Collections.sort(items);
		Spinner spnBanks = (Spinner)findViewById(R.id.spnBankeditBanklist);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnBanks.setAdapter(adapter);
		spnBanks.setOnItemSelectedListener(this);

		findViewById(R.id.btnSettingsCancel).setOnClickListener(this);
		findViewById(R.id.btnSettingsOk).setOnClickListener(this);

		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			BANKID = extras.getString("id");
			if (BANKID != null) {
				DBAdapter db = new DBAdapter(this);
				db.open();
				Cursor c = db.getBank(BANKID);
				if (c != null) {
					((EditText)findViewById(R.id.edtBankeditUsername)).setText(c.getString(c.getColumnIndex("username")));
					((EditText)findViewById(R.id.edtBankeditPassword)).setText(c.getString(c.getColumnIndex("password")));
					SELECTED_BANK = c.getString(c.getColumnIndex("banktype"));
					int i = items.indexOf(SELECTED_BANK);
					spnBanks.setSelection(i);
					c.close();
				}
				db.close();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnSettingsCancel) {
			this.finish();
		}
		else if (v.getId() == R.id.btnSettingsOk){
			new DataRetrieverTask(this).execute(SELECTED_BANK, ((EditText) findViewById(R.id.edtBankeditUsername)).getText().toString().trim(), ((EditText) findViewById(R.id.edtBankeditPassword)).getText().toString().trim());
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
		SELECTED_BANK = parentView.getItemAtPosition(pos).toString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg) {
	}

	private class DataRetrieverTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(AccountActivity.this);
		private Class<?> cls;
		private Exception exc = null;
		private Bank bank;
		private AccountActivity parent;
		private Resources res;

		public DataRetrieverTask(AccountActivity parent) {
			this.parent = parent;
			this.res = parent.getResources();
			
		}
		protected void onPreExecute() {
			this.dialog.setMessage(res.getText(R.string.logging_in));
			this.dialog.show();
		}

		protected Void doInBackground(final String... args) {
			try {
				cls = Class.forName("com.liato.bankdroid.Bank"+args[0]);
				bank = (Bank) cls.newInstance();
				bank.update(args[1], args[2]);
				DBAdapter dba = new DBAdapter(AccountActivity.this);
				dba.open();
				if (BANKID != null) {
					dba.updateBank(bank, new Long(BANKID));
				}
				else {
					dba.createBank(bank);
				}
				dba.close();
			} 
			catch (BankException e) {
				this.exc = e;
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
			return null;
		}

		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (this.exc != null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
				builder.setMessage(this.exc.getMessage()).setTitle(res.getText(R.string.could_not_create_account))
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			else {
				parent.finish();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}   
}
