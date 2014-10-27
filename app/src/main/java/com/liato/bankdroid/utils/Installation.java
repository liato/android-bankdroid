package com.liato.bankdroid.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 * Provides a unique identification for the installation.
 *
 * The id is generated the first time the {@link #id(android.content.Context)} is called
 * and are then persisted until the application is uninstalled.
 *
 * @see <a href="http://android-developers.blogspot.se/2011/03/identifying-app-installations.html">Identifying App Installations</a>.
 */
public class Installation {

    private static String sID = null;

    private static final String INSTALLATION = "INSTALLATION";

    /**
     * Get the unique identification for the installation.
     * A new id will be generated the first time the method is called and are then
     * persisted until the application is uninstalled.
     * @return The unique identification for the installed application.
     */
    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}
