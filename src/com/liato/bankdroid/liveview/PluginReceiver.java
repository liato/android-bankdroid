/*
 * Copyright (C) 2011 Nullbyte <http://nullbyte.eu>
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

/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.liato.bankdroid.liveview;

import com.liato.bankdroid.SettingsActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receives broadcast intents from LiveView service.
 * 
 * @author firetech
 */
public class PluginReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String command = intent.getExtras().getString(PluginConstants.BROADCAST_COMMAND);
		Log.d(PluginConstants.LOG_TAG, "Received command: " + command);
		
		if(command == null) {
			return;
		}
		
		if(command.contentEquals(PluginConstants.BROADCAST_COMMAND_PREFERENCES)) {
			String pluginName = intent.getExtras().getString(PluginConstants.BROADCAST_COMMAND_PLUGIN_NAME);
			String myPluginName = PluginUtils.getDynamicResourceString(context, PluginConstants.RESOURCE_STRING_PLUGIN_NAME);

			if(pluginName != null && pluginName.contentEquals(myPluginName)) {
				Log.d(PluginConstants.LOG_TAG, "Starting preferences!");
				
				Intent prefsIntent = new Intent(context, SettingsActivity.class);
				prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(prefsIntent);
			}
		}
		else if(command.contentEquals(PluginConstants.BROADCAST_COMMAND_START)) {
			if(LiveViewService.isAlreadyRunning()) {
			    Log.d(PluginConstants.LOG_TAG, "Service is already running.");
			} else {
				Log.d(PluginConstants.LOG_TAG, "Starting service!");
				
				context.startService(new Intent(context, LiveViewService.class));
			}
		}
		
	}
	
}