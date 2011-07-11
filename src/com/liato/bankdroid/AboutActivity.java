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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends LockableActivity implements OnClickListener {
	final static String TAG = "AboutActivity";
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
        PackageInfo pInfo;
        String version = "v1.x.x";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            version = pInfo.versionName;
        } catch (final NameNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView)findViewById(R.id.txtVersion)).setText(getText(R.string.version).toString().replace("$version", version));	    
        this.addTitleButton(R.drawable.title_icon_donate, "donate", this);
        this.addTitleButton(R.drawable.title_icon_web, "web", this);
 
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

    @Override
    public void onClick(View v) {
        String tag = (String)v.getTag();
        Intent i = new Intent(Intent.ACTION_VIEW);
        
        if ("web".equals(tag)) {
            i.setData(Uri.parse("https://github.com/liato/android-bankdroid"));
        }
        else if ("donate".equals(tag)) {
            i.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=KWRCBB4PAA3LC"));
        }
        startActivity(i);
    }
	
}
