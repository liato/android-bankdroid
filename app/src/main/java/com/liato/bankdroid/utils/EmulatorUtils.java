package com.liato.bankdroid.utils;

import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

class EmulatorUtils {
    static final boolean RUNNING_ON_EMULATOR = isRunningOnEmulator();
    static final boolean RUNNING_ON_ANDROID = isRunningOnAndroid();

    private EmulatorUtils() {
    }

    private static boolean isRunningOnEmulator() {
        // Inspired by
        // http://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in>
        if (Build.PRODUCT == null) {
            return false;
        }

        Set<String> parts = new HashSet<>(Arrays.asList(Build.PRODUCT.split("_")));
        if (parts.size() == 0) {
            return false;
        }

        parts.remove("sdk");
        parts.remove("google");
        parts.remove("x86");
        parts.remove("64");
        parts.remove("phone");

        // If the build identifier contains only the above keywords in some order, then we're
        // in an emulator
        return parts.isEmpty();
    }

    private static boolean isRunningOnAndroid() {
        // Inspired by:
        // https://developer.android.com/reference/java/lang/System.html#getProperties()
        // Developed using trial and error...
        final Properties properties = System.getProperties();
        final String httpAgent = (String) properties.get("http.agent");
        return httpAgent != null && httpAgent.contains("Android");
    }
}