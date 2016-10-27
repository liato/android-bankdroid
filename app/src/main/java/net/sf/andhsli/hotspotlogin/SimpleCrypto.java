/*
 * Copyright (c) 2009 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * http://www.androidsnippets.org/snippets/39/index.html
 */

package net.sf.andhsli.hotspotlogin;

import com.liato.bankdroid.utils.StringUtils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Usage:
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>
 *
 * @deprecated <a href="http://android-developers.blogspot.se/2016/06/security-crypto-provider-deprecated-in.html">Broken
 * on Android Nougat</a>,
 * <a href="https://android.googlesource.com/platform/tools/base/+/2d252fc75960a3eaf7297c7a7713baf0c60b6aed/lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks/CipherGetInstanceDetector.java#44">considered
 * broken by Android Lint even before then</a>.
 *
 * @author ferenc.hechler
 */
public class SimpleCrypto {
    public static String decrypt(String seed, String encrypted) throws Exception {
        byte[] rawKey = getRawKey(StringUtils.getBytes(seed));
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return StringUtils.toString(result);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr;
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        } else {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }
}
