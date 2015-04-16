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

import com.liato.bankdroid.appwidget.AutoRefreshService;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.nordea.Nordea;
import com.liato.bankdroid.banking.banks.swedbank.Swedbank;
import com.liato.bankdroid.lockpattern.ChooseLockPattern;
import com.liato.bankdroid.lockpattern.ConfirmLockPattern;
import com.liato.bankdroid.lockpattern.LockPatternUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.math.BigDecimal;

public class SettingsActivity extends LockablePreferenceActivity
        implements OnPreferenceClickListener, OnPreferenceChangeListener {

    private final static String TAG = "SettingsActivity";

    private final static int DISABLE_LOCKPATTERN = 1;

    private final static int ENABLE_LOCKPATTERN = 2;

    private final static int CHANGE_LOCKPATTERN = 3;

    private LockPatternUtils mLockPatternUtils;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        addPreferencesFromResource(R.xml.settings);
        getWindow().setBackgroundDrawableResource(R.drawable.background_repeat);
        (findPreference("patternlock_change")).setOnPreferenceClickListener(this);
        (findPreference("remotenotifier_help")).setOnPreferenceClickListener(this);
        (findPreference("openwatch_help")).setOnPreferenceClickListener(this);
        (findPreference("liveview_help")).setOnPreferenceClickListener(this);
        (findPreference("account_types_screen")).setOnPreferenceClickListener(this);
        (findPreference("remotenotifier_screen")).setOnPreferenceClickListener(this);
        (findPreference("openwatch_screen")).setOnPreferenceClickListener(this);
        (findPreference("liveview_screen")).setOnPreferenceClickListener(this);
        (findPreference("notification_sound")).setOnPreferenceClickListener(this);
        (findPreference("test_notification")).setOnPreferenceClickListener(this);
        (findPreference("notify_min_delta")).setOnPreferenceChangeListener(this);
        final CheckBoxPreference patternLock = ((CheckBoxPreference) findPreference(
                "patternlock_enabled"));
        patternLock.setOnPreferenceClickListener(this);
        // Check the pattern lock check box if the lock pattern is enabled
        patternLock.setChecked(mLockPatternUtils.isLockPatternEnabled());
    }

    @Override
    public boolean onPreferenceClick(final Preference pref) {
        final String prefKey = pref.getKey();
        if ("account_types_screen".equals(prefKey) ||
                "remotenotifier_screen".equals(prefKey) ||
                "openwatch_screen".equals(prefKey) ||
                "liveview_screen".equals(prefKey)) {
            ((PreferenceScreen) pref).getDialog().getWindow()
                    .setBackgroundDrawableResource(R.drawable.background_repeat);
            return false;
        }

        if ("notification_sound".equals(prefKey)) {
            return false;
        }

        if ("patternlock_enabled".equals(prefKey)) {
            this.setLockEnabled(false);
            if (mLockPatternUtils.isLockPatternEnabled()) {
                // The user is trying to disable the lock pattern,
                // only disable if the user knows the pattern.
                startActivityForResult(new Intent(this, ConfirmLockPattern.class),
                        DISABLE_LOCKPATTERN);
                return true;
            } else {
                // No lock pattern has been set yet, let the user choose a new one.
                startActivityForResult(new Intent(this, ChooseLockPattern.class),
                        ENABLE_LOCKPATTERN);
                return true;
            }
        } else if ("patternlock_change".equals(prefKey)) {
            this.setLockEnabled(false);
            startActivityForResult(new Intent(this, ChooseLockPattern.class), CHANGE_LOCKPATTERN);
            return true;
        } else if ("remotenotifier_help".equals(prefKey)) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://code.google.com/p/android-notifier/")));
            return true;
        } else if ("openwatch_help".equals(prefKey)) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://forum.xda-developers.com/showthread.php?t=554551")));
            return true;
        } else if ("liveview_help".equals(prefKey)) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            "http://www.sonyericsson.com/cws/products/accessories/overview/liveviewmicrodisplay")));
            return true;
        } else if ("test_notification".equals(prefKey)) {
            Log.d(TAG, "Sending test notification.");
            Account account1 = new Account("Personkonto", new BigDecimal(8351.00), "22");
            Bank bank1 = new Swedbank(this);
            bank1.setDbid(21);
            bank1.setCustomName("800416-0001");

            Account account2 = new Account("Personkonto", new BigDecimal(8341.00), "23");
            Bank bank2 = new Nordea(this);
            bank2.setDbid(22);
            bank2.setCustomName("800416-0002");

            AutoRefreshService.showNotification(bank1, account1, new BigDecimal(-143.50), this);
            AutoRefreshService.showNotification(bank2, account2, new BigDecimal(-123.50), this);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISABLE_LOCKPATTERN) {
            if (resultCode == RESULT_OK) {
                mLockPatternUtils.setLockPatternEnabled(false);
                mLockPatternUtils.saveLockPattern(null);
                ((CheckBoxPreference) findPreference("patternlock_enabled")).setChecked(false);
            } else {
                Log.d(TAG, "User was unable to disable pattern lock.");
            }
        } else if (requestCode == ENABLE_LOCKPATTERN) {
            // User attempted to enable the pattern lock, toggle the checkbox.
            ((CheckBoxPreference) findPreference("patternlock_enabled"))
                    .setChecked(mLockPatternUtils.isLockPatternEnabled());
        } else if (requestCode == CHANGE_LOCKPATTERN) {
            // Don't do anything special
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setLockEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StartupReceiver.setAlarm(this);
        // Blur/unblur the widget balance
        AutoRefreshService.sendWidgetRefresh(this);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        final String prefKey = pref.getKey();
        if ("notify_min_delta".equals(prefKey)) {
            Integer val;
            try {
                val = Integer.valueOf((String) newValue);
            } catch (NumberFormatException e) {
                val = null;
            }

            if (val != null && val >= 0) {
                return true;
            } else {
                Toast.makeText(pref.getContext(),
                        String.format(pref.getContext().getString(R.string.invalid_integer),
                                newValue), Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return false;
    }

}
