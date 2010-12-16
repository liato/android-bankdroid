/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liato.bankdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class LockableActivity extends Activity {
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
