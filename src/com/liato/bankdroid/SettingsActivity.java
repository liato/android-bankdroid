package com.liato.bankdroid;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends LockableActivity implements OnClickListener, OnItemSelectedListener {
	private SharedPreferences prefs;
	private Integer refreshrate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		ArrayList<Pair<String, Integer>> items = new ArrayList<Pair<String, Integer>>();
		items.add(new Pair<String, Integer>(getString(R.string.disabled), -1));
		items.add(new Pair<String, Integer>("15 "+getString(R.string.minutes), 15));
		items.add(new Pair<String, Integer>("30 "+getString(R.string.minutes), 30));
		items.add(new Pair<String, Integer>("1 "+getString(R.string.hour), 60));
		items.add(new Pair<String, Integer>("2 "+getString(R.string.hours), 60*2));
		items.add(new Pair<String, Integer>("4 "+getString(R.string.hours), 60*4));
		items.add(new Pair<String, Integer>("8 "+getString(R.string.hours), 60*8));
		items.add(new Pair<String, Integer>("16 "+getString(R.string.hours), 60*16));
		items.add(new Pair<String, Integer>(getString(R.string.daily), 60*24));

		Spinner spnFrequency = (Spinner)findViewById(R.id.spnUpdateFrequency);
		ArrayAdapter<Pair<String, Integer>> adapter = new ArrayAdapter<Pair<String, Integer>>(this,
				android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnFrequency.setAdapter(adapter);
		int refreshrate = prefs.getInt("refreshrate", -1);
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getValue() == refreshrate) {
				spnFrequency.setSelection(i);
				break;
			}
			
		}
		spnFrequency.setOnItemSelectedListener(this);

		findViewById(R.id.btnSettingsCancel).setOnClickListener(this);
		findViewById(R.id.btnSettingsOk).setOnClickListener(this);
		findViewById(R.id.chkNotifyOnChange).setOnClickListener(this);
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
			if (!(((EditText)findViewById(R.id.edtAccessCode)).getText().toString().equals(((EditText)findViewById(R.id.edtAccessCodeRepeat)).getText().toString()))) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getText(R.string.passwords_mismatch)).setTitle(getText(R.string.passwords_mismatch_title))
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
				Editor editor = prefs.edit();
				editor.putString("access_code", ((EditText)findViewById(R.id.edtAccessCode)).getText().toString());
				editor.putBoolean("notify_on_change", ((CheckBox)findViewById(R.id.chkNotifyOnChange)).isChecked());
				editor.putBoolean("notify_with_sound", ((CheckBox)findViewById(R.id.chkWithSound)).isChecked());
                editor.putBoolean("notify_with_vibration", ((CheckBox)findViewById(R.id.chkWithVibration)).isChecked());
                editor.putBoolean("notify_for_deposit", ((CheckBox)findViewById(R.id.chkDeposit)).isChecked());
                editor.putBoolean("notify_for_funds", ((CheckBox)findViewById(R.id.chkFunds)).isChecked());
                editor.putBoolean("notify_for_loans", ((CheckBox)findViewById(R.id.chkLoans)).isChecked());
                editor.putBoolean("notify_for_ccards", ((CheckBox)findViewById(R.id.chkCCards)).isChecked());
                editor.putBoolean("notify_for_other", ((CheckBox)findViewById(R.id.chkOther)).isChecked());
                editor.putInt("refreshrate", refreshrate);
				editor.commit();
				StartupReceiver.setAlarm(this);
				this.finish();
			}
		}
		else if (v.getId() == R.id.chkNotifyOnChange) {
			findViewById(R.id.chkWithSound).setEnabled(((CheckBox)v).isChecked());
            findViewById(R.id.chkWithVibration).setEnabled(((CheckBox)v).isChecked());
            findViewById(R.id.chkDeposit).setEnabled(((CheckBox)v).isChecked());
            findViewById(R.id.chkFunds).setEnabled(((CheckBox)v).isChecked());
            findViewById(R.id.chkLoans).setEnabled(((CheckBox)v).isChecked());
            findViewById(R.id.chkCCards).setEnabled(((CheckBox)v).isChecked());
            findViewById(R.id.chkOther).setEnabled(((CheckBox)v).isChecked());
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
		Pair<String, Integer> pair = (Pair<String, Integer>)parentView.getItemAtPosition(pos);
		refreshrate = pair.getValue();
	}
	
	
	@Override
	public void onNothingSelected(AdapterView<?> arg) {
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		((EditText)findViewById(R.id.edtAccessCode)).setText(prefs.getString("access_code", ""));
		((EditText)findViewById(R.id.edtAccessCodeRepeat)).setText(prefs.getString("access_code", ""));
		((CheckBox)findViewById(R.id.chkNotifyOnChange)).setChecked(prefs.getBoolean("notify_on_change", true));
		((CheckBox)findViewById(R.id.chkWithSound)).setChecked(prefs.getBoolean("notify_with_sound", true));
		((CheckBox)findViewById(R.id.chkWithSound)).setEnabled(prefs.getBoolean("notify_on_change", true));
		((CheckBox)findViewById(R.id.chkWithVibration)).setChecked(prefs.getBoolean("notify_with_vibration", true));
		((CheckBox)findViewById(R.id.chkWithVibration)).setEnabled(prefs.getBoolean("notify_on_change", true));
        ((CheckBox)findViewById(R.id.chkDeposit)).setChecked(prefs.getBoolean("notify_for_deposit", true));
        ((CheckBox)findViewById(R.id.chkDeposit)).setEnabled(prefs.getBoolean("notify_on_change", true));
        ((CheckBox)findViewById(R.id.chkFunds)).setChecked(prefs.getBoolean("notify_for_funds", false));
        ((CheckBox)findViewById(R.id.chkFunds)).setEnabled(prefs.getBoolean("notify_on_change", true));
        ((CheckBox)findViewById(R.id.chkLoans)).setChecked(prefs.getBoolean("notify_for_loans", false));
        ((CheckBox)findViewById(R.id.chkLoans)).setEnabled(prefs.getBoolean("notify_on_change", true));
        ((CheckBox)findViewById(R.id.chkCCards)).setChecked(prefs.getBoolean("notify_for_ccards", true));
        ((CheckBox)findViewById(R.id.chkCCards)).setEnabled(prefs.getBoolean("notify_on_change", true));
        ((CheckBox)findViewById(R.id.chkOther)).setChecked(prefs.getBoolean("notify_for_other", false));
        ((CheckBox)findViewById(R.id.chkOther)).setEnabled(prefs.getBoolean("notify_on_change", true));
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
