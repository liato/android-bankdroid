package com.liato.bankdroid;

import com.liato.bankdroid.LockPatternUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class LockableActivity extends Activity {
    private static String TAG = "LockableActivity";
    private static int PATTERNLOCK_UNLOCK = 42;
	private SharedPreferences prefs;
	private Editor editor;
	private LockPatternUtils mLockPatternUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mLockPatternUtils = new LockPatternUtils(this);		
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Don't do anything if not lock pattern is set
		if (!mLockPatternUtils.isLockPatternEnabled()) return;
        // Save the current time If a lock pattern has been set
		writeLockTime();
	}

	@Override
	protected void onResume() {
		super.onResume();
        // Don't do anything if not lock pattern is set
		if (!mLockPatternUtils.isLockPatternEnabled()) {
		    return;
		}
		// If a lock pattern is set we need to check the time for when the last
		// activity was open. If it's been more than two seconds the user
		// will have to enter the lock pattern to continue.
		long currentTime = System.currentTimeMillis();
		long lockedAt = prefs.getLong("locked_at", currentTime-10000);
		long timedif = currentTime - lockedAt;
		if (timedif > 2000) {
		    launchPatternLock();
		}
	}

	private void launchPatternLock() {
        Intent intent = new Intent(this, ConfirmLockPattern.class);
        intent.putExtra(ConfirmLockPattern.DISABLE_BACK_KEY, true);
        intent.putExtra(ConfirmLockPattern.HEADER_TEXT, getText(R.string.patternlock_header));
        startActivityForResult(intent, PATTERNLOCK_UNLOCK);         
	}
	
	private void writeLockTime() {
        editor = prefs.edit();
        editor.putLong("locked_at", System.currentTimeMillis());
        editor.commit();	    
	}
	
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == PATTERNLOCK_UNLOCK) {
            if (resultCode == RESULT_OK) {
                writeLockTime();
            }
            else {
                launchPatternLock();
            }
        }
    }   	
}
