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
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LockableActivity extends Activity {
    private static int PATTERNLOCK_UNLOCK = 42;
	private SharedPreferences prefs;
	private Editor editor;
	private LockPatternUtils mLockPatternUtils;
	
	private LinearLayout mTitlebarButtons;
	private LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mLockPatternUtils = new LockPatternUtils(this);		
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }
	
	@Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        View titlebar = findViewById(R.id.layTitle);
        mTitlebarButtons = (LinearLayout)titlebar.findViewById(R.id.layTitleButtons);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ImageView homeButton = (ImageView)titlebar.findViewById(R.id.imgTitle);
        OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LockableActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };        
        homeButton.setOnClickListener(listener);
        homeButton.setFocusable(true);
        homeButton.setClickable(true);
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
