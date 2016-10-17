/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.liato.bankdroid.lockpattern;

import com.google.common.collect.Lists;

import com.liato.bankdroid.utils.StringUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * Utilities for the lock patten and its settings.
 */
public class LockPatternUtils {

    /**
     * The maximum number of incorrect attempts before the user is prevented
     * from trying again for {@link #FAILED_ATTEMPT_TIMEOUT_MS}.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;

    /**
     * The number of incorrect attempts before which we fall back on an alternative
     * method of verifying the user, and resetting their lock pattern.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_RESET = 20;

    /**
     * How long the user is prevented from trying again after entering the
     * wrong pattern too many times.
     */
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000L;

    /**
     * The interval of the countdown for showing progress of the lockout.
     */
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000L;

    /**
     * The minimum number of dots in a valid pattern.
     */
    public static final int MIN_LOCK_PATTERN_SIZE = 4;

    /**
     * The minimum number of dots the user must include in a wrong pattern
     * attempt for it to be counted against the counts that affect
     * {@link #FAILED_ATTEMPTS_BEFORE_TIMEOUT} and {@link #FAILED_ATTEMPTS_BEFORE_RESET}
     */
    public static final int MIN_PATTERN_REGISTER_FAIL = 3;

    private static final String LOCK_PATTERN_FILE = "gesture.key";

    private final static String LOCKOUT_PERMANENT_KEY = "lockscreen.lockedoutpermanently";

    private final static String LOCKOUT_ATTEMPT_DEADLINE = "lockscreen.lockoutattemptdeadline";

    private final static String PATTERN_EVER_CHOSEN = "lockscreen.patterneverchosen";

    private static String sLockPatternFilename;

    private static SharedPreferences mPrefs;

    private final ContentResolver mContentResolver;

    public LockPatternUtils(Context context) {
        mContentResolver = context.getContentResolver();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Initialize the location of gesture lock file
        if (sLockPatternFilename == null) {
            sLockPatternFilename = context.getFilesDir() + LOCK_PATTERN_FILE;
            //sLockPatternFilename = android.os.Environment.getDataDirectory()
            //        .getAbsolutePath() + LOCK_PATTERN_FILE;
        }
    }

    /**
     * Deserialize a pattern.
     *
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        List<LockPatternView.Cell> result = Lists.newArrayList();

        final byte[] bytes = StringUtils.getBytes(string);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     *
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return StringUtils.toString(res);
    }

    /*
     * Generate an SHA-1 hash for the pattern. Not the most secure, but it is
     * at least a second level of protection. First level is that the file
     * is in a location only readable by the system process.
     * @param pattern the gesture pattern.
     * @return the hash of the pattern in a byte array.
     */
    static byte[] patternToHash(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return null;
        }

        final int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(res);
            return hash;
        } catch (NoSuchAlgorithmException nsa) {
            return res;
        }
    }

    /**
     * Check to see if a pattern matches the saved pattern.  If no pattern exists,
     * always returns true.
     *
     * @param pattern The pattern to check.
     * @return Whether the pattern matchees the stored one.
     */
    public boolean checkPattern(List<LockPatternView.Cell> pattern) {
        try {
            // Read all the bytes from the file
            RandomAccessFile raf = new RandomAccessFile(sLockPatternFilename, "r");
            final byte[] stored = new byte[(int) raf.length()];
            int got = raf.read(stored, 0, stored.length);
            raf.close();
            if (got <= 0) {
                return true;
            }
            // Compare the hash from the file with the entered pattern's hash
            return Arrays.equals(stored, LockPatternUtils.patternToHash(pattern));
        } catch (FileNotFoundException fnfe) {
            return true;
        } catch (IOException ioe) {
            return true;
        }
    }

    /**
     * Check to see if the user has stored a lock pattern.
     *
     * @return Whether a saved pattern exists.
     */
    public boolean savedPatternExists() {
        try {
            // Check if we can read a byte from the file
            RandomAccessFile raf = new RandomAccessFile(sLockPatternFilename, "r");
            byte first = raf.readByte();
            raf.close();
            return true;
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Return true if the user has ever chosen a pattern.  This is true even if the pattern is
     * currently cleared.
     *
     * @return True if the user has ever chosen a pattern.
     */
    public boolean isPatternEverChosen() {
        return getBoolean(PATTERN_EVER_CHOSEN);
    }

    /**
     * Save a lock pattern.
     *
     * @param pattern The new pattern to save.
     */
    public void saveLockPattern(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            Timber.d("Removing lock pattern");
        } else {
            Timber.v("Saving lock pattern: %s", LockPatternUtils.patternToString(pattern));
        }
        // Compute the hash
        final byte[] hash = LockPatternUtils.patternToHash(pattern);
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(sLockPatternFilename, "rw");
            // Truncate the file if pattern is null, to clear the lock
            if (pattern == null) {
                raf.setLength(0);
            } else {
                raf.write(hash, 0, hash.length);
            }
            raf.close();
            setBoolean(PATTERN_EVER_CHOSEN, true);
        } catch (FileNotFoundException fnfe) {
            // Cant do much, unless we want to fail over to using the settings provider
            Timber.e(fnfe, "Unable to save lock pattern to %s", sLockPatternFilename);
        } catch (IOException ioe) {
            // Cant do much
            Timber.e(ioe, "Unable to save lock pattern to %s", sLockPatternFilename);
        }
    }

    /**
     * @return Whether the lock pattern is enabled.
     */
    public boolean isLockPatternEnabled() {
        return getBoolean(Settings.System.LOCK_PATTERN_ENABLED);
    }

    /**
     * Set whether the lock pattern is enabled.
     */
    public void setLockPatternEnabled(boolean enabled) {
        setBoolean(Settings.System.LOCK_PATTERN_ENABLED, enabled);
    }

    /**
     * @return Whether the visible pattern is enabled.
     */
    public boolean isVisiblePatternEnabled() {
        return getBoolean(Settings.System.LOCK_PATTERN_VISIBLE);
    }

    /**
     * Set whether the visible pattern is enabled.
     */
    public void setVisiblePatternEnabled(boolean enabled) {
        setBoolean(Settings.System.LOCK_PATTERN_VISIBLE, enabled);
    }

    /**
     * @return Whether tactile feedback for the pattern is enabled.
     */
    public boolean isTactileFeedbackEnabled() {
        return getBoolean(Settings.System.LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED);
    }

    /**
     * Set whether tactile feedback for the pattern is enabled.
     */
    public void setTactileFeedbackEnabled(boolean enabled) {
        setBoolean(Settings.System.LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED, enabled);
    }

    /**
     * Set and store the lockout deadline, meaning the user can't attempt his/her unlock
     * pattern until the deadline has passed.
     *
     * @return the chosen deadline.
     */
    public long setLockoutAttemptDeadline() {
        final long deadline = SystemClock.elapsedRealtime() + FAILED_ATTEMPT_TIMEOUT_MS;
        setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline);
        return deadline;
    }

    /**
     * @return The elapsed time in millis in the future when the user is allowed to
     * attempt to enter his/her lock pattern, or 0 if the user is welcome to
     * enter a pattern.
     */
    public long getLockoutAttemptDeadline() {
        final long deadline = getLong(LOCKOUT_ATTEMPT_DEADLINE, 0L);
        final long now = SystemClock.elapsedRealtime();
        if (deadline < now || deadline > (now + FAILED_ATTEMPT_TIMEOUT_MS)) {
            return 0L;
        }
        return deadline;
    }

    /**
     * @return Whether the user is permanently locked out until they verify their
     * credentials.  Occurs after {@link #FAILED_ATTEMPTS_BEFORE_RESET} failed
     * attempts.
     */
    public boolean isPermanentlyLocked() {
        return getBoolean(LOCKOUT_PERMANENT_KEY);
    }

    /**
     * Set the state of whether the device is permanently locked, meaning the user
     * must authenticate via other means.  If false, that means the user has gone
     * out of permanent lock, so the existing (forgotten) lock pattern needs to
     * be cleared.
     *
     * @param locked Whether the user is permanently locked out until they verify their
     *               credentials.  Occurs after {@link #FAILED_ATTEMPTS_BEFORE_RESET} failed
     *               attempts.
     */
    public void setPermanentlyLocked(boolean locked) {
        setBoolean(LOCKOUT_PERMANENT_KEY, locked);

        if (!locked) {
            setLockPatternEnabled(false);
            saveLockPattern(null);
        }
    }

    /**
     * @return A formatted string of the next alarm (for showing on the lock screen),
     * or null if there is no next alarm.
     */
    public String getNextAlarm() {
        String nextAlarm = Settings.System.getString(mContentResolver,
                Settings.System.NEXT_ALARM_FORMATTED);
        if (nextAlarm == null || TextUtils.isEmpty(nextAlarm)) {
            return null;
        }
        return nextAlarm;
    }

    private boolean getBoolean(String systemSettingKey) {
        return mPrefs.getBoolean(systemSettingKey, false);
    }

    private void setBoolean(String systemSettingKey, boolean enabled) {
        Editor editor = mPrefs.edit();
        editor.putBoolean(systemSettingKey, enabled);
        editor.apply();
    }

    private long getLong(String systemSettingKey, long def) {
        return mPrefs.getLong(systemSettingKey, def);
    }

    private void setLong(String systemSettingKey, long value) {
        Editor editor = mPrefs.edit();
        editor.putLong(systemSettingKey, value);
        editor.apply();
    }
}
