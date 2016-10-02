package com.liato.bankdroid.utils;

import java.io.UnsupportedEncodingException;

public class StringUtils {
    private static final String CHARSET = "UTF-8";

    private StringUtils() {
        // This constructor is here to prevent people from instantiating this utility class
    }

    public static byte[] getBytes(String string) {
        try {
            return string.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Internal error");
        }
    }

    public static String toString(byte[] bytes) {
        try {
            return new String(bytes, CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Internal error");
        }
    }
}
