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

interface IPluginServiceCallbackV1 {
	// Start the plugin.
	void startPlugin();
	
	// Stop the plugin.
	// A stopped plugin should stop its polling, but can stay alive
	void stopPlugin();
	
	// Should return the name the plugin used to register itself with
	String getPluginName();

 	// Give the action needed to open the current announcement on the phone
	// such as a view in browser action or something else that your application
	// responds to.
	void openInPhone(in String openInPhoneAction);      

	// Kicked out by framework. Implement this with stopSelf()
	void onUnregistered();
	
	// displayWidthPixels equals 0 and displayheigthPixels equals 0
	// means no available device is attached, or has no display
	void displayCaps(in int displayWidthPixels, in int displayHeigthPixels);
	
	// Button event - note that doublepress is not implemented for the V1
	// interface but still left here for compatibility reasons.
	void button(in String buttonType, in boolean doublepress, in boolean longpress);

	// Screen mode changed event - this notifies the current active sandbox plugin that the screen has been 
	// turned on or off. 0 = Screen OFF, 1 = Screen ON
	void screenMode(in int screenMode);
}