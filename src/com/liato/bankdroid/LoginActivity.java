package com.liato.bankdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	private SharedPreferences prefs;
	private String access_code;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		access_code = prefs.getString("access_code", "");
		if (access_code.length() > 0) {
			setContentView(R.layout.login);
			((Button)findViewById(R.id.btnLogin)).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (((EditText)findViewById(R.id.edtAccessCode)).getText().toString().equals(access_code)) {
						loginSuccess();
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
			loginSuccess();
		}
	}
	
	private void loginSuccess() {
		Intent intent = new Intent(this, MainActivity.class);
		this.startActivity(intent);
		this.finish();
		return;
	}
}
