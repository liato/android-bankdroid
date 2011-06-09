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

import com.liato.bankdroid.MainActivity;
import com.sonyericsson.extras.liveview.IPluginServiceCallbackV1;
import com.sonyericsson.extras.liveview.IPluginServiceV1;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Implementation of the Live View plug-in service.
 * 
 * @author firetech
 */
public class LiveViewService extends Service {
	// Announce intent information keys
	public static final String INTENT_EXTRA_ANNOUNCE = "isAnnounce";
	public static final String INTENT_EXTRA_TITLE = "title";
	public static final String INTENT_EXTRA_TEXT = "text";

    // Template menu icon file name.
    private static final String MENU_ICON_FILENAME = "plugin_icon.png";

    // There should only be one instance of the service
    protected static boolean alreadyRunning = false;

    // Plugin name
    protected String mPluginName = null;

    // Current plugin Id
    protected int mPluginId = 0;
    
    // Reference to LiveView application stub
    private IPluginServiceV1 mLiveView = null;

    // Menu icon that will be shown in LiveView unit
    protected String mMenuIcon = null;

    /**
     * LiveView callback interface method.
     */
    private class LiveViewCallback extends IPluginServiceCallbackV1.Stub {
        Handler mCallbackHandler = new Handler();

        @Override
        public String getPluginName() throws RemoteException {
            return mPluginName;
        }

        @Override
        public void openInPhone(final String openInPhoneAction) throws RemoteException {
            mCallbackHandler.post(new Runnable() {
                public void run() {
                    LiveViewService.this.openInPhone(openInPhoneAction);
                }
            });
        }

		//Unused methods required by API.
		public void startPlugin() throws RemoteException {}
		public void stopPlugin() throws RemoteException {}
		public void onUnregistered() throws RemoteException {}
		public void displayCaps(int displayWidthPixels, int displayHeigthPixels)
				throws RemoteException {}
		public void button(String buttonType, boolean doublepress,
				boolean longpress) throws RemoteException {}
		public void screenMode(int screenMode) throws RemoteException {}
    }

    /**
     * Check if service is already running.
     * 
     * @return running?
     */
    public static boolean isAlreadyRunning() {
        return alreadyRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(PluginConstants.LOG_TAG, "Enter LiveViewService.onCreate.");

        // Load menu icon
        int iconId = PluginUtils.getDynamicResourceId(this, "icon", "drawable");
        mMenuIcon = PluginUtils.storeIconToFile(this, getResources(), iconId, MENU_ICON_FILENAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(PluginConstants.LOG_TAG, "Enter LiveViewService.onDestroy.");

        // Unbind from LiveView service
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }

        // No longer a running service
        alreadyRunning = false;
    }

	@Override
	public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(PluginConstants.LOG_TAG, "Enter LiveViewService.onStart.");
        
		if (intent.getBooleanExtra(INTENT_EXTRA_ANNOUNCE, false)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				try {
					if(mLiveView != null) {
						mLiveView.sendAnnounce(mPluginId, mMenuIcon, extras.getString(INTENT_EXTRA_TITLE), 
					    		extras.getString(INTENT_EXTRA_TEXT), System.currentTimeMillis(), "");
						Log.d(PluginConstants.LOG_TAG, "Announce sent to LiveView Application");
					} else {
						Log.d(PluginConstants.LOG_TAG, "LiveView Application not reachable");
					}
				} catch(Exception e) {
					Log.e(PluginConstants.LOG_TAG, "Failed to send announce", e);
				}
			}
			
		} else {
			// We end up here when LiveView Application probes the plugin
	        if (isAlreadyRunning()) {
	            Log.d(PluginConstants.LOG_TAG, "Already started.");
	        } else {
	            // Init
	            mPluginName = PluginUtils.getDynamicResourceString(this,
	                    PluginConstants.RESOURCE_STRING_PLUGIN_NAME);

	            // Bind to LiveView
	            connectToLiveView();

	            // Singleton
	            alreadyRunning = true;
	        }
		}
	}

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(PluginConstants.LOG_TAG, "Enter LiveViewService.onBind.");
        return null;
    }

    /**
     * The service connection that is used to bind the plugin to the LiveView
     * service.
     * 
     * When connected to the service, the plugin is registered. When
     * disconnected to the service, the plugin is unregistered.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className, IBinder service) {
            Log.d(PluginConstants.LOG_TAG,
                    "Enter LiveViewService.ServiceConnection.onServiceConnected.");

            mLiveView = IPluginServiceV1.Stub.asInterface(service);

            // Init adapter
            LiveViewCallback lvCallback = new LiveViewCallback();

            // Install plugin
            try {
            	if(mLiveView != null) {
	                // Register
	                mPluginId = mLiveView.register(lvCallback, mMenuIcon, mPluginName, false, getPackageName());
	                Log.d(PluginConstants.LOG_TAG, "Plugin registered with id: " + mPluginId);
	            }
            } catch (RemoteException re) {
                Log.e(PluginConstants.LOG_TAG, "Failed to install plugin. Stop self.");
                stopSelf();
            }

            Log.d(PluginConstants.LOG_TAG, "Plugin registered. mPluginId: " + mPluginId);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(PluginConstants.LOG_TAG,
                    "Enter LiveViewService.ServiceConnection.onServiceDisconnected.");
            stopSelf();
        }

    };

    /**
     * Connects to the LiveView service.
     */
    private void connectToLiveView() {
        boolean result = bindService(new Intent(PluginConstants.LIVEVIEW_SERVICE_BIND_INTENT),
                mServiceConnection, 0);
        if (result) {
            Log.d(PluginConstants.LOG_TAG, "Bound to LiveView.");
        } else {
            Log.d(PluginConstants.LOG_TAG, "No bind.");
            stopSelf();
        }
    }

	/**
	 * When a user presses the "open in phone" button on the LiveView device, this method is called.
	 * 
	 * Opens the MainActivity on the phone.
	 */
	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone");
		Intent i = new Intent(this, MainActivity.class)
			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

}
