/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.liato.bankdroid.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class ChooseLockPatternExample extends Activity implements View.OnClickListener {

    private static final int REQUESTCODE_CHOOSE = 1;

    private static final long START_DELAY = 1000;

    private View mNextButton;

    private View mSkipButton;

    private AnimationDrawable mAnimation;

    private Runnable mRunnable = new Runnable() {
        public void run() {
            startAnimation(mAnimation);
        }
    };

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lock_pattern_example);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, START_DELAY);
    }

    @Override
    protected void onPause() {
        stopAnimation(mAnimation);
        super.onPause();
    }

    public void onClick(View v) {
        if (v == mSkipButton) {
            // Canceling, so finish all
            setResult(ChooseLockPattern.RESULT_FINISHED);
            finish();
        } else if (v == mNextButton) {
            stopAnimation(mAnimation);
            Intent intent = new Intent(this, ChooseLockPattern.class);
            startActivityForResult(intent, REQUESTCODE_CHOOSE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE_CHOOSE && resultCode == ChooseLockPattern.RESULT_FINISHED) {
            setResult(resultCode);
            finish();
        }
    }

    private void initViews() {
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);

        mSkipButton = findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(this);

        View imageView = (ImageView) findViewById(R.id.lock_anim);
        imageView.setBackgroundResource(R.drawable.lock_anim);
        imageView.setOnClickListener(this);
        mAnimation = (AnimationDrawable) imageView.getBackground();
    }

    protected void startAnimation(final AnimationDrawable animation) {
        if (animation != null && !animation.isRunning()) {
            animation.run();
        }
    }

    protected void stopAnimation(final AnimationDrawable animation) {
        if (animation != null && animation.isRunning()) {
            animation.stop();
        }
    }
}

