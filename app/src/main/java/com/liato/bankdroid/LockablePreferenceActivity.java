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

import com.liato.bankdroid.lockpattern.ConfirmLockPattern;
import com.liato.bankdroid.lockpattern.LockPatternUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

public class LockablePreferenceActivity extends PreferenceActivity {

    private static final int PATTERNLOCK_UNLOCK = 42;

    private SharedPreferences mPrefs;

    private LockPatternUtils mLockPatternUtils;

    private boolean mHasLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mLockPatternUtils = new LockPatternUtils(this);
        mLockPatternUtils
                .setVisiblePatternEnabled(mPrefs.getBoolean("patternlock_visible_pattern", true));
        mLockPatternUtils.setTactileFeedbackEnabled(
                mPrefs.getBoolean("patternlock_tactile_feedback", false));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't do anything if no lock pattern is set
        if (!mLockPatternUtils.isLockPatternEnabled()) {
            return;
        }
        /*
        Save the current time If a lock pattern has been set
        If this activity never loaded set the lock time to
        10 seconds ago.
        This is to prevent the following scenario:
            1. Activity Starts
            2. Lock screen is displayed
            3. User presses the home button
            4. "lock time" is set in onPause to when the home button was pressed
            5. Activity is started again within 2 seconds and no lock screen is shown this time.
        */
        if (mHasLoaded) {
            writeLockTime();
        } else {
            writeLockTime(SystemClock.elapsedRealtime() - 10000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Don't do anything if lock pattern is not set
        if (!mLockPatternUtils.isLockPatternEnabled() || !isLockEnabled()) {
            return;
        }
        // If a lock pattern is set we need to check the time for when the last
        // activity was open. If it's been more than two seconds the user
        // will have to enter the lock pattern to continue.
        long currentTime = SystemClock.elapsedRealtime();
        long lockedAt = mPrefs.getLong("locked_at", currentTime - 10000);
        long timedif = Math.abs(currentTime - lockedAt);
        if (timedif > 2000) {
            launchPatternLock();
        } else {
            mHasLoaded = true;
        }
    }

    private void launchPatternLock() {
        Intent intent = new Intent(this, ConfirmLockPattern.class);
        intent.putExtra(ConfirmLockPattern.HEADER_TEXT, getText(R.string.patternlock_header));
        startActivityForResult(intent, PATTERNLOCK_UNLOCK);
    }

    private void writeLockTime() {
        writeLockTime(SystemClock.elapsedRealtime());
    }

    private void writeLockTime(long time) {
        Editor editor = mPrefs.edit();
        editor.putLong("locked_at", time);
        editor.apply();
    }

    protected boolean isLockEnabled() {
        return mPrefs.getBoolean("lock_enabled", true);
    }

    protected void setLockEnabled(boolean enabled) {
        Editor editor = mPrefs.edit();
        editor.putBoolean("lock_enabled", enabled);
        editor.apply();
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PATTERNLOCK_UNLOCK) {
            if (resultCode == RESULT_OK) {
                writeLockTime();
            } else {
                launchPatternLock();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setLockEnabled(true);
    }

}
