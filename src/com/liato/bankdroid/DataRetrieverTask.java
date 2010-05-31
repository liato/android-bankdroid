package com.liato.bankdroid;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class DataRetrieverTask extends AsyncTask<String, String, Void> {
	private ProgressDialog dialog;
	private Class<?> cls;
	private ArrayList<String> errors;
	private Bank bank;
	private MainActivity parent;
	private int bankcount;
	private Resources res;

	public DataRetrieverTask(MainActivity parent) {
		this.parent = parent;
		this.res = parent.getResources();
		this.dialog =  new ProgressDialog(parent);
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
		int clmDisabled = c.getColumnIndex("disabled");
		int i = 0; 
		while (!c.isLast() && !c.isAfterLast()) {
			c.moveToNext();
			publishProgress(new String[] {new Integer(i).toString(), c.getString(clmBanktype)+" ("+c.getString(clmUsername)+")"});
			if (c.getInt(clmDisabled) == 1) {
				Log.d("AA", c.getString(clmBanktype)+" ("+c.getString(clmUsername)+") is disabled. Skipping refresh.");
				continue;
			}
			Log.d("AA", "Refreshing "+c.getString(clmBanktype)+" ("+c.getString(clmUsername)+").");
			try {				cls = Class.forName("com.liato.bankdroid.Bank"+Helpers.toAscii(c.getString(clmBanktype)));
				bank = (Bank) cls.newInstance();
				bank.update(c.getString(clmUsername), c.getString(clmPassword), parent);
				db.updateBank(bank, new Long(c.getString(clmId)));
				i++;
			} 
			catch (BankException e) {
				this.errors.add(c.getString(clmBanktype)+" ("+c.getString(clmUsername));
				db.disableBank(c.getLong(clmId));
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
		c.close();
		db.close();
		publishProgress(new String[] {new Integer(i).toString(), ""});
		return null;
	}

	protected void onProgressUpdate(String... args) {
		this.dialog.setProgress(new Integer(args[0]));
		this.dialog.setMessage(res.getText(R.string.updating_account_balance)+"\n"+args[1]);
	}
	protected void onPostExecute(final Void unused) {
		parent.refreshView();
		AutoRefreshService.sendWidgetRefresh(parent);
		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}
		
		if (this.errors != null && !this.errors.isEmpty()) {
			StringBuilder errormsg = new StringBuilder();
			errormsg.append(res.getText(R.string.accounts_were_not_updated)+":\n");
			for (String err : errors)
			{
			  errormsg.append(err);
			  errormsg.append("\n");
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(parent);
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
