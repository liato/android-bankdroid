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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utils for LiveView plugin.
 *
 * @author firetech
 */
public final class PluginUtils {

    private PluginUtils() {

    }

    /**
     * Stores icon to phone file system
     *
     * @param resources Reference to project resources
     * @param resource  Reference to specific resource
     * @param fileName  The icon file name
     */
    public static String storeIconToFile(Context ctx, Resources resources, int resource,
            String fileName) {
        Log.d(PluginConstants.LOG_TAG, "Store icon to file.");

        if (resources == null) {
            return "";
        }

        Bitmap bitmap = BitmapFactory.decodeStream(resources.openRawResource(resource));

        try {
            FileOutputStream fos = ctx.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(PluginConstants.LOG_TAG, "Failed to store to device", e);
        }

        File iconFile = ctx.getFileStreamPath(fileName);
        Log.d(PluginConstants.LOG_TAG, "Icon stored. " + iconFile.getAbsolutePath());

        return iconFile.getAbsolutePath();
    }

    /**
     * Gets resource id dynamically
     */
    public static int getDynamicResourceId(Context context, String resourceName,
            String resourceType) {
        return context.getResources()
                .getIdentifier(resourceName, resourceType, context.getPackageName());
    }

    /**
     * Gets resource string dynamically
     */
    public static String getDynamicResourceString(Context context, String resourceName) {
        int resourceId = getDynamicResourceId(context, resourceName, "string");
        return context.getString(resourceId);
    }
}
