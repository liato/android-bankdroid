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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.liato.bankdroid.lockpattern.ConfirmLockPattern;
import com.liato.bankdroid.lockpattern.LockPatternUtils;

public class LockableActivity extends ActionBarActivity {
    private static int PATTERNLOCK_UNLOCK = 42;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	private LockPatternUtils mLockPatternUtils;
	private boolean mHasLoaded = false;
	protected boolean mSkipLockOnce = false;
	
//	private LinearLayout mTitlebarButtons;
	private LayoutInflater mInflater;
//	private ProgressBar mProgressBar;
	
//    private ImageView mHomeButton;
//    private View mHomeButtonCont;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mLockPatternUtils = new LockPatternUtils(this);
        mLockPatternUtils.setVisiblePatternEnabled(mPrefs.getBoolean("patternlock_visible_pattern", true));
        mLockPatternUtils.setTactileFeedbackEnabled(mPrefs.getBoolean("patternlock_tactile_feedback", false));
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }
	
	@Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            if (shouldShowActionBar()) {
                setSupportActionBar(toolbar);
                toolbar.setLogo(R.drawable.ic_launcher);
            } else {
                toolbar.setVisibility(View.GONE);
            }

        }
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
//        View titlebar = findViewById(R.id.layTitle);
//        mTitlebarButtons = (LinearLayout)titlebar.findViewById(R.id.layTitleButtons);
//        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        mHomeButton = (ImageView)titlebar.findViewById(R.id.imgTitle);
//        mHomeButtonCont = titlebar.findViewById(R.id.layLogoContainer);
//        mProgressBar = (ProgressBar)titlebar.findViewById(R.id.progressBar);
//        OnClickListener listener = new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intent = new Intent(LockableActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                LockableActivity.this.finish();
//            }
//        };
//        mHomeButton.setOnClickListener(listener);
//        mHomeButtonCont.setOnClickListener(listener);
//        setHomeButtonEnabled(true);
    }

//    protected void addTitleButton(int imageResourceId, String tag, OnClickListener listener) {
//        View child = mInflater.inflate(R.layout.title_item, mTitlebarButtons, false);
//        ImageButton button = (ImageButton)child.findViewById(R.id.imgItemIcon);
//        button.setImageResource(imageResourceId);
//        button.setTag(tag);
//        child.setTag("item_"+tag);
//        button.setOnClickListener(listener);
//        mTitlebarButtons.addView(child);
//	}
//
//    protected void hideTitleButton(String tag) {
//        View v = mTitlebarButtons.findViewWithTag("item_"+tag);
//        if (v != null) {
//            v.setVisibility(View.GONE);
//        }
//    }
//
//    protected void showTitleButton(String tag) {
//        View v = mTitlebarButtons.findViewWithTag("item_"+tag);
//        if (v != null) {
//            v.setVisibility(View.VISIBLE);
//        }
//    }
//
//    protected void setTitleButtonEnabled(String tag, boolean enabled) {
//        View v = mTitlebarButtons.findViewWithTag("item_"+tag);
//        if (v != null) {
//            ImageButton button = (ImageButton)v.findViewById(R.id.imgItemIcon);
//            if (button != null) {
//                v.setEnabled(enabled);
//                v.setFocusable(enabled);
//                button.setEnabled(enabled);
//                button.setAlpha(enabled ? 255 : 50);
//            }
//        }
//    }
//
//    protected void setHomeButtonEnabled(boolean enabled) {
//        mHomeButtonCont.setFocusable(enabled);
//        mHomeButtonCont.setClickable(enabled);
//        mHomeButton.setFocusable(enabled);
//        mHomeButton.setClickable(enabled);
//    }
//
//    protected void setProgressBar(int progress) {
//        mProgressBar.setProgress(progress);
//    }
//
//    protected void showProgressBar() {
//        mProgressBar.setVisibility(View.VISIBLE);
//    }
//
//    protected void hideProgressBar() {
//        AlphaAnimation animation = new AlphaAnimation(1, 0);
//        animation.setDuration(350);
//        animation.setAnimationListener(new AnimationListener() {
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mProgressBar.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {}
//
//            @Override
//            public void onAnimationStart(Animation animation) {}
//        });
//        mProgressBar.startAnimation(animation);
//    }
    
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
    
    
    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        View decorview = getWindow().getDecorView();
        if (decorview == null) {
            return true;
        }

        final int vw = decorview.getWidth();
        final int vh = decorview.getHeight();
        final int dw = outBitmap.getWidth();
        final int dh = outBitmap.getHeight();

        Bitmap bluredBitmap = Bitmap.createBitmap(outBitmap.getWidth(), outBitmap.getHeight(), outBitmap.getConfig());
        Canvas c = new Canvas(bluredBitmap);
        
        c.save();
        c.scale(((float)dw)/vw, ((float)dh)/vh);
        decorview.draw(c);
        c.restore();        

        canvas.drawBitmap(pixelate(bluredBitmap, 5), 0, 0, null);
        Bitmap lockbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lock);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setFilterBitmap(true);
        
        canvas.drawBitmap(lockbitmap, null, new RectF(dw*0.25f,dh*0.25f,dw*0.75f,dh*0.75f), p);
        
        return true;
    }
    
    private Bitmap pixelate(Bitmap bitmap, int size) {
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setStyle(Style.FILL);
        int w = bm.getWidth();
        int h = bm.getHeight();
        
        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < h; i = i+size) {
            for (int j = 0; j < w; j = j+size) {
                int a = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                int pc = 0;
                for (int k = 0; k < size; k++) {
                    for (int l = 0; l < size; l++) {
                        int pxp = (i+k)*w+j+l;
                        if (pxp < pixels.length) {
                            int pixel = pixels[pxp];
                            a += Color.alpha(pixel);
                            r += Color.red(pixel);
                            g += Color.green(pixel);
                            b += Color.blue(pixel);
                            pc++;
                        }
                    }
                }
                a /= pc;
                r /= pc;
                g /= pc;
                b /= pc;
                p.setColor(Color.argb(a, r, g, b));
                c.drawRect(j, i, j+size, i+size, p);
            }
        }
        return bm;
    }

    public boolean shouldShowActionBar() {
        return true;
    }
    
}
