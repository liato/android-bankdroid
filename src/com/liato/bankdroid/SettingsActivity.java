package com.liato.bankdroid;

import java.util.ArrayList;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		ArrayList<Pair<String, Integer>> items = new ArrayList<Pair<String, Integer>>();
		items.add(new Pair<String, Integer>("Disabled", -1));
		items.add(new Pair<String, Integer>("15 minutes", 10));
		items.add(new Pair<String, Integer>("30 minutes", 30));
		items.add(new Pair<String, Integer>("1 hour", 60));
		items.add(new Pair<String, Integer>("2 hours", 60*2));
		items.add(new Pair<String, Integer>("4 hours", 60*4));
		items.add(new Pair<String, Integer>("8 hours", 60*8));

		Spinner spnFrequency = (Spinner)findViewById(R.id.spnUpdateFrequency);
		ArrayAdapter<Pair<String, Integer>> adapter = new ArrayAdapter<Pair<String, Integer>>(this,
				android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnFrequency.setAdapter(adapter);
		spnFrequency.setOnItemSelectedListener(this);

		findViewById(R.id.btnSettingsCancel).setOnClickListener(this);
		findViewById(R.id.btnSettingsOk).setOnClickListener(this);
		

		((EditText)findViewById(R.id.edtAccessCode)).setText(prefs.getString("access_code", ""));
		//Resources r = getResources();

		/*Bundle extras = getIntent().getExtras(); 
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
		}*/
	}
	
	private class Pair<T, S> {
		private T key;
		private S value;
		public Pair(T key, S value) { 
			this.key = key;
			this.value = value;   
		}

		public T getKey() {
			return key;
		}

		public S getValue() {
			return value;
		}

		public String toString() { 
			return (String) getKey(); 
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnSettingsCancel) {
			this.finish();
		}
		else if (v.getId() == R.id.btnSettingsOk){
			Editor editor = prefs.edit();
			editor.putString("access_code", ((EditText)findViewById(R.id.edtAccessCode)).getText().toString());
			editor.commit();
			this.finish();
			//new DataRetrieverTask(this).execute(SELECTED_BANK, ((EditText) findViewById(R.id.edtBankeditUsername)).getText().toString().trim(), ((EditText) findViewById(R.id.edtBankeditPassword)).getText().toString().trim());
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
		//SELECTED_BANK = parentView.getItemAtPosition(pos).toString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg) {
	}

	/*private class DataRetrieverTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
		private Class<?> cls;
		private Exception exc = null;
		private Bank bank;
		private SettingsActivity parent;
		private Resources res;

		public DataRetrieverTask(SettingsActivity parent) {
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
				DBAdapter dba = new DBAdapter(SettingsActivity.this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
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
	}*/
	
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
