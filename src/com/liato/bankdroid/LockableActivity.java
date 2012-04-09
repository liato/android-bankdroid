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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.liato.bankdroid.lockpattern.ConfirmLockPattern;
import com.liato.bankdroid.lockpattern.LockPatternUtils;

public class LockableActivity extends Activity {
    private static int PATTERNLOCK_UNLOCK = 42;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	private LockPatternUtils mLockPatternUtils;
	private boolean mHasLoaded = false;
	protected boolean mSkipLockOnce = false;
	
	private LinearLayout mTitlebarButtons;
	private LayoutInflater mInflater;
	private ProgressBar mProgressBar;
	
    private ImageView mHomeButton;
    private View mHomeButtonCont;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mLockPatternUtils = new LockPatternUtils(this);		
        mLockPatternUtils.setVisiblePatternEnabled(mPrefs.getBoolean("patternlock_visible_pattern", true));
        mLockPatternUtils.setTactileFeedbackEnabled(mPrefs.getBoolean("patternlock_tactile_feedback", false));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }
	
	@Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        View titlebar = findViewById(R.id.layTitle);
        mTitlebarButtons = (LinearLayout)titlebar.findViewById(R.id.layTitleButtons);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mHomeButton = (ImageView)titlebar.findViewById(R.id.imgTitle);
        mHomeButtonCont = titlebar.findViewById(R.id.layLogoContainer);
        mProgressBar = (ProgressBar)titlebar.findViewById(R.id.progressBar);
        OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LockableActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                LockableActivity.this.finish();
            }
        };
        mHomeButton.setOnClickListener(listener);
        mHomeButtonCont.setOnClickListener(listener);
        setHomeButtonEnabled(true);
    }

    protected void addTitleButton(int imageResourceId, String tag, OnClickListener listener) {
        View child = mInflater.inflate(R.layout.title_item, mTitlebarButtons, false);
        ImageButton button = (ImageButton)child.findViewById(R.id.imgItemIcon);
        button.setImageResource(imageResourceId);
        button.setTag(tag);
        child.setTag("item_"+tag);
        button.setOnClickListener(listener);
        mTitlebarButtons.addView(child);
	}

    protected void hideTitleButton(String tag) {
        View v = mTitlebarButtons.findViewWithTag("item_"+tag);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }
    
    protected void showTitleButton(String tag) {
        View v = mTitlebarButtons.findViewWithTag("item_"+tag);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    protected void setTitleButtonEnabled(String tag, boolean enabled) {
        View v = mTitlebarButtons.findViewWithTag("item_"+tag);
        if (v != null) {
            ImageButton button = (ImageButton)v.findViewById(R.id.imgItemIcon);
            if (button != null) {
                v.setEnabled(enabled);
                v.setFocusable(enabled);
                button.setEnabled(enabled);
                button.setAlpha(enabled ? 255 : 50);
            }
        }
    }
    
    protected void setHomeButtonEnabled(boolean enabled) {
        mHomeButtonCont.setFocusable(enabled);
        mHomeButtonCont.setClickable(enabled);
        mHomeButton.setFocusable(enabled);
        mHomeButton.setClickable(enabled);
    }    
    
    protected void setProgressBar(int progress) {
        mProgressBar.setProgress(progress);
    }

    protected void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(350);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {}
        });
        mProgressBar.startAnimation(animation);
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		// Don't do anything if no lock pattern is set
		if (!mLockPatternUtils.isLockPatternEnabled()) return;
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
		    writeLockTime(SystemClock.elapsedRealtime()-10000);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
        // Don't do anything if no lock pattern is set
		if (!mLockPatternUtils.isLockPatternEnabled()) {
		    return;
		}
		if (mSkipLockOnce) {
		    mSkipLockOnce = false;
		    return;
		}
		// If a lock pattern is set we need to check the time for when the last
		// activity was open. If it's been more than two seconds the user
		// will have to enter the lock pattern to continue.
		long currentTime = SystemClock.elapsedRealtime();
		long lockedAt = mPrefs.getLong("locked_at", currentTime-10000);
		long timedif = Math.abs(currentTime - lockedAt);
        Log.d("Lock", "timedif: " + timedif);
		if (timedif > 2000) {
            mHasLoaded = false;         
		    launchPatternLock();
		}
		else {
		    mHasLoaded = true;		    
		}
	}

	private void launchPatternLock() {
        Intent intent = new Intent(this, ConfirmLockPattern.class);
        intent.putExtra(ConfirmLockPattern.DISABLE_BACK_KEY, true);
        intent.putExtra(ConfirmLockPattern.HEADER_TEXT, getText(R.string.patternlock_header));
        startActivityForResult(intent, PATTERNLOCK_UNLOCK);         
	}
	
	private void writeLockTime() {
        writeLockTime(SystemClock.elapsedRealtime());
	}

    private void writeLockTime(long time) {
        mEditor = mPrefs.edit();
        mEditor.putLong("locked_at", time);
        mEditor.commit();       
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
    
    protected void skipLockOnce() {
        mSkipLockOnce = true;
    }
}
