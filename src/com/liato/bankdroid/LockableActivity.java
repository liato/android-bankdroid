package com.liato.bankdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class LockableActivity extends Activity {
	private SharedPreferences prefs;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isProtected()) return;
		editor = prefs.edit();
		editor.putLong("locked_at", System.currentTimeMillis());
		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isProtected()) return;
		long currentTime = System.currentTimeMillis();
		long lockedAt = prefs.getLong("locked_at", currentTime-10000);
		long timedif = currentTime - lockedAt;
		if (timedif > 3000) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	private boolean isProtected() {
		String access_code = prefs.getString("access_code", "");
		if (access_code.length() > 0) return true;
		return false;
		
	}

}
