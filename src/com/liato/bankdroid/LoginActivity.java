package com.liato.bankdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	private SharedPreferences prefs;
	private String access_code;

	public void onCreate(Bundle savedInstanceState) {
		String widgetAction = getIntent().getAction();
		if (!widgetAction.equals("widgetLogin")) {
		    setTheme(android.R.style.Theme);
		}
		super.onCreate(savedInstanceState);
	
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		access_code = prefs.getString("access_code", "");
		setResult(Activity.RESULT_CANCELED); // default
		if (access_code.length() > 0) {
			setContentView(R.layout.login);
			((Button)findViewById(R.id.btnLogin)).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (((EditText)findViewById(R.id.edtAccessCode)).getText().toString().equals(access_code)) {
						Log.d("LoginActivity", "Success");
						String widgetAction = getIntent().getAction();
						if (widgetAction.equals("widgetLogin")) {
							Log.d("LoginActivity", "unLockOnly");
							setResult(Activity.RESULT_OK);
							finish();
						} else {
							loginSuccess();
						}
					}
					else {
						TextView txtResult = (TextView)findViewById(R.id.txtResult);
				    	txtResult.setVisibility(TextView.VISIBLE);
				    	AlphaAnimation anim = new AlphaAnimation(1, 0);
				    	anim.setFillAfter(true);
				    	anim.setStartOffset(5000);
					    anim.setDuration(1000);
				    	txtResult.startAnimation(anim);
				 
					}
				}
			});	
		}
		else {
			Log.d("LoginActivity", widgetAction);
			if (widgetAction.equals("widgetLogin")) {
				Log.d("LoginActivity", "unLockOnly");
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				loginSuccess();
			}
		}
	}
	
	private void loginSuccess() {
		unLock();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		this.finish();
	}
	
	private void unLock() {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putLong("locked_at", System.currentTimeMillis());
		editor.commit();
	}
}
