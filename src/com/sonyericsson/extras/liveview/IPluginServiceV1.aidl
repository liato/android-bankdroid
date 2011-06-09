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

package com.sonyericsson.extras.liveview;

import com.sonyericsson.extras.liveview.IPluginServiceCallbackV1;

interface IPluginServiceV1 {

	// Register to the Liveview application
	// cb - callback instance
    // imageMenu - path to the menu bitmap
    // pluginName - name of the plugin - must be unique
    // selectableMenu - is set to true if controlling display and getting buttons. Set to false to only handle announces
    // packageName - the package name (use getPackageName()).
    // returns id of plugin in system, 0 means that the registration failed
    int register(in IPluginServiceCallbackV1 cb, in String imageMenu, in String pluginName, in boolean selectableMenu, in String packageName);
    
    // This method should be called if the application/service is uninstalled using the phone application handler 
    // id - the plugin id received in registerPlugin
    // cb - the callback
    void unregister( in int id, in IPluginServiceCallbackV1 cb);
    
    // Used to send announcements to the device - can only be used when _not_ registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    // imageAnnounce - the path to the announce bitmap
    // header - header text
    // body - body text
    // time - timestamp for this announce in milliseconds
    // openInPhoneAction - a tag to use for openInPhone callback. Set to null when announce not selectable
    void sendAnnounce(in int id, in String imageAnnounce, in String header, in String body, in long timestamp, in String openInPhoneAction);
    
    // Used to send image data to the device while in sandbox mode - Can only be used if you registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    // x - from left side
    // y - from top side
    // image - the path to the image on file system
    void sendImage(in int id, in int x, in int y, in String image);

    // Used to send image data to the device while in sandbox mode - Can only be used if you registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    // x - from left side
    // y - from top side
    // bitmapData - the bitmap to send
    void sendImageAsBitmap(in int id, in int x, in int y, in Bitmap bitmapData);
    
    // Clears the display, for example if several images are sent while in sandbox mode - Can only be used if you registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    void clearDisplay(in int id);
    
    // Provide the Liveview application with means to launch the service 
    // that shoul receive and send data in sandbox mode - Must be called if you registered as "selectableMenu"
    // launcherIntent - the intent to start the plugin service
    // pluginName - the name of the plugin, must match the name you registered with! 
    // returns -1 for failure, 0 for already registered, anything else for success
    int notifyInstalled(in String launcherIntent, in String pluginName);

    // Controls LED - can only be used if you registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    // rgb565 - the color to use
    // delayTime - the delay in ms
    // onTime - the on time in ms
    void ledControl(in int id, int rgb565, int delayTime, int onTime);

    // Controls Vibration - can only be used if you registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    // delayTime - the delay in ms
    // onTime - the on time in ms
    void vibrateControl(in int id, int delayTime, int onTime);

    // Used to send image data to the device while in sandbox mode - Can only be used if you registered as "selectableMenu"
    // id - the plugin id received in registerPlugin
    // x - from left side
    // y - from top side
    // bitmapBytes - the byteArray containing the bitmap data
    void sendImageAsBitmapByteArray(in int id, in int x, in int y, in byte[] bitmapByteArray);

    // Used to put the screen in powersave mode
    // id - the plugin id received in registerPlugin
    void screenOff(in int id);

    // Used to put the screen in dimmed mode
    // id - the plugin id received in registerPlugin
    void screenDim(in int id);

    // Used to wake the screen from powersave mode
    // id - the plugin id received in registerPlugin
    void screenOn(in int id);

    // Used to set the to powersave mode "AUTO"
    // id - the plugin id received in registerPlugin
    void screenOnAuto(in int id);
}