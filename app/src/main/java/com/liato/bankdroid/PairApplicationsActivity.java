/**
 * Copyright 2011 Magnus Andersson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liato.bankdroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liato.bankdroid.provider.BankTransactionsProvider;
import com.liato.bankdroid.provider.IBankTransactionsProvider;

/**
 * @author Magnus Andersson
 * @since 8 jun 2011
 */
public class PairApplicationsActivity extends LockableActivity {

	/**
	 * 
	 */
	private static final String TAG = "Pair Application";
	private static final String PAIR_APP_NAME = "com.liato.bankdroid.PAIR_APP_NAME";

	/** {@inheritDoc} */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pair_applications_layout);
		setHomeButtonEnabled(false);
		
		Bundle bundle = getIntent().getExtras();

		if(bundle.containsKey(PAIR_APP_NAME)) {
			String appName = bundle.getString(PAIR_APP_NAME);

			ImageView img = (ImageView) findViewById(R.id.imageView1);
			String logoUri = "drawable/applogo_"+appName.toLowerCase();

			// Dynamically load logo
			int imageResource = getResources().getIdentifier(logoUri, null, getPackageName());

			try {
				Drawable d = getResources().getDrawable(imageResource);
				img.setImageDrawable(d);
				img.requestLayout();
			} catch (NotFoundException e) {
				Log.e(TAG, "Could not find the requested image using uri " + logoUri);
				e.printStackTrace();
			}
			// Change application name
			TextView appNameView = (TextView) findViewById(R.id.app_name);
			appNameView.setText(appName);
		} else {
			Log.w(TAG, "Unknown application");
		}


	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	} 

	public void cancelPairing(final View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void confirmPairing(final View v) {
		Intent intent = this.getIntent();

		Context ctx = getBaseContext();

		// Make sure sharing is enabled
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor editor = pref.edit();
		editor.putBoolean("content_provider_enabled", true);
		editor.commit();
        String apiKey;

        try{
		    apiKey = BankTransactionsProvider.getApiKey(ctx);
        }
        catch (IllegalArgumentException e){
            //Initialize API key if it is not set
            initialSetupApiKey(ctx);
            apiKey = BankTransactionsProvider.getApiKey(ctx);
        }

		intent.putExtra(IBankTransactionsProvider.API_KEY,apiKey);
		setResult(RESULT_OK, intent);

		finish();
	}

	public static void initialSetupApiKey(Context ctx) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		final String apiKey = prefs.getString("content_provider_api_key", "");
		if(apiKey.equals("")) {
			final SharedPreferences.Editor editor = prefs.edit();

			// Create a random HEX String
			final String genKey = Long.toHexString(Double.doubleToLongBits(Math.random()));

			// Commit to preferences
			editor.putString("content_provider_api_key", genKey.toUpperCase());
			editor.commit();
		}
	}
}
